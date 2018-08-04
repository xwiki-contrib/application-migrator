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
package org.xwiki.contrib.migrator.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.migrator.AbstractMigrationDescriptor;
import org.xwiki.contrib.migrator.MigrationException;
import org.xwiki.contrib.migrator.MigrationHistoryStore;
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
 * This is the default implementation of the {@link MigrationHistoryStore}. It uses wiki documents to use store
 * the status of the migrations.
 *
 * Information about the migrations of a specific extension version are stored using the following path:
 * <MIGRATOR_SPACE_NAME>.<STORE_SPACE_NAME>.<EXTENSION_ID>.<VERSION>
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class DefaultMigrationHistoryStore implements MigrationHistoryStore
{
    /**
     * The space name of the migrator application.
     */
    public static final String MIGRATOR_SPACE_NAME = "Migrator";

    /**
     * The sub-space name of store of the migrator app.
     */
    public static final String STORE_SPACE_NAME = "Store";

    /**
     * The sub-space name of the code of the migrator app.
     */
    public static final String CODE_SPACE_NAME = "Code";

    @Inject
    private Provider<XWikiContext> xWikiContextProvider;

    @Inject
    private QueryManager queryManager;

    @Inject
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @Override
    public Set<UUID> getAppliedMigrationsForVersion(ExtensionId extensionId) throws MigrationException
    {
        Set<UUID> resultSet = new HashSet<>();

        if (extensionId.getVersion() != null && extensionId.getId() != null && !extensionId.getId().isEmpty()) {
            try {
                Query query = queryManager.createQuery(
                        "select uuid.value from BaseObject obj, StringProperty uuid "
                                + "where obj.name = :doc and obj.className = :class and uuid.id.id = obj.id "
                                + "and uuid.id.name = :uuid ", Query.HQL);

                query.bindValue("doc", String.format("%s.%s.%s.%s", MIGRATOR_SPACE_NAME, STORE_SPACE_NAME,
                        extensionId.getId(), extensionId.getVersion()));
                query.bindValue("class", MigrationHistoryClassDocumentInitializer.CLASS_REFERENCE);
                query.bindValue("uui", MigrationHistoryClassDocumentInitializer.UUID_PROPERTY);
                List<String> results = query.execute();

                resultSet = results.stream().map(UUID::fromString).collect(Collectors.toSet());

            } catch (QueryException e) {
                throw new MigrationException(
                        "Failed to retrieve the list of applied migrations for the given extension ID", e);
            }
        }

        return resultSet;
    }

    @Override
    public void addAppliedMigration(AbstractMigrationDescriptor migrationDescriptor) throws MigrationException
    {
        XWikiContext xContext = xWikiContextProvider.get();
        XWiki xWiki = xContext.getWiki();
        UUID migrationUUID = migrationDescriptor.getMigrationUUID();

        if (xWiki != null) {
            try {
                // Get the concerned document and create an XObject for holding the new migration UUID
                XWikiDocument document =
                        xWiki.getDocument(buildDocumentReference(migrationDescriptor.getExtensionId()), xContext);
                DocumentReference migrationClassReference =
                        documentReferenceResolver.resolve(MigrationHistoryClassDocumentInitializer.CLASS_REFERENCE);
                int objectNumber = document.createXObject(migrationClassReference, xContext);
                BaseObject migrationObject = document.getXObject(migrationClassReference, objectNumber);

                if (migrationObject != null) {
                    // Actually set the object value and save the document.
                    migrationObject.set(MigrationHistoryClassDocumentInitializer.UUID_PROPERTY,
                            migrationUUID, xContext);
                    xWiki.saveDocument(document, String.format("Add migration [%s].", migrationUUID), xContext);
                } else {
                    throw new MigrationException(String.format("Failed to create the XObject [%s] on document [%s]",
                            objectNumber, document.getDocumentReference()));
                }

            } catch (XWikiException e) {
                throw new MigrationException(String.format("Failed to store the migration [%s]", migrationUUID), e);
            }
        }
    }

    private DocumentReference buildDocumentReference(ExtensionId extensionId)
    {
        return new DocumentReference(
                xWikiContextProvider.get().getWikiId(),
                Arrays.asList(MIGRATOR_SPACE_NAME, STORE_SPACE_NAME, extensionId.getId()),
                extensionId.getVersion().getValue());
    }
}
