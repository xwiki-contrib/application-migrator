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
package org.xwiki.contrib.migrator.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.migrator.AbstractMigrationDescriptor;
import org.xwiki.contrib.migrator.MigrationDescriptorProvider;
import org.xwiki.contrib.migrator.MigrationException;
import org.xwiki.contrib.migrator.MigrationHistoryStore;
import org.xwiki.contrib.migrator.MigrationManager;
import org.xwiki.contrib.migrator.internal.job.DefaultBulkMigrationJobRequest;
import org.xwiki.contrib.migrator.internal.job.DefaultMigrationJobRequest;
import org.xwiki.contrib.migrator.job.AbstractBulkMigrationJob;
import org.xwiki.contrib.migrator.job.AbstractBulkMigrationJobRequest;
import org.xwiki.contrib.migrator.job.AbstractBulkMigrationJobStatus;
import org.xwiki.contrib.migrator.job.AbstractMigrationJob;
import org.xwiki.contrib.migrator.job.AbstractMigrationJobRequest;
import org.xwiki.contrib.migrator.job.AbstractMigrationJobStatus;
import org.xwiki.extension.ExtensionId;
import org.xwiki.job.JobException;
import org.xwiki.job.JobExecutor;

/**
 * This is the default implementation for {@link MigrationManager}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class DefaultMigrationManager implements MigrationManager
{
    @Inject
    @Named("context")
    private ComponentManager componentManager;

    @Inject
    private Logger logger;

    @Inject
    private JobExecutor jobExecutor;

    @Inject
    private Provider<MigrationHistoryStore> migrationHistoryStoreProvider;

    @Override
    public boolean hasAvailableMigrations(ExtensionId extensionId) throws MigrationException
    {
        return (getAvailableMigrations(extensionId).size() != 0);
    }

    @Override
    public Set<AbstractMigrationDescriptor> getAvailableMigrations(ExtensionId extensionId) throws MigrationException
    {
        // Get through every provider available in the component manager and load their migrations
        Set<AbstractMigrationDescriptor> availableMigrations = new HashSet<>();
        try {
            for (Object providerObject
                    : componentManager.getInstanceList(MigrationDescriptorProvider.class)) {
                MigrationDescriptorProvider provider = (MigrationDescriptorProvider) providerObject;

                availableMigrations.addAll(provider.getMigrations(extensionId));
            }
        } catch (ComponentLookupException e) {
            logger.error("Failed to retrieve a list of available migration descriptor providers: {}", e);
        }

        // Remove every migration that has already been applied according to the migration history store
        Set<String> appliedMigrations = migrationHistoryStoreProvider.get().getAppliedMigrationsForVersion(extensionId);
        availableMigrations = availableMigrations.stream()
                .filter(x -> !appliedMigrations.contains(x.getMigrationUUID())).collect(Collectors.toSet());

        return availableMigrations;
    }

    @Override
    public AbstractMigrationJobStatus applyMigration(AbstractMigrationDescriptor migrationDescriptor)
            throws MigrationException
    {
        AbstractMigrationJobRequest jobRequest = new DefaultMigrationJobRequest();
        jobRequest.setMigrationDescriptor(migrationDescriptor);

        try {
            return ((AbstractMigrationJob) jobExecutor.execute(AbstractMigrationJob.JOB_TYPE, jobRequest)).getStatus();
        } catch (JobException e) {
            throw new MigrationException("Failed to start a migration job.", e);
        }
    }

    @Override
    public AbstractBulkMigrationJobStatus applyMigrations(Set<AbstractMigrationDescriptor> migrationDescriptors)
            throws MigrationException
    {
        AbstractBulkMigrationJobRequest jobRequest = new DefaultBulkMigrationJobRequest();
        jobRequest.setMigrationDescriptors(migrationDescriptors);

        try {
            return ((AbstractBulkMigrationJob) jobExecutor.execute(AbstractBulkMigrationJob.JOB_TYPE, jobRequest))
                    .getStatus();
        } catch (JobException e) {
            throw new MigrationException("Failed to start a bulk migration job.", e);
        }
    }

    @Override
    public AbstractBulkMigrationJobStatus applyMigrationsForVersion(ExtensionId extensionId) throws MigrationException
    {
        return applyMigrations(getAvailableMigrations(extensionId));
    }
}
