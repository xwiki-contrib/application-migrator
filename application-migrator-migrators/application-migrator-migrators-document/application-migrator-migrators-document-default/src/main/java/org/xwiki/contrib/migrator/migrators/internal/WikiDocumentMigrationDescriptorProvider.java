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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.migrator.AbstractMigrationDescriptor;
import org.xwiki.contrib.migrator.MigrationDescriptorProvider;
import org.xwiki.contrib.migrator.MigrationException;
import org.xwiki.extension.ExtensionId;
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

/**
 * Provides {@link DocumentMigrationDescriptor} extracted from wiki pages through the XClass defined in
 * {@link DocumentMigrationClassDocumentInitializer}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named(WikiDocumentMigrationDescriptorProvider.COMPONENT_NAME)
@Singleton
public class WikiDocumentMigrationDescriptorProvider implements MigrationDescriptorProvider
{
    /**
     * The name of the component used as a hint against the ComponentManager.
     */
    public static final String COMPONENT_NAME = "WikiDocumentMigrationDescriptorProvider";

    private static final String CLASS_LITERAL = "class";

    @Inject
    private QueryManager queryManager;

    @Inject
    private DocumentReferenceResolver<String> stringDocumentReferenceResolver;

    @Inject
    private Provider<XWikiContext> xWikiContextProvider;

    @Override
    public Set<AbstractMigrationDescriptor> getMigrations(ExtensionId extensionId) throws MigrationException
    {
        Set<AbstractMigrationDescriptor> resultSet = new HashSet<>();

        Set<DocumentReference> documentSet = getDocumentSet();

        if (documentSet.size() != 0) {
            XWikiContext context = xWikiContextProvider.get();
            XWiki xwiki = context.getWiki();

            DocumentReference classReference =
                    stringDocumentReferenceResolver.resolve(DocumentMigrationClassDocumentInitializer.CLASS_REFERENCE);

            try {
                for (DocumentReference documentReference : documentSet) {
                    XWikiDocument document = xwiki.getDocument(documentReference, context);

                    for (BaseObject object : document.getXObjects(classReference)) {
                        resultSet.add(createFromBaseObject(object));
                    }
                }
            } catch (XWikiException e) {
                throw new MigrationException("Failed to retrieve a list of document migration XObjects.", e);
            }

        }

        return resultSet;
    }

    private DocumentMigrationDescriptor createFromBaseObject(BaseObject object)
    {
        ExtensionId extensionId = new ExtensionId(
                object.getStringValue(DocumentMigrationClassDocumentInitializer.EXTENSION_ID_PROPERTY),
                object.getStringValue(DocumentMigrationClassDocumentInitializer.EXTENSION_VERSION_PROPERTY));


        DocumentMigrationParameters migrationParameters = new DocumentMigrationParameters(
                stringDocumentReferenceResolver.resolve(
                        object.getStringValue(DocumentMigrationClassDocumentInitializer.DOCUMENT_REFERENCE_PROPERTY)),
                (object.getIntValue(DocumentMigrationClassDocumentInitializer.DELETE_DOCUMENT_PROPERTY, 0) == 1));

        return new DocumentMigrationDescriptor(
                extensionId,
                object.getStringValue(DocumentMigrationClassDocumentInitializer.MIGRATION_NAME_PROPERTY),
                object.getStringValue(DocumentMigrationClassDocumentInitializer.MIGRATION_DESCRIPTION_PROPERTY),
                migrationParameters);
    }

    /**
     * @return a set of documents containing XObjects of the DocumentMigrationClass XClass
     */
    private Set<DocumentReference> getDocumentSet() throws MigrationException
    {
        try {
            Query query = queryManager.createQuery(
                    "select distinct doc.fullName "
                            + "from XWikiDocument doc, BaseObject obj "
                            + "where obj.name = doc.fullName and obj.className = :class", Query.HQL);
            query.bindValue(CLASS_LITERAL, DocumentMigrationClassDocumentInitializer.CLASS_REFERENCE);

            List<String> results = query.execute();
            Set<DocumentReference> documentSet = new HashSet<>();

            for (String result : results) {
                documentSet.add(stringDocumentReferenceResolver.resolve(result));
            }

            return documentSet;
        } catch (QueryException e) {
            throw new MigrationException("Failed to retrieve a list of documents containing document migrations.", e);
        }
    }
}
