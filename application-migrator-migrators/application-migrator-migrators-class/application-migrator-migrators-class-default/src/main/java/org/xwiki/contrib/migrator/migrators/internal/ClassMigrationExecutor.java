/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.migrator.migrators.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.contrib.migrator.MigrationException;
import org.xwiki.contrib.migrator.MigrationExecutor;
import org.xwiki.contrib.migrator.MigrationStatus;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.text.StringUtils;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;

/**
 * Executes migrations of type {@link ClassMigrationType}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class ClassMigrationExecutor implements MigrationExecutor<ClassMigrationDescriptor>
{
    /**
     * Properties coming from the XWiki document itself, that are not stored as XProperties.
     */
    private static final List<String> DOCUMENT_PROPERTIES = Arrays.asList(
        ClassMigrationDescriptor.DOC_CONTENT_MAPPING_PROPERTY,
        ClassMigrationDescriptor.DOC_TITLE_MAPPING_PROPERTY);

    @Inject
    private QueryManager queryManager;

    @Inject
    private Provider<XWikiContext> xWikiContextProvider;

    @Inject
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @Inject
    private Logger logger;

    // As we're instantiating the executor per lookup (and not as a singleton), we can have the freedom to
    // store private attributes without them interfering with other executions.

    private XWikiContext xWikiContext;

    private XWiki xwiki;

    private ClassMigrationDescriptor migrationDescriptor;

    private ClassMigrationParameters migrationParameters;

    private DocumentReference oldClassReference;

    private DocumentReference newClassReference;

    private Map<String, String> classPropertiesMapping;

    @Override
    public MigrationStatus execute(ClassMigrationDescriptor migrationDescriptor) throws MigrationException
    {
        /*
          TODO: Rely more on the MigrationStatus that we have to return as it will determine whether we can store
          the migration and mark it as done or not.
         */

        // Initialize the essential attributes of our executor
        this.migrationDescriptor = migrationDescriptor;
        migrationParameters = (ClassMigrationParameters) migrationDescriptor.getMigrationParameters();
        classPropertiesMapping = new HashMap<>();

        xWikiContext = xWikiContextProvider.get();
        xwiki = xWikiContext.getWiki();

        // Transform some parameters that we got so that we don't need to repeat the operation multiple times
        oldClassReference = documentReferenceResolver.resolve(migrationParameters.getOldClass());

        if (migrationParameters.isInPlace() && migrationParameters.getNewClass().equals(StringUtils.EMPTY)) {
            newClassReference = oldClassReference;
        } else {
            newClassReference = documentReferenceResolver.resolve(migrationParameters.getNewClass());
        }

        if (!migrationParameters.isInPlace() && !xwiki.exists(newClassReference, xWikiContext)) {
            logger.error("The new class reference does not exists! Aborting ...");
            throw new MigrationException("Failed to migrate the XClasses : the new class does not exist.");
        }

        // Step 0 : Construct the complete mapping that we'll use to migrate one object to another
        constructPropertiesMapping();

        // Step 1 : Migrate the XObjects (if needed, delete the old XObjects)
        migrateAllXObjects();

        // Step 2 : If needed, remove the old XClass
        if (!migrationParameters.isInPlace() && migrationParameters.isRemoveOldXClass()
            && xwiki.exists(oldClassReference, xWikiContext)) {
            try {
                xwiki.deleteDocument(xwiki.getDocument(oldClassReference, xWikiContext), false, xWikiContext);
            } catch (XWikiException e) {
                throw new MigrationException(
                        String.format("Failed to remove the old XClass [%s]", migrationParameters.getOldClass()));
            }
        }

        return MigrationStatus.SUCCESS;
    }

    private void constructPropertiesMapping() throws MigrationException
    {
        logger.info("Building properties mapping ...");
        try {
            // Here again, three steps
            // Step 1 : Get the current structures of the two XClasses
            Set<String> oldProperties =
                    xwiki.getDocument(oldClassReference, xWikiContext).getXClass().getPropertyList();
            Set<String> newProperties =
                    xwiki.getDocument(newClassReference, xWikiContext).getXClass().getPropertyList();

            // Add the document content and title as properties
            oldProperties.addAll(DOCUMENT_PROPERTIES);
            newProperties.addAll(DOCUMENT_PROPERTIES);

            // Intersect the two properties to get the ones that have a good mapping "by default" as the properties
            // have the same name.
            oldProperties.retainAll(newProperties);
            oldProperties.forEach(property -> classPropertiesMapping.put(property, property));
        } catch (XWikiException e) {
            throw new MigrationException("Failed to construct property mapping of the XClasses.", e);
        }

        // Step 2 : Add the custom mapping ; update the existing default mapping if needed
        classPropertiesMapping.putAll(migrationParameters.getPropertiesMapping());
        logger.info("{} properties mapped.", classPropertiesMapping.size());

        if (logger.isDebugEnabled()) {
            logger.debug("Complete mapping :");
            for (String key : classPropertiesMapping.keySet()) {
                logger.debug("[{}] => [{}]", key, classPropertiesMapping.get(key));
            }
        }
    }

    private void migrateAllXObjects() throws MigrationException
    {
        logger.info("Migrating XObjects from XClass [{}] to new XClass [{}].", oldClassReference, newClassReference);
        try {
            // Get a list of XObjects implementing the old XClass
            Query query = queryManager.createQuery("select distinct doc.fullName "
                    + "from XWikiDocument doc, BaseObject obj "
                    + "where doc.fullName = obj.name and obj.className = :oldClassName", Query.HQL);

            List<String> results = query.bindValue("oldClassName", migrationParameters.getOldClass()).execute();

            for (String result : results) {
                try {
                    // TODO: Log something if the document is null
                    migrateDocumentXObjects(xwiki.getDocument(documentReferenceResolver.resolve(result), xWikiContext));
                } catch (XWikiException e) {
                    throw new MigrationException(String.format("Failed to retrieve document [%s].", result), e);
                }
            }
        } catch (QueryException e) {
            throw new MigrationException("Failed to get a list of documents to migrate.", e);
        }
    }

    private void migrateDocumentXObjects(XWikiDocument document) throws MigrationException
    {
        if (document != null) {
            logger.debug("Migrating XObjects of document [{}] ...", document.getDocumentReference());
            List<BaseObject> objects = document.getXObjects(oldClassReference);

            for (BaseObject object : objects) {
                if (object != null && migrationParameters.isInPlace()) {
                    logger.debug("Migrating XObject [{}] in place ...", object);
                    migrateProperties(document, object, object);
                } else if (object != null) {
                    logger.debug("Migrating XObject [{}], creating XObject with the new XClass ...", object);
                    document.addXObject(migrateXObject(document, object));
                }
            }

            // If asked, we remove the old XObjects
            if (!migrationParameters.isInPlace() && migrationParameters.isRemoveOldXObjects()) {
                removeOldXObjects(document, objects);
            }

            String saveComment = String.format("Migrate objects from XClass [%s] to XClass [%s] "
                            + "as part of the migration \"%s\" (%s)",
                    migrationParameters.getOldClass(),
                    migrationParameters.getNewClass(),
                    migrationDescriptor.getMigrationName(),
                    migrationDescriptor.getMigrationUUID());

            try {
                xwiki.saveDocument(document, saveComment, xWikiContext);
            } catch (XWikiException e) {
                throw new MigrationException(String.format("Failed to save migrated document [%s]",
                        document.getDocumentReference()), e);
            }
        }
    }

    private void removeOldXObjects(XWikiDocument document, List<BaseObject> objects)
    {
        logger.debug("Removing old XObjects from the document [{}] ...", document.getDocumentReference());
        for (BaseObject object : objects) {
            if (object != null) {
                document.removeXObject(object);
            }
        }
    }

    private BaseObject migrateXObject(XWikiDocument document, BaseObject oldObject) throws MigrationException
    {
        BaseObject newObject = new BaseObject();
        newObject.setXClassReference(newClassReference);

        migrateProperties(document, oldObject, newObject);

        return newObject;
    }

    private void migrateProperties(XWikiDocument document, BaseObject oldObject, BaseObject newObject)
        throws MigrationException
    {
        for (Map.Entry<String, String> mappingEntry : classPropertiesMapping.entrySet()) {
            if (!migrationParameters.isInPlace() || !mappingEntry.getKey().equals(mappingEntry.getValue())) {

                // Handle the case where we migrate the document content or the document title from the original object
                // to the new one
                Object oldValue;
                if (mappingEntry.getKey().equals(ClassMigrationDescriptor.DOC_CONTENT_MAPPING_PROPERTY)) {
                    oldValue = document.getContent();
                } else if (mappingEntry.getKey().equals(ClassMigrationDescriptor.DOC_TITLE_MAPPING_PROPERTY)) {
                    oldValue = document.getTitle();
                } else {
                    oldValue = ((BaseProperty) oldObject.safeget(mappingEntry.getKey())).getValue();
                }

                // Handle the case where we migrate the property to the document content or the document title
                if (DOCUMENT_PROPERTIES.contains(mappingEntry.getValue())) {
                    if (!(oldValue instanceof String)) {
                        logger.warn("Not migrating property [{}] to [{}] as the value of the value of the property is"
                            + " not a string.", mappingEntry.getKey(), mappingEntry.getValue());
                    } else if (mappingEntry.getValue().equals(ClassMigrationDescriptor.DOC_TITLE_MAPPING_PROPERTY)) {
                        document.setTitle((String) oldValue);
                    } else if (mappingEntry.getValue().equals(ClassMigrationDescriptor.DOC_CONTENT_MAPPING_PROPERTY)) {
                        document.setContent((String) oldValue);
                    }
                } else {
                    newObject.set(mappingEntry.getValue(), oldValue, xWikiContext);
                }
            }
        }
    }
}
