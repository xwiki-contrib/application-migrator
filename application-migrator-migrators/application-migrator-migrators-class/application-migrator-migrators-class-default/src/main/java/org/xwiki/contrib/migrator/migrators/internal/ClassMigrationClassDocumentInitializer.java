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
import com.xpn.xwiki.objects.classes.TextAreaClass;

/**
 * This XClass defines a migration of XClasses for an extension.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
@Named("Migrator.Migrators.ClassMigrationClass")
public class ClassMigrationClassDocumentInitializer extends AbstractMigrationClassDocumentInitializer
{
    /**
     * The name of the XClass.
     */
    public static final String CLASS_NAME = "ClassMigrationClass";

    /**
     * The XClass reference.
     */
    public static final String CLASS_REFERENCE = String.format("Migrator.Migrators.%s", CLASS_NAME);

    /**
     * Whether the migration should be done in place.
     */
    public static final String IN_PLACE_PROPERTY = "inPlace";

    /**
     * The reference of the old class to target.
     */
    public static final String OLD_CLASS_PROPERTY = "oldClass";

    /**
     * The reference of the new class to target.
     */
    public static final String NEW_CLASS_PROPERTY = "newClass";

    /**
     * Should the XObjects using the old XClass be removed from the documents?
     */
    public static final String REMOVE_OLD_XOBJECT_PROPERTY = "removeOldXObjects";

    /**
     * Should the old XClass be removed from the documents?
     */
    public static final String REMOVE_OLD_XCLASS_PROPERTY = "removeOldXClass";

    /**
     * The properties mapping.
     */
    public static final String PROPERTIES_MAPPING_PROPERTY = "propertiesMapping";

    private static final List<String> SPACE_PATH = Arrays.asList("Migrator", "Migrators");

    private static final String CHECKBOX_DISPLAY = "checkbox";

    /**
     * Builds a new {@link ClassMigrationClassDocumentInitializer}.
     */
    public ClassMigrationClassDocumentInitializer()
    {
        super(new LocalDocumentReference(SPACE_PATH, CLASS_NAME));
    }

    @Override
    protected void appendClassProperties(BaseClass xclass)
    {
        xclass.addBooleanField(IN_PLACE_PROPERTY, "Is the migration on the same XClass ?", CHECKBOX_DISPLAY);

        xclass.addTextField(OLD_CLASS_PROPERTY, "Old XClass reference", 128);
        xclass.addTextField(NEW_CLASS_PROPERTY, "New XClass reference", 128);

        xclass.addBooleanField(REMOVE_OLD_XCLASS_PROPERTY, "Remove old XClass?", CHECKBOX_DISPLAY);
        xclass.addBooleanField(REMOVE_OLD_XOBJECT_PROPERTY, "Remove old XObjects?", CHECKBOX_DISPLAY);

        xclass.addTextAreaField(PROPERTIES_MAPPING_PROPERTY, "Properties mapping", 120, 20,
                TextAreaClass.EditorType.PURE_TEXT);
    }
}
