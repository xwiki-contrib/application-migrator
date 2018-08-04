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
 * Define a migration.
 *
 * A migration should be related to a specific extension and extension version, it describes the migration itself
 * (actions taken, when should the migration occur, …).
 *
 * We enforce {@link #equals(Object)} and {@link #hashCode()} to be overridden so that we can compare
 * MigrationDescriptors between each others. We don't want to run the same migration twice by mistake.
 *
 * @version $Id$
 * @since 1.0
 */
public abstract class AbstractMigrationDescriptor
{
    /**
     * {@inheritDoc}
     */
    public abstract boolean equals(Object other);

    /**
     * {@inheritDoc}
     */
    public abstract int hashCode();
}
