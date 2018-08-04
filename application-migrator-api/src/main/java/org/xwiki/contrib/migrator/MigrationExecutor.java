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

/**
 * Define a migration executor.
 *
 * A migration executor is called for actually executing the currently running migration. It will handle
 * the extraction of the information needed to perform the migration from the {@link AbstractMigrationDescriptor}.
 *
 * @param <D> the type of migration descriptor handled
 * @version $Id$
 * @since 1.0
 */
public interface MigrationExecutor<D extends AbstractMigrationDescriptor>
{
    /**
     * Run the actual migration.
     *
     * @param migrationDescriptor the migration descriptor
     * @throws MigrationException if an error happens
     * @return the status of the current migration
     */
    MigrationStatus execute(D migrationDescriptor) throws MigrationException;
}
