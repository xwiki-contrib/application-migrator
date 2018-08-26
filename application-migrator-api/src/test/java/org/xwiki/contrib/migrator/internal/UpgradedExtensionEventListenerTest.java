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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.contrib.migrator.MigrationManager;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.event.ExtensionUpgradedEvent;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UpgradedExtensionEventListener}.
 *
 * @version $Id$
 * @since 1.0
 */
public class UpgradedExtensionEventListenerTest
{
    @Rule
    public final MockitoComponentMockingRule<UpgradedExtensionEventListener> mocker =
            new MockitoComponentMockingRule<>(UpgradedExtensionEventListener.class);

    private MigrationManager migrationManager;

    private InstalledExtension dummyExtension;

    private ExtensionId dummyExtensionId;

    @Before
    public void setUp() throws Exception
    {
        migrationManager = mocker.registerMockComponent(MigrationManager.class);

        dummyExtension = mock(InstalledExtension.class);
        dummyExtensionId = new ExtensionId("dummy-extension", "42");
        when(dummyExtension.getId()).thenReturn(dummyExtensionId);
    }

    @Test
    public void onEventWithRandomEvent() throws Exception
    {
        mocker.getComponentUnderTest().onEvent(new DocumentCreatedEvent(), null, null);
        mocker.getComponentUnderTest().onEvent(new DocumentUpdatedEvent(), null, null);

        verify(migrationManager, never()).applyMigrations(any());
        verify(migrationManager, never()).applyMigrationsForVersion(any());
    }

    @Test
    public void onEventWithCorrectEventButNoMigration() throws Exception
    {
        when(migrationManager.hasAvailableMigrations(dummyExtensionId)).thenReturn(false);

        mocker.getComponentUnderTest().onEvent(new ExtensionUpgradedEvent(), dummyExtension, null);

        verify(migrationManager, times(1)).hasAvailableMigrations(eq(dummyExtensionId));
        verify(migrationManager, never()).applyMigrationsForVersion(any());
        verify(migrationManager, never()).applyMigrations(any());
    }

    @Test
    public void onEventWithCorrectEventAndAvailableMigration() throws Exception
    {
        when(migrationManager.hasAvailableMigrations(dummyExtensionId)).thenReturn(true);

        mocker.getComponentUnderTest().onEvent(new ExtensionUpgradedEvent(), dummyExtension, null);

        verify(migrationManager, times(1)).hasAvailableMigrations(eq(dummyExtensionId));
        verify(migrationManager, times(1)).applyMigrationsForVersion(dummyExtensionId);
        verify(migrationManager, never()).applyMigrations(any());
    }
}
