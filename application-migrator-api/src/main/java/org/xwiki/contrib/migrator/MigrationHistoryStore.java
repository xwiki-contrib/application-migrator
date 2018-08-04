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
import java.util.UUID;

import org.xwiki.component.annotation.Role;
import org.xwiki.extension.ExtensionId;

/**
 * A migration history store is a component that allows to store the status of the migrations applied to the
 * platform extensions.
 *
 * The goal of the store is only to help defining whether a migration has been executed or not. For each version
 * of an extension, we use the hashCode of a MigrationDescriptor provided by this version in order uniquely
 * identify the migration.
 *
 * @version $Id$
 * @since 1.0
 */
@Role
public interface MigrationHistoryStore
{
    /**
     * Get a set of applied migrations represented by their UUID
     * ({@link AbstractMigrationDescriptor#getMigrationUUID()}).
     *
     * @param extensionId the extension and its version
     * @return a set of applied migrations for the corresponding version of the extension
     * @throws MigrationException if an error happens
     */
    Set<UUID> getAppliedMigrationsForVersion(ExtensionId extensionId) throws MigrationException;
}
