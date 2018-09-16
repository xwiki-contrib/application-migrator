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

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.contrib.migrator.MigrationException;
import org.xwiki.contrib.migrator.MigrationExecutor;
import org.xwiki.contrib.migrator.MigrationStatus;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

/**
 * Executes migrations of type {@link DocumentMigrationDescriptor}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DocumentMigrationExecutor implements MigrationExecutor<DocumentMigrationDescriptor>
{
    @Inject
    private Provider<XWikiContext> xWikiContextProvider;

    @Inject
    private Logger logger;

    // As we're instantiating the executor per lookup (and not as a singleton), we can have the freedom to
    // store private attributes without them interfering with other executions.

    private XWikiContext xWikiContext;

    private XWiki xwiki;

    private DocumentMigrationDescriptor migrationDescriptor;

    private DocumentMigrationParameters migrationParameters;

    @Override
    public MigrationStatus execute(DocumentMigrationDescriptor migrationDescriptor) throws MigrationException
    {
        // Initialize the essential attributes of our executor
        this.migrationDescriptor = migrationDescriptor;
        migrationParameters = (DocumentMigrationParameters) migrationDescriptor.getMigrationParameters();

        xWikiContext = xWikiContextProvider.get();
        xwiki = xWikiContext.getWiki();

        // If needed, remove the document
        if (migrationParameters.isDeleteDocument()
                && xwiki.exists(migrationParameters.getDocumentReference(), xWikiContext)) {
            logger.info("Removing document [{}] ...", migrationParameters.getDocumentReference());
            try {
                xwiki.deleteDocument(
                        xwiki.getDocument(
                                migrationParameters.getDocumentReference(), xWikiContext), false, xWikiContext);
            } catch (XWikiException e) {
                throw new MigrationException(
                        String.format("Failed to remove the old XClass [%s]",
                                migrationParameters.getDocumentReference()));
            }
        }

        return MigrationStatus.SUCCESS;
    }
}
