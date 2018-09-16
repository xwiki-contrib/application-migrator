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
import org.xwiki.model.reference.LocalDocumentReference;

import com.xpn.xwiki.doc.AbstractMandatoryClassInitializer;
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
@Named("Migrator.Migrators.DocumentMigrationClass")
public class DocumentMigrationClassDocumentInitializer extends AbstractMandatoryClassInitializer
{
    /**
     * The name of the XClass.
     */
    public static final String CLASS_NAME = "DocumentMigrationClass";

    /**
     * The XClass reference.
     */
    public static final String CLASS_REFERENCE = String.format("Migrator.Migrators.%s", CLASS_NAME);

    /**
     * The name of the migration.
     */
    public static final String MIGRATION_NAME_PROPERTY = "migrationName";

    /**
     * The description of the migration.
     */
    public static final String MIGRATION_DESCRIPTION_PROPERTY = "migrationDescription";

    /**
     * The extension ID targeted by the migration.
     */
    public static final String EXTENSION_ID_PROPERTY = "extensionId";

    /**
     * The extension version targeted by the migration.
     */
    public static final String EXTENSION_VERSION_PROPERTY = "extensionVersion";

    /**
     * The reference of the document to target.
     */
    public static final String DOCUMENT_REFERENCE_PROPERTY = "documentReference";

    /**
     * Should the targeted document be deleted?
     */
    public static final String DELETE_DOCUMENT_PROPERTY = "deleteDocument";

    /**
     * The properties mapping.
     */
    public static final String PROPERTIES_MAPPING_PROPERTY = "propertiesMapping";

    private static final List<String> SPACE_PATH = Arrays.asList("Migrator", "Migrators");

    private static final String CHECKBOX_DISPLAY = "checkbox";

    /**
     * Builds a new {@link DocumentMigrationClassDocumentInitializer}.
     */
    public DocumentMigrationClassDocumentInitializer()
    {
        super(new LocalDocumentReference(SPACE_PATH, CLASS_NAME));
    }

    @Override
    protected void createClass(BaseClass xclass)
    {
        xclass.addTextField(MIGRATION_NAME_PROPERTY, "Migration name", 64);
        xclass.addTextField(MIGRATION_DESCRIPTION_PROPERTY, "Migration description", 128);
        xclass.addTextField(EXTENSION_ID_PROPERTY, "Extension ID", 64);
        xclass.addTextField(EXTENSION_VERSION_PROPERTY, "Extension version", 64);
        xclass.addTextField(DOCUMENT_REFERENCE_PROPERTY, "Document reference", 128);
        xclass.addBooleanField(DELETE_DOCUMENT_PROPERTY, "Should the targeted document be deleted?", CHECKBOX_DISPLAY);

        xclass.addTextAreaField(PROPERTIES_MAPPING_PROPERTY, "Properties mapping", 120, 20,
                TextAreaClass.EditorType.PURE_TEXT);
    }
}
