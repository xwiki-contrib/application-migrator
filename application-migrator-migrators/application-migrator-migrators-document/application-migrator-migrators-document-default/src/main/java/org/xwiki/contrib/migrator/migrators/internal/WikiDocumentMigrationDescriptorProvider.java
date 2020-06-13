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

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.migrator.migrators.AbstractWikiMigrationDescriptorProvider;
import org.xwiki.extension.ExtensionId;

import com.xpn.xwiki.objects.BaseObject;

/**
 * Provides {@link DocumentMigrationDescriptor} extracted from wiki pages through the XClass defined in
 * {@link DocumentMigrationClassDocumentInitializer}.
 *
 * @version $Id$
 * @since 1.1
 */
@Component
@Named(WikiDocumentMigrationDescriptorProvider.COMPONENT_NAME)
@Singleton
public class WikiDocumentMigrationDescriptorProvider extends AbstractWikiMigrationDescriptorProvider
{
    /**
     * The name of the component used as a hint against the ComponentManager.
     */
    public static final String COMPONENT_NAME = "WikiDocumentMigrationDescriptorProvider";

    @Override
    protected DocumentMigrationDescriptor createFromBaseObject(BaseObject object)
    {
        ExtensionId extensionId =
            new ExtensionId(object.getStringValue(DocumentMigrationClassDocumentInitializer.EXTENSION_ID_PROPERTY),
                object.getStringValue(DocumentMigrationClassDocumentInitializer.EXTENSION_VERSION_PROPERTY));

        DocumentMigrationParameters migrationParameters = new DocumentMigrationParameters(
            stringDocumentReferenceResolver
                .resolve(object.getStringValue(DocumentMigrationClassDocumentInitializer.DOCUMENT_REFERENCE_PROPERTY)),
            (object.getIntValue(DocumentMigrationClassDocumentInitializer.DELETE_DOCUMENT_PROPERTY, 0) == 1));

        return new DocumentMigrationDescriptor(extensionId,
            object.getStringValue(DocumentMigrationClassDocumentInitializer.MIGRATION_NAME_PROPERTY),
            object.getStringValue(DocumentMigrationClassDocumentInitializer.MIGRATION_DESCRIPTION_PROPERTY),
            migrationParameters);
    }

    @Override
    protected String getXClassReferenceAsString()
    {
        return DocumentMigrationClassDocumentInitializer.CLASS_REFERENCE;
    }
}
