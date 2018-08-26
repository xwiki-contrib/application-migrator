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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.migrator.MigrationException;
import org.xwiki.contrib.migrator.MigrationManager;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.event.ExtensionUpgradedEvent;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.Event;

/**
 * This listener aims to trigger an eventual XClass migration during an extension upgrade.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
@Named(UpgradedExtensionEventListener.LISTENER_NAME)
public class UpgradedExtensionEventListener extends AbstractEventListener
{
    /**
     * The listener name.
     */
    public static final String LISTENER_NAME = "UpgradedExtensionEventListener";

    @Inject
    private MigrationManager migrationManager;

    @Inject
    private Logger logger;

    /**
     * Build a new {@link UpgradedExtensionEventListener}.
     */
    public UpgradedExtensionEventListener()
    {
        super(LISTENER_NAME, new ExtensionUpgradedEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (event instanceof ExtensionUpgradedEvent) {
            InstalledExtension installedExtension = (InstalledExtension) source;

            try {
                if (migrationManager.hasAvailableMigrations(installedExtension.getId())) {
                    migrationManager.applyMigrationsForVersion(installedExtension.getId());
                }
            } catch (MigrationException e) {
                logger.error("Failed to apply extension migrations correctly.", e);
            }
        }
    }
}
