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

import java.util.Map;

import org.xwiki.contrib.migrator.MigrationParameters;

/**
 * Define the generic parameters that can be used for class migration descriptors.
 *
 * @version $Id$
 * @since 1.0
 */
public class ClassMigrationParameters implements MigrationParameters<ClassMigrationType>
{
    private boolean inPlace;

    private String oldClass;

    private String newClass;

    private boolean removeOldXClass;

    private boolean removeOldXObjects;

    private Map<String, String> propertiesMapping;

    private String saveComment;

    private boolean createNewVersion;

    /**
     * Constructs a new {@link ClassMigrationParameters}.
     *
     * @param inPlace whether the migration should be done in place
     * @param oldClass the reference to the old XClass
     * @param newClass the reference to the new XClass
     * @param removeOldXClass should the old XClass be removed?
     * @param removeOldXObjects should the old XObjects be removed?
     * @param propertiesMapping the properties mapping if necessary
     */
    public ClassMigrationParameters(boolean inPlace, String oldClass, String newClass, boolean removeOldXClass,
            boolean removeOldXObjects, Map<String, String> propertiesMapping)
    {
        this.inPlace = inPlace;
        this.oldClass = oldClass;
        this.newClass = newClass;
        this.removeOldXClass = removeOldXClass;
        this.removeOldXObjects = removeOldXObjects;
        this.propertiesMapping = propertiesMapping;
    }

    /**
     * @param saveComment the save comment
     * @param createNewVersion whether the document should be updated
     */
    public void setSaveParameters(String saveComment, boolean createNewVersion)
    {
        this.saveComment = saveComment;
        this.createNewVersion = createNewVersion;
    }

    /**
     * @return whether the migration should be done in place (on the same XClass)
     */
    public boolean isInPlace()
    {
        return inPlace;
    }

    /**
     * @return a string reference to the old XClass
     */
    public String getOldClass()
    {
        return oldClass;
    }

    /**
     * @return a string reference to the new XClass
     */
    public String getNewClass()
    {
        return newClass;
    }

    /**
     * @return whether the old XClass should be removed after the main migration
     */
    public boolean isRemoveOldXClass()
    {
        return removeOldXClass;
    }

    /**
     * @return whether the old XObjects should be removed when migrating to the new XClass
     */
    public boolean isRemoveOldXObjects()
    {
        return removeOldXObjects;
    }

    /**
     * @return the properties mapping between the two XClasses
     */
    public Map<String, String> getPropertiesMapping()
    {
        return propertiesMapping;
    }

    /**
     * @return the save comment to be used when saving the glossary entry
     */
    public String getSaveComment()
    {
        return saveComment;
    }

    /**
     * @return true if the document should be updated (if a new entry should be created in the document history)
     */
    public boolean isCreateNewVersion()
    {
        return createNewVersion;
    }
}
