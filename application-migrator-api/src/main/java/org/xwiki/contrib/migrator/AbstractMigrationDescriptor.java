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
 * @version $Id$
 * @since 1.0
 */
public abstract class AbstractMigrationDescriptor
{
    /**
     * @return the extension ID for which this migration applies to
     */
    public abstract ExtensionId getExtensionId();

    /**
     * @return the name of the migration
     */
    public abstract String getMigrationName();

    /**
     * @return the description of the migration
     */
    public abstract String getMigrationDescription();

    /**
     * @return the UUID of the current migration
     */
    public final UUID getMigrationUUID()
    {
        return UUID.nameUUIDFromBytes(String.format("%s-%s-%s-%s", getExtensionId(),
                getMigrationName(), getMigrationDescription(), hashCode()).getBytes());
    }
}
