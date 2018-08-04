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
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.LocalDocumentReference;

import com.xpn.xwiki.doc.AbstractMandatoryClassInitializer;
import com.xpn.xwiki.objects.classes.BaseClass;

/**
 * This class helps defining a migration that has been successfully applied for a given extension ID.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
@Named("Migrator.Code.MigrationHistoryClass")
public class MigrationHistoryClassDocumentInitializer extends AbstractMandatoryClassInitializer
{
    /**
     * The name of the XClass.
     */
    public static final String CLASS_NAME = "MigrationHistoryClass";

    /**
     * The XClass reference.
     */
    public static final String CLASS_REFERENCE = String.format("%s.%s.%s",
            DefaultMigrationHistoryStore.MIGRATOR_SPACE_NAME,
            DefaultMigrationHistoryStore.CODE_SPACE_NAME, CLASS_NAME);

    /**
     * The name of the UUID property.
     */
    public static final String UUID_PROPERTY = "uuid";

    private static final List<String> SPACE_PATH = Arrays.asList(
            DefaultMigrationHistoryStore.MIGRATOR_SPACE_NAME,
            DefaultMigrationHistoryStore.CODE_SPACE_NAME);

    /**
     * Builds a new {@link MigrationHistoryClassDocumentInitializer}.
     */
    public MigrationHistoryClassDocumentInitializer()
    {
        super(new LocalDocumentReference(SPACE_PATH, CLASS_NAME));
    }

    @Override
    protected void createClass(BaseClass xclass)
    {
        xclass.addTextField(UUID_PROPERTY, "Migration UUID", 128);
    }
}
