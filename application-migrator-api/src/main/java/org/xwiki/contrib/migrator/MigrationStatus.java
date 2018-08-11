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
package org.xwiki.contrib.migrator;

import org.xwiki.text.StringUtils;

/**
 * Define a migration status. This is specially used by the {@link MigrationExecutor} when executing migrations.
 *
 * @version $Id$
 * @since 1.0
 */
public class MigrationStatus
{
    /**
     * Define a list of status that can be returned.
     *
     * TODO: Make this extensible so that anyone can register new migration statuses.
     */
    public enum Status {
        /**
         * The migration was successful.
         */
        SUCCESS,

        /**
         * The migration failed.
         */
        FAILURE
    }

    /**
     * A simple instance of a successful migration status.
     */
    public static final MigrationStatus SUCCESS = new MigrationStatus(Status.SUCCESS);

    /**
     * A simple instance of a failed migration status.
     */
    public static final MigrationStatus FAILURE = new MigrationStatus(Status.FAILURE);

    private Status status;

    private String message;

    /**
     * Builds a new MigrationStatus.
     *
     * @param status the status of the migration
     */
    public MigrationStatus(Status status)
    {
        this(status, StringUtils.EMPTY);
    }

    /**
     * Builds a new MigrationStatus.
     *
     * @param status the status of the migration
     * @param message an explicative message
     */
    public MigrationStatus(Status status, String message)
    {
        this.status = status;
        this.message = message;
    }

    /**
     * @return the status of the migration
     */
    public Status getStatus()
    {
        return this.status;
    }

    /**
     * @return a message explaining the current status. Note that the message in itself can be empty if undefined.
     */
    public String getMessage()
    {
        return this.message;
    }
}
