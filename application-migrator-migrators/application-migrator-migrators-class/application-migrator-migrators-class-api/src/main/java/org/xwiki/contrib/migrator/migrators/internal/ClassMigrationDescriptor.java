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

import org.xwiki.contrib.migrator.AbstractMigrationDescriptor;
import org.xwiki.contrib.migrator.MigrationParameters;
import org.xwiki.extension.ExtensionId;

/**
 * Define a class migration descriptor. This class can be used in order to describe migrations that are related to
 * classes.
 *
 * @version $Id$
 * @since 1.0
 */
public class ClassMigrationDescriptor extends AbstractMigrationDescriptor<ClassMigrationType>
{
    /**
     * Special mapping property to refer to the document content.
     */
    public static final String DOC_CONTENT_MAPPING_PROPERTY = "document:content";

    /**
     * Special mapping property to refer to the document title.
     */
    public static final String DOC_TITLE_MAPPING_PROPERTY = "document:title";

    /**
     * Builds a new {@link ClassMigrationDescriptor}.
     *
     * @param extensionId the extension to which this migration applies
     * @param migrationName the name of the migration
     * @param migrationDescription a description of the migration
     * @param migrationParameters the migration parameters
     */
    public ClassMigrationDescriptor(ExtensionId extensionId, String migrationName,
            String migrationDescription,
            MigrationParameters<? extends ClassMigrationType> migrationParameters)
    {
        super(extensionId, migrationName, migrationDescription, migrationParameters);
    }
}
