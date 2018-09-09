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

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import org.slf4j.Logger;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.migrator.AbstractMigrationDescriptor;
import org.xwiki.contrib.migrator.MigrationDescriptorProvider;
import org.xwiki.contrib.migrator.MigrationHistoryStore;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.job.JobExecutor;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DefaultMigrationManager}.
 *
 * @version $Id$
 * @since 1.0
 */
public class DefaultMigrationManagerTest
{
    @Rule
    public final MockitoComponentMockingRule<DefaultMigrationManager> mocker =
            new MockitoComponentMockingRule<>(DefaultMigrationManager.class);

    private ComponentManager contextComponentManager;

    private Logger logger;

    private JobExecutor jobExecutor;

    private MigrationHistoryStore migrationHistoryStore;

    private MigrationDescriptorProvider migrationDescriptorProvider1;

    private MigrationDescriptorProvider migrationDescriptorProvider2;

    private Extension dummyExtension;

    private ExtensionId dummyExtensionId;

    @Before
    public void setUp() throws Exception
    {
        contextComponentManager = mocker.registerMockComponent(ComponentManager.class, "context");
        logger = mocker.getMockedLogger();
        jobExecutor = mocker.registerMockComponent(JobExecutor.class);

        // By default, the migration history store will always return no migrations, this can be easily
        // overridden if needed to perform some tests
        migrationHistoryStore = mocker.registerMockComponent(MigrationHistoryStore.class);
        when(migrationHistoryStore.getAppliedMigrationsForVersion(any(ExtensionId.class))).thenReturn(
                Collections.EMPTY_SET);

        // We register by default two descriptor providers that can be then mocked to provide dummy migrations
        // for testing purposes.
        migrationDescriptorProvider1 = mock(MigrationDescriptorProvider.class);
        when(migrationDescriptorProvider1.getMigrations(any(ExtensionId.class))).thenReturn(Collections.EMPTY_SET);
        migrationDescriptorProvider2 = mock(MigrationDescriptorProvider.class);
        when(migrationDescriptorProvider2.getMigrations(any(ExtensionId.class))).thenReturn(Collections.EMPTY_SET);

        when(contextComponentManager.getInstanceList(MigrationDescriptorProvider.class)).thenReturn(
                Arrays.asList(migrationDescriptorProvider1, migrationDescriptorProvider2));

        dummyExtension = mock(Extension.class);
        dummyExtensionId = new ExtensionId("dummy-extension", "42");
        when(dummyExtension.getId()).thenReturn(dummyExtensionId);
    }

    @Test
    public void hasAvailableMigrationsWithNoMigrations() throws Exception
    {
        assertFalse(mocker.getComponentUnderTest().hasAvailableMigrations(dummyExtensionId));
    }

    @Test
    public void hasAvailableMigrationsWithMigrations() throws Exception
    {
        Set<AbstractMigrationDescriptor> dummyDescriptors = Sets.newSet(mock(AbstractMigrationDescriptor.class));
        when(migrationDescriptorProvider1.getMigrations(dummyExtensionId))
                .thenReturn(dummyDescriptors);

        assertTrue(mocker.getComponentUnderTest().hasAvailableMigrations(dummyExtensionId));
    }

    @Test
    public void getAvailableMigrationsWithNoMigrations() throws Exception
    {
        assertEquals(Collections.EMPTY_SET, mocker.getComponentUnderTest().getAvailableMigrations(dummyExtensionId));
    }

    @Test
    public void getAvailableMigrationsWithUnappliedMigrations() throws Exception
    {
        Set<AbstractMigrationDescriptor> descriptorsForProvider1 = Sets.newSet(
                mock(AbstractMigrationDescriptor.class),
                mock(AbstractMigrationDescriptor.class),
                mock(AbstractMigrationDescriptor.class)
        );

        Set<AbstractMigrationDescriptor> descriptorsForProvider2 = Sets.newSet(
                mock(AbstractMigrationDescriptor.class),
                mock(AbstractMigrationDescriptor.class)
        );

        when(migrationDescriptorProvider1.getMigrations(dummyExtensionId)).thenReturn(descriptorsForProvider1);
        when(migrationDescriptorProvider2.getMigrations(dummyExtensionId)).thenReturn(descriptorsForProvider2);

        Set<AbstractMigrationDescriptor> resultSet = mocker.getComponentUnderTest()
                .getAvailableMigrations(dummyExtensionId);

        assertEquals(5, resultSet.size());
        assertTrue(resultSet.containsAll(descriptorsForProvider1));
        assertTrue(resultSet.containsAll(descriptorsForProvider2));
    }

    @Test
    public void getAvailableMigrationsWithPartiallyAppliedMigrations() throws Exception
    {
        AbstractMigrationDescriptor appliedDescriptor1 = new FakeMigrationDescriptor(dummyExtensionId, "m1",
                "md1", null);
        AbstractMigrationDescriptor appliedDescriptor2 = new FakeMigrationDescriptor(dummyExtensionId, "m2",
                "md2", null);

        Set<UUID> appliedDescriptors = Sets.newSet(
                appliedDescriptor1.getMigrationUUID(),
                appliedDescriptor2.getMigrationUUID());

        Set<AbstractMigrationDescriptor> descriptorsForProvider1 = Sets.newSet(
                mock(AbstractMigrationDescriptor.class),
                appliedDescriptor1,
                mock(AbstractMigrationDescriptor.class)
        );

        Set<AbstractMigrationDescriptor> descriptorsForProvider2 = Sets.newSet(
                mock(AbstractMigrationDescriptor.class),
                appliedDescriptor2
        );

        when(migrationHistoryStore.getAppliedMigrationsForVersion(dummyExtensionId)).thenReturn(appliedDescriptors);
        when(migrationDescriptorProvider1.getMigrations(dummyExtensionId)).thenReturn(descriptorsForProvider1);
        when(migrationDescriptorProvider2.getMigrations(dummyExtensionId)).thenReturn(descriptorsForProvider2);

        Set<AbstractMigrationDescriptor> resultSet = mocker.getComponentUnderTest()
                .getAvailableMigrations(dummyExtensionId);

        assertEquals(3, resultSet.size());
        assertFalse(resultSet.contains(appliedDescriptor1));
        assertFalse(resultSet.contains(appliedDescriptor2));
    }
}
