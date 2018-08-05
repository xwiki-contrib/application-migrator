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

import java.util.UUID;

import org.xwiki.extension.ExtensionId;

/**
 * Define a migration.
 *
 * A migration should be related to a specific extension and extension version, it describes the migration itself
 * (actions taken, when should the migration occur, …).
 *
 * @param <T> the type of migration that this descriptor describes
 * @version $Id$
 * @since 1.0
 */
public abstract class AbstractMigrationDescriptor<T extends MigrationType>
{
    private ExtensionId extensionId;

    private String migrationName;

    private String migrationDescription;

    private MigrationParameters<? extends T> migrationParameters;

    /**
     * Build a new migration descriptors with its default attributes.
     *
     * @param extensionId the extension to which this migration applies
     * @param migrationName the name of the migration
     * @param migrationDescription a description of the migration
     * @param migrationParameters the migration parameters
     */
    public AbstractMigrationDescriptor(ExtensionId extensionId, String migrationName, String migrationDescription,
            MigrationParameters<? extends T> migrationParameters)
    {
        this.extensionId = extensionId;
        this.migrationName = migrationName;
        this.migrationDescription = migrationDescription;
        this.migrationParameters = migrationParameters;
    }

    /**
     * @return the extension ID for which this migration applies to
     */
    public ExtensionId getExtensionId()
    {
        return extensionId;
    }

    /**
     * @return the name of the migration
     */
    public String getMigrationName()
    {
        return migrationName;
    }

    /**
     * @return the description of the migration
     */
    public String getMigrationDescription()
    {
        return migrationDescription;
    }

    /**
     * @return the parameters that will be used in this migration
     */
    public MigrationParameters<? extends T> getMigrationParameters()
    {
        return migrationParameters;
    }

    /**
     * @return the UUID of the current migration
     */
    public final UUID getMigrationUUID()
    {
        return UUID.nameUUIDFromBytes(String.format("%s-%s-%s-%s", getExtensionId(),
                getMigrationName(), getMigrationDescription(), hashCode()).getBytes());
    }
}
