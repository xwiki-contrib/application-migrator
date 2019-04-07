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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.migrator.AbstractMigrationDescriptor;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

/**
 * Tests for {@link DefaultMigrationDependencyManager}.
 *
 * @version $Id$
 * @since 1.0
 */
public class DefaultMigrationDependecyManagerTest
{
	@Rule
    public final MockitoComponentMockingRule<DefaultMigrationDependencyManager> mocker =
            new MockitoComponentMockingRule<>(DefaultMigrationDependencyManager.class);

    private ComponentManager contextComponentManager;

    private Logger logger;

    private Extension dummyExtension;

    private ExtensionId dummyExtensionId;
	
    @Before
    public void setUp() throws Exception
    {
        dummyExtension = mock(Extension.class);
        dummyExtensionId = new ExtensionId("dummy-extension", "42");
        when(dummyExtension.getId()).thenReturn(dummyExtensionId);
    }
    
    @Test
    public void getDependencyOrderWithNoMigration() throws Exception
    {
    	assertEquals(Collections.emptyList(), mocker.getComponentUnderTest().getDependecyOrder());
    }
    
    @Test
    public void getDependencyOrderWithProperDependencies() throws Exception
    {
    	AbstractMigrationDescriptor migrationDescriptor1 = new FakeMigrationDescriptor(dummyExtensionId, "m1",
                "md1", null);
        AbstractMigrationDescriptor migrationDescriptor2 = new FakeMigrationDescriptor(dummyExtensionId, "m2",
                "md2", null);
        AbstractMigrationDescriptor migrationDescriptor3 = new FakeMigrationDescriptor(dummyExtensionId, "m3",
                "md3", null);
        AbstractMigrationDescriptor migrationDescriptor4 = new FakeMigrationDescriptor(dummyExtensionId, "m4",
                "md4", null);
    	mocker.getComponentUnderTest().createMigrationDependency(migrationDescriptor1, migrationDescriptor2);
    	mocker.getComponentUnderTest().createMigrationDependency(migrationDescriptor1, migrationDescriptor3);
    	mocker.getComponentUnderTest().createMigrationDependency(migrationDescriptor2, migrationDescriptor3);
    	mocker.getComponentUnderTest().createMigrationDependency(migrationDescriptor2, migrationDescriptor4);
    	mocker.getComponentUnderTest().createMigrationDependency(migrationDescriptor3, migrationDescriptor4);
    	LinkedList<AbstractMigrationDescriptor> dependencyList = (LinkedList) mocker.getComponentUnderTest().getDependecyOrder();
    	
    	assertEquals(4, dependencyList.size());
    	assertEquals(migrationDescriptor1, dependencyList.poll());
    	assertEquals(migrationDescriptor2, dependencyList.poll());
    	assertEquals(migrationDescriptor3, dependencyList.poll());
    	assertEquals(migrationDescriptor4, dependencyList.poll());
    }
    
    @Test
    public void getDependencyOrderWithCycleDependencies() throws Exception
    {
    	AbstractMigrationDescriptor migrationDescriptor1 = new FakeMigrationDescriptor(dummyExtensionId, "m1",
                "md1", null);
        AbstractMigrationDescriptor migrationDescriptor2 = new FakeMigrationDescriptor(dummyExtensionId, "m2",
                "md2", null);
        AbstractMigrationDescriptor migrationDescriptor3 = new FakeMigrationDescriptor(dummyExtensionId, "m3",
                "md3", null);
    	mocker.getComponentUnderTest().createMigrationDependency(migrationDescriptor1, migrationDescriptor2);
    	mocker.getComponentUnderTest().createMigrationDependency(migrationDescriptor2, migrationDescriptor3);
    	mocker.getComponentUnderTest().createMigrationDependency(migrationDescriptor3, migrationDescriptor1);
    	List<AbstractMigrationDescriptor> dependencyList = mocker.getComponentUnderTest().getDependecyOrder();
    	
    	assertEquals(0, dependencyList.size());
    }
}
