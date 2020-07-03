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
package org.xwiki.contrib.migrator.migrators.internal;

import org.xwiki.contrib.migrator.MigrationParameters;

/**
 * Define the generic parameters that can be used for property migration descriptors.
 * 
 * @version $Id$
 * @since 1.1
 */
public class PropertyMigrationParameters implements MigrationParameters<PropertyMigrationType>
{
    private String targetClass;

    private String propertyName;

    private String oldProperty;

    private String newProperty;

    private boolean removeOldProperty;

    /**
     * Constructs a new {@link PropertyMigrationParameters}.
     *
     * @param targetClass the reference to the Class (where oldProperty and new Property sits together).
     * @param propertyName the target of the modified property.
     * @param oldProperty the reference to the old XClass.
     * @param newProperty the reference to the new XClass.
     * @param removeOldProperty should the old property be removed?
     */
    public PropertyMigrationParameters(String targetClass, String propertyName, String oldProperty, String newProperty,
        boolean removeOldProperty)
    {
        this.targetClass = targetClass;
        this.propertyName = propertyName;
        this.oldProperty = oldProperty;
        this.newProperty = newProperty;
        this.removeOldProperty = removeOldProperty;
    }

    /**
     * @return the reference to the XClass containing properties that will be dealt with during the migration.
     */
    public String getTargetClass()
    {
        return this.targetClass;
    }

    /**
     * @return the reference to the old property
     */
    public String getPropertyName()
    {
        return this.propertyName;
    }

    /**
     * @return the reference to the old property
     */
    public String getOldProperty()
    {
        return this.oldProperty;
    }

    /**
     * @return the reference to the new property
     */
    public String getNewProperty()
    {
        return this.newProperty;
    }

    /**
     * @return whether the old property should be deleted or not
     */
    public boolean isRemoveOldProperty()
    {
        return this.removeOldProperty;
    }
}
