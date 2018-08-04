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

import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.contrib.migrator.MigrationException;
import org.xwiki.contrib.migrator.MigrationExecutor;
import org.xwiki.contrib.migrator.job.AbstractMigrationJob;

/**
 * This is the default implementation of {@link AbstractMigrationJob}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named(AbstractMigrationJob.JOB_TYPE)
public class DefaultMigrationJob extends AbstractMigrationJob
{
    @Override
    protected void runInternal() throws Exception
    {
        // Fetch the executor that could be used for the migration
        try {
            MigrationExecutor executor = componentManager.getInstance(
                    new DefaultParameterizedType(MigrationExecutor.class, MigrationExecutor.class,
                                    request.getMigrationDescriptor().getClass()));

            status.setMigrationStatus(executor.execute(request.getMigrationDescriptor()));
        } catch (ComponentLookupException e) {
            throw new MigrationException(String.format(
                    "Failed to retrieve a MigrationExecutor for the descriptor type [%s]",
                    request.getMigrationDescriptor().getClass()), e);
        }
    }
}
