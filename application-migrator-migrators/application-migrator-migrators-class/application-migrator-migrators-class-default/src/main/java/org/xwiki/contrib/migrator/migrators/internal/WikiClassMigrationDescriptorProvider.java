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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.migrator.AbstractMigrationDescriptor;
import org.xwiki.contrib.migrator.MigrationDescriptorProvider;
import org.xwiki.contrib.migrator.MigrationException;
import org.xwiki.extension.ExtensionId;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.objects.BaseObject;

/**
 * Provides {@link ClassMigrationDescriptor} extracted from wiki pages through the XClass defined in
 * {@link ClassMigrationClassDocumentInitializer}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named(WikiClassMigrationDescriptorProvider.COMPONENT_NAME)
@Singleton
public class WikiClassMigrationDescriptorProvider implements MigrationDescriptorProvider
{
    /**
     * The name of the component used as a hint against the ComponentManager.
     */
    public static final String COMPONENT_NAME = "WikiClassMigrationDescriptorProvider";

    private static final String CLASS_LITERAL = "class";

    @Inject
    private QueryManager queryManager;

    @Override
    public Set<AbstractMigrationDescriptor> getMigrations(ExtensionId extensionId) throws MigrationException
    {
        Set<AbstractMigrationDescriptor> resultSet = new HashSet<>();

        try {
            Query query = queryManager.createQuery(
                    "select obj from BaseObject obj "
                            + "where obj.name in :documents and obj.className = :class", Query.HQL);
            query.bindValue("documents", getDocumentList());
            query.bindValue(CLASS_LITERAL, ClassMigrationClassDocumentInitializer.CLASS_NAME);
            List<BaseObject> results = query.execute();

            for (BaseObject result : results) {
                resultSet.add(createFromBaseObject(result));
            }
        } catch (QueryException e) {
            throw new MigrationException("Failed to retrieve a list of class migration XObjects.", e);
        }

        return resultSet;
    }

    private ClassMigrationDescriptor createFromBaseObject(BaseObject object)
    {
        ExtensionId extensionId = new ExtensionId(
                object.getStringValue(ClassMigrationClassDocumentInitializer.EXTENSION_ID_PROPERTY),
                object.getStringValue(ClassMigrationClassDocumentInitializer.EXTENSION_VERSION_PROPERTY));


        ClassMigrationParameters migrationParameters = new ClassMigrationParameters(
                object.getStringValue(ClassMigrationClassDocumentInitializer.OLD_CLASS_PROPERTY),
                object.getStringValue(ClassMigrationClassDocumentInitializer.NEW_CLASS_PROPERTY),
                (object.getIntValue(ClassMigrationClassDocumentInitializer.REMOVE_OLD_XCLASS_PROPERTY, 0) == 1),
                (object.getIntValue(ClassMigrationClassDocumentInitializer.REMOVE_OLD_XOBJECT_PROPERTY, 0) == 1),
                extractMapping(
                        object.getStringValue(ClassMigrationClassDocumentInitializer.PROPERTIES_MAPPING_PROPERTY)));

        return new ClassMigrationDescriptor(
                extensionId,
                object.getStringValue(ClassMigrationClassDocumentInitializer.MIGRATION_NAME_PROPERTY),
                object.getStringValue(ClassMigrationClassDocumentInitializer.MIGRATION_DESCRIPTION_PROPERTY),
                migrationParameters);
    }

    private Map<String, String> extractMapping(String stringMapping)
    {
        Map<String, String> resultMapping = new HashMap<>();

        String[] mappingEntries = stringMapping.split("\n");
        for (String entry : mappingEntries) {
            String[] splitEntry = entry.split("=");

            if (splitEntry.length == 2) {
                resultMapping.put(splitEntry[0], splitEntry[1]);
            }
        }

        return resultMapping;
    }

    /**
     * @return a list of documents containing XObjects of the ClassMigrationClass XClass
     */
    private List<String> getDocumentList() throws MigrationException
    {
        try {
            Query query = queryManager.createQuery(
                    "select distinct doc.fullName "
                            + "from XWikiDocument doc, BaseObject obj "
                            + "where obj.name = doc.fullName and obj.className = :class", Query.HQL);
            query.bindValue(CLASS_LITERAL, ClassMigrationClassDocumentInitializer.CLASS_REFERENCE);
            return query.execute();
        } catch (QueryException e) {
            throw new MigrationException("Failed to retrieve a list of documents containing class migrations.", e);
        }
    }
}
