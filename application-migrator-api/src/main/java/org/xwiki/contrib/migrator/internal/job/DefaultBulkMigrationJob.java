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

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.migrator.AbstractMigrationDescriptor;
import org.xwiki.contrib.migrator.MigrationException;
import org.xwiki.contrib.migrator.job.AbstractBulkMigrationJob;
import org.xwiki.contrib.migrator.job.AbstractBulkMigrationJobRequest;
import org.xwiki.contrib.migrator.job.AbstractBulkMigrationJobStatus;
import org.xwiki.contrib.migrator.job.AbstractMigrationJob;
import org.xwiki.contrib.migrator.job.AbstractMigrationJobRequest;
import org.xwiki.job.Job;
import org.xwiki.job.JobException;
import org.xwiki.job.JobExecutor;
import org.xwiki.job.event.status.JobStatus;

/**
 * This is the default implementation of {@link AbstractBulkMigrationJob}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named(AbstractBulkMigrationJob.JOB_TYPE)
public class DefaultBulkMigrationJob extends AbstractBulkMigrationJob
{
    @Inject
    private JobExecutor jobExecutor;

    @Override
    protected AbstractBulkMigrationJobStatus createNewStatus(AbstractBulkMigrationJobRequest request)
    {
        Job currentJob = this.jobContext.getCurrentJob();
        JobStatus currentJobStatus = currentJob != null ? currentJob.getStatus() : null;
        return new DefaultBulkMigrationJobStatus(AbstractBulkMigrationJob.JOB_TYPE, request, currentJobStatus,
                this.observationManager, this.loggerManager);
    }

    @Override
    protected void runInternal() throws Exception
    {
        status.setMigrationStatuses(new HashMap<>());

        if (!request.getMigrationDescriptors().isEmpty()) {
            for (AbstractMigrationDescriptor migrationDescriptor : request.getMigrationDescriptors()) {
                try {
                    executeMigration(migrationDescriptor);
                } catch (MigrationException e) {
                    logger.error("An exception occurred while running the migration [{}] : ",
                            migrationDescriptor.getMigrationUUID(),
                            ExceptionUtils.getRootCause(e));
                }
            }
        }
    }

    private void executeMigration(AbstractMigrationDescriptor migrationDescriptor) throws MigrationException
    {
        AbstractMigrationJobRequest jobRequest = new DefaultMigrationJobRequest();
        jobRequest.setMigrationDescriptor(migrationDescriptor);

        try {
            AbstractMigrationJob migrationJob =
                    (AbstractMigrationJob) jobExecutor.execute(AbstractMigrationJob.JOB_TYPE, jobRequest);
            migrationJob.join();

            status.getMigrationStatuses().put(migrationDescriptor.getMigrationUUID(),
                    migrationJob.getStatus().getMigrationStatus());

        } catch (JobException e) {
            throw new MigrationException(String.format("Failed to execute the migration job for the migration [%s]",
                    migrationDescriptor.getMigrationUUID()), e);
        } catch (InterruptedException e) {
            throw new MigrationException(String.format(
                    "The migration job for the migration [%s] has been interrupted.",
                    migrationDescriptor.getMigrationUUID()), e);
        }
    }
}
