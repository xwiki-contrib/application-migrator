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
import org.xwiki.model.reference.DocumentReference;

/**
 * Define the generic parameters that can be used for document migration descriptors.
 *
 * @version $Id$
 * @since 1.1
 */
public class DocumentMigrationParameters implements MigrationParameters<DocumentMigrationType>
{
    private DocumentReference documentReference;

    private boolean deleteDocument;

    /**
     * Constructs a new {@link DocumentMigrationParameters}.
     *
     * @param documentReference the document that is targeted by the current migration
     * @param deleteDocument should the current document be deleted?
     */
    public DocumentMigrationParameters(DocumentReference documentReference, boolean deleteDocument)
    {
        this.documentReference = documentReference;
        this.deleteDocument = deleteDocument;
    }

    /**
     * @return the reference to the document that will be dealt with during the migration
     */
    public DocumentReference getDocumentReference()
    {
        return documentReference;
    }

    /**
     * @return wether the document should be deleted or not
     */
    public boolean isDeleteDocument()
    {
        return deleteDocument;
    }
}
