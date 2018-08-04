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
package org.xwiki.contrib.migrator.internal.job;

import java.util.Set;

import org.xwiki.contrib.migrator.AbstractMigrationDescriptor;
import org.xwiki.contrib.migrator.job.AbstractBulkMigrationJobRequest;
import org.xwiki.contrib.migrator.job.AbstractMigrationJobRequest;

/**
 * This is the default implementation of {@link AbstractMigrationJobRequest}.
 *
 * @version $Id$
 * @since 1.0
 */
public class DefaultBulkMigrationJobRequest extends AbstractBulkMigrationJobRequest
{
    private Set<AbstractMigrationDescriptor> migrationDescriptors;

    @Override
    public void setMigrationDescriptors(Set<AbstractMigrationDescriptor> migrationDescriptors)
    {
        this.migrationDescriptors = migrationDescriptors;
    }

    @Override
    public Set<AbstractMigrationDescriptor> getMigrationDescriptors()
    {
        return migrationDescriptors;
    }
}
