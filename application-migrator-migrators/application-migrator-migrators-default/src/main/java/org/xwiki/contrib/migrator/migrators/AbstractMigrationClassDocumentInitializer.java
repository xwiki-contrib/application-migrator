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
package org.xwiki.contrib.migrator.migrators;

import org.xwiki.model.reference.LocalDocumentReference;

import com.xpn.xwiki.doc.AbstractMandatoryClassInitializer;
import com.xpn.xwiki.objects.classes.BaseClass;

/**
 * This abstract defines the base fields needed in order to build an XClass later used for describing a migration.
 *
 * @version $Id$
 * @since 1.1
 */
public abstract class AbstractMigrationClassDocumentInitializer extends AbstractMandatoryClassInitializer
{
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
     * Builds a new {@link AbstractMigrationClassDocumentInitializer}.
     *
     * @param localDocumentReference the document reference of the XClass
     */
    protected AbstractMigrationClassDocumentInitializer(LocalDocumentReference localDocumentReference)
    {
        super(localDocumentReference);
    }

    @Override
    protected void createClass(BaseClass xclass)
    {
        xclass.addTextField(MIGRATION_NAME_PROPERTY, "Migration name", 64);
        xclass.addTextField(MIGRATION_DESCRIPTION_PROPERTY, "Migration description", 128);
        xclass.addTextField(EXTENSION_ID_PROPERTY, "Extension ID", 64);
        xclass.addTextField(EXTENSION_VERSION_PROPERTY, "Extension version", 64);

        appendClassProperties(xclass);
    }

    /**
     * This method should be used by the migration document initializers in order to append the fields specific
     * to the migration that the XClass will describe.
     *
     * @param xclass the {@link BaseClass} to modify
     */
    protected abstract void appendClassProperties(BaseClass xclass);
}
