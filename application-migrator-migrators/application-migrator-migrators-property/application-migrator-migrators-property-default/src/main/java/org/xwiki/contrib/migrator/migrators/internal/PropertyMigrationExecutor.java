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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.migrator.MigrationException;
import org.xwiki.contrib.migrator.MigrationExecutor;
import org.xwiki.contrib.migrator.MigrationStatus;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;

/**
 * Executes migrations of type {@link PropertyMigrationType}.
 * 
 * @version $Id$
 * @since 1.1
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class PropertyMigrationExecutor implements MigrationExecutor<PropertyMigrationDescriptor>
{
    private static final String STRING_TYPE = "String";

    private static final String TEXT_AREA_TYPE = "TextArea";

    private static final String NUMBER_TYPE = "Number";

    private static final String EMAIL_TYPE = "EMail";

    @Inject
    private QueryManager queryManager;

    @Inject
    private Provider<XWikiContext> xWikiContextProvider;

    @Inject
    private Logger logger;

    @Inject
    private DocumentReferenceResolver<String> documentReferenceResolver;

    private XWikiContext xWikiContext;

    private XWiki xwiki;

    private PropertyMigrationDescriptor migrationDescriptor;

    private PropertyMigrationParameters migrationParameters;

    private DocumentReference targetClassReference;

    private String oldPropertyReference;

    private String oldPropertyType;

    private String newPropertyType;

    private String newPropertyReference;

    private PropertyConversionCheckerProvider provider = new PropertyConversionCheckerProvider();

    @Inject
    @Named("context")
    private ComponentManager componentManager;

    static class PropertyConversionCheckerProvider
    {
        private List<PropertyConversionChecker> checkList;

        PropertyConversionCheckerProvider()
        {
            this.checkList = new ArrayList<PropertyConversionChecker>();
        }

        public List<PropertyConversionChecker> getList()
        {
            return this.checkList;
        }
    }

    private class PropertyConversionChecker
    {
        private List<String> allSupportedProperties;

        private String propertyType;

        PropertyConversionChecker(String propertyType)
        {
            this.propertyType = propertyType;
            this.allSupportedProperties = new ArrayList<String>();
        }

        public List<String> getAllSupportedProperties()
        {
            return this.allSupportedProperties;
        }

        public String getPropertyType()
        {
            return this.propertyType;
        }
    }

    @Override
    public MigrationStatus execute(PropertyMigrationDescriptor migrationDescriptor) throws MigrationException
    {
        this.migrationDescriptor = migrationDescriptor;
        migrationParameters = (PropertyMigrationParameters) this.migrationDescriptor.getMigrationParameters();

        xWikiContext = xWikiContextProvider.get();
        xwiki = xWikiContext.getWiki();

        populizePropertyConversionChecker();

        // STEP 1 : we get two properties (old property and new property could be document meta data)
        try {
            targetClassReference = documentReferenceResolver.resolve(migrationParameters.getTargetClass());

            for (String propertyName : xwiki.getDocument(targetClassReference, xWikiContext).getXClass()
                .getPropertyList()) {
                if (propertyName.equals(migrationParameters.getOldPropertyReference())) {
                    oldPropertyReference = propertyName;
                } else if (propertyName.equals(migrationParameters.getNewPropertyReference())) {
                    newPropertyReference = propertyName;
                }
            }

            oldPropertyType = migrationParameters.getOldPropertyType();
            newPropertyType = migrationParameters.getNewPropertyType();

        } catch (Exception e) {
            throw new MigrationException(String.format("fail to fetch properties"), e);
        }

        // STEP 2 : check compatibility (from anything to String (Compatible))
        if (areTwoPropertiesCompatible(oldPropertyType, newPropertyType)) {
            // STEP 3: start migration
            // TODO : isRemovedOldProperty?
            try {
                Query query =
                    queryManager.createQuery("select distinct doc.fullName " + "from XWikiDocument doc, BaseObject obj "
                        + "where doc.fullName = obj.name and obj.className = :oldClassName", Query.HQL);

                List<String> results = query.bindValue("oldClassName", migrationParameters.getTargetClass()).execute();

                for (String result : results) {
                    migrateProperty(result);
                }
            } catch (QueryException e) {
                throw new MigrationException(
                    String.format("Query fails at the old property: [%s]", oldPropertyReference), e);
            }
        } else {
            return MigrationStatus.FAILURE;
        }

        return MigrationStatus.SUCCESS;

    }

    private void populizePropertyConversionChecker()
    {
        // There are examples of how the conversion check should be initialized
        // In the future, we will support more types, and they can be defined here
        logger.info("Initializing Checkers for property conversion ...");

        PropertyConversionChecker pStringChecker = new PropertyConversionChecker(STRING_TYPE);
        pStringChecker.getAllSupportedProperties().add(STRING_TYPE);

        PropertyConversionChecker pTextAreaChecker = new PropertyConversionChecker(TEXT_AREA_TYPE);
        pTextAreaChecker.getAllSupportedProperties().add(STRING_TYPE);

        PropertyConversionChecker pNumberChecker = new PropertyConversionChecker(NUMBER_TYPE);
        pNumberChecker.getAllSupportedProperties().add(STRING_TYPE);

        PropertyConversionChecker pEMailChecker = new PropertyConversionChecker(EMAIL_TYPE);
        pEMailChecker.getAllSupportedProperties().add(STRING_TYPE);

        provider.getList().add(pStringChecker);
        provider.getList().add(pTextAreaChecker);
        provider.getList().add(pNumberChecker);
        provider.getList().add(pEMailChecker);
    }

    private boolean areTwoPropertiesCompatible(String oldPropertyType, String newPropertyType)
        throws MigrationException
    {
        logger.info("checking compatibility for property conversion ...");

        PropertyConversionChecker localChecker = null;

        // case 1 : same type
        if (oldPropertyType.equals(newPropertyType)) {
            return true;
        }
        // case 2 : different types
        // get the proper checker
        for (PropertyConversionChecker checker : provider.getList()) {
            if (oldPropertyType.equals(checker.getPropertyType())) {
                localChecker = checker;
            }
        }
        // check if conversion between the target type and the resource type is supported
        for (String supportedType : localChecker.getAllSupportedProperties()) {
            if (newPropertyType.equals(supportedType)) {
                return true;
            }
        }

        return false;
    }

    private void migrateProperty(String result) throws MigrationException
    {
        logger.info("Migrating a property from type [{}] to new type [{}].", oldPropertyReference,
            newPropertyReference);
        try {
            XWikiDocument tempDoc = xwiki.getDocument(documentReferenceResolver.resolve(result), xWikiContext);
            List<BaseObject> objects = tempDoc.getXObjects(targetClassReference);

            for (BaseObject o : objects) {
                // an entity in the target document
                // set the new field with the old property class
                if (o != null) {
                    o.set(newPropertyReference, ((BaseProperty) o.safeget(oldPropertyReference)).getValue(),
                        xWikiContext);
                }
            }
            try {
                xwiki.saveDocument(tempDoc, "trial", xWikiContext);
            } catch (XWikiException e) {
                throw new MigrationException(
                    String.format("Failed to save migrated document [%s]", tempDoc.getDocumentReference()), e);
            }
        } catch (XWikiException e) {
            throw new MigrationException(
                String.format("Property Migration fails at the old property: [%s]", oldPropertyReference), e);
        }
    }

}
