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

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.migrator.migrators.AbstractMigrationClassDocumentInitializer;
import org.xwiki.model.reference.LocalDocumentReference;

import com.xpn.xwiki.objects.classes.BaseClass;

/**
 * This XClass defines a migration of Properties for an extension.
 *
 * @version $Id$
 * @since 1.1
 */
@Component
@Singleton
@Named("Migrator.Migrators.PropertyMigrationClass")
public class PropertyMigrationClassDocumentInitializer extends AbstractMigrationClassDocumentInitializer
{
    /**
     * The name of the XClass.
     */
    public static final String CLASS_NAME = "PropertyMigrationClass";

    /**
     * The XClass reference.
     */
    public static final String CLASS_REFERENCE = String.format("Migrator.Migrators.%s", CLASS_NAME);

    /**
     * The reference of the class to target.
     */
    public static final String CLASS_REFERENCE_PROPERTY = "classReference";

    /**
     * The reference of the old property.
     */
    public static final String OLD_PROPERTY_REFERENCE = "oldPropertyReference";
    
    /**
     * The type of the old property.
     */
    public static final String OLD_PROPERTY_TYPE = "oldPropertyType";

    /**
     * The type of the new property.
     */
    public static final String NEW_PROPERTY_TYPE = "newPropertyType";
    
    /**
     * The reference of the new property.
     */
    public static final String NEW_PROPERTY_REFERENCE = "newPropertyReference";

    /**
     * Should the targeted property be deleted?
     */
    public static final String DELETE_PROPERTY_PROPERTY = "deleteProperty";

    private static final List<String> SPACE_PATH = Arrays.asList("Migrator", "Migrators");

    private static final String CHECKBOX_DISPLAY = "checkbox";

    /**
     * Builds a new {@link PropertyMigrationClassDocumentInitializer}.
     */
    public PropertyMigrationClassDocumentInitializer()
    {
        super(new LocalDocumentReference(SPACE_PATH, CLASS_NAME));
    }

    @Override
    protected void appendClassProperties(BaseClass xclass)
    {
        xclass.addTextField(CLASS_REFERENCE_PROPERTY, "XClass reference", 128);
        xclass.addTextField(OLD_PROPERTY_REFERENCE, "Old property reference", 128);
        xclass.addTextField(OLD_PROPERTY_TYPE, "Old property type", 128);
        xclass.addTextField(NEW_PROPERTY_REFERENCE, "New property reference", 128);
        xclass.addTextField(NEW_PROPERTY_TYPE, "New property type", 128);

        xclass.addBooleanField(DELETE_PROPERTY_PROPERTY, "Remove old Property?", CHECKBOX_DISPLAY);
    }
}
