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
package org.xwiki.contrib.migrator.migrators;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

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
 * This abstract is a helper for defining {@link MigrationDescriptorProvider} that are based on XObjects.
 * It provides the default methods used to search in the wiki for XObjects capable to implement such providers
 * and once those XObjects are extracted, implementation-specific methods are being called in order to instantiate
 * the actual MigrationDescriptor.
 *
 * @version $Id$
 * @since 1.1
 */
public abstract class AbstractWikiMigrationDescriptorProvider implements MigrationDescriptorProvider
{
    private static final String CLASS_LITERAL = "class";

    @Inject
    protected QueryManager queryManager;

    @Inject
    protected DocumentReferenceResolver<String> stringDocumentReferenceResolver;

    @Inject
    protected Provider<XWikiContext> xWikiContextProvider;

    @Override
    public Set<AbstractMigrationDescriptor> getMigrations(ExtensionId extensionId) throws MigrationException
    {
        Set<AbstractMigrationDescriptor> resultSet = new HashSet<>();

        Set<DocumentReference> documentSet = getDocumentSet();

        if (documentSet.size() != 0) {
            XWikiContext context = xWikiContextProvider.get();
            XWiki xwiki = context.getWiki();

            DocumentReference classReference =
                    stringDocumentReferenceResolver.resolve(getXClassReferenceAsString());

            try {
                for (DocumentReference documentReference : documentSet) {
                    XWikiDocument document = xwiki.getDocument(documentReference, context);

                    for (BaseObject object : document.getXObjects(classReference)) {
                        AbstractMigrationDescriptor abstractMigrationDescriptor = createFromBaseObject(object);
                        if (extensionId.getId().equals(abstractMigrationDescriptor.getExtensionId().getId())) {
                            resultSet.add(abstractMigrationDescriptor);
                        }
                    }
                }
            } catch (XWikiException e) {
                throw new MigrationException(
                        String.format("Failed to retrieve a list of [%s] migration XObjects.",
                                getXClassReferenceAsString()), e);
            }

        }

        return resultSet;
    }

    /**
     * @return a set of documents containing XObjects of the searched XClass
     */
    protected Set<DocumentReference> getDocumentSet() throws MigrationException
    {
        try {
            Query query = queryManager.createQuery(
                    "select distinct doc.fullName "
                            + "from XWikiDocument doc, BaseObject obj "
                            + "where obj.name = doc.fullName and obj.className = :class", Query.HQL);
            query.bindValue(CLASS_LITERAL, getXClassReferenceAsString());

            List<String> results = query.execute();
            Set<DocumentReference> documentSet = new HashSet<>();

            for (String result : results) {
                documentSet.add(stringDocumentReferenceResolver.resolve(result));
            }

            return documentSet;
        } catch (QueryException e) {
            throw new MigrationException(
                    String.format("Failed to retrieve a list of documents containing [%s] XObjects.",
                            getXClassReferenceAsString()), e);
        }
    }

    /**
     * From the given {@link BaseObject}, create the corresponding MigrationDescriptor.
     *
     * @param object the {@link BaseObject} retrieved
     * @return the result MigrationDescriptor
     */
    protected abstract AbstractMigrationDescriptor createFromBaseObject(BaseObject object);

    /**
     * @return the reference to the XClass that needs to be searched as a string
     */
    protected abstract String getXClassReferenceAsString();
}
