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
package org.xwiki.contrib.migrator;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionId;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

/**
 * Define a script service for managing migrations.
 *
 * @version $Id$
 * @since 1.0
 */
@Singleton
@Component
@Named("migration")
@Unstable
public class MigrationScriptService implements ScriptService
{
    @Inject
    private MigrationManager migrationManager;

    @Inject
    private MigrationHistoryStore migrationHistoryStore;

    /**
     * @return the {@link MigrationManager}
     */
    public MigrationManager getManager()
    {
        return migrationManager;
    }

    /**
     * @return the {@link MigrationHistoryStore}
     */
    public MigrationHistoryStore getStore()
    {
        return migrationHistoryStore;
    }

    /**
     * Helper for {@link MigrationManager#hasAvailableMigrations(ExtensionId)} allowing to get migrations without
     * actually needing to instantiate a new {@link ExtensionId}.
     *
     * @param extensionId the ID of the extension to check
     * @param extensionVersion the version of the extension to check
     * @return true if the extension has at least one migration available
     * @throws MigrationException if an error happens
     */
    public boolean hasAvailableMigrations(String extensionId, String extensionVersion) throws MigrationException
    {
        return migrationManager.hasAvailableMigrations(new ExtensionId(extensionId, extensionVersion));
    }

    /**
     * Helper for {@link MigrationManager#getAvailableMigrations(ExtensionId)} allowing to get migrations without
     * actually needing to instantiate a new {@link ExtensionId}.
     *
     * @param extensionId the ID of the extension to check
     * @param extensionVersion the version of the extension to check
     * @return the available migrations for the given extension
     * @throws MigrationException if an error happens
     */
    public Set<AbstractMigrationDescriptor> getAvailableMigrations(String extensionId, String extensionVersion)
        throws MigrationException
    {
        return migrationManager.getAvailableMigrations(new ExtensionId(extensionId, extensionVersion));
    }
}
