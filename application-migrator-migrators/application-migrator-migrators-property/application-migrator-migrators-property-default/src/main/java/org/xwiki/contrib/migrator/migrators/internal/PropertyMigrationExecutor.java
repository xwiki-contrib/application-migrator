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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.migrator.MigrationException;
import org.xwiki.contrib.migrator.MigrationExecutor;
import org.xwiki.contrib.migrator.MigrationStatus;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;

/**
 * Executes migrations of type {@link PropertyMigrationType}.
 * 
 * @version $Id$
 * @since 1.1
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class PropertyMigrationExecutor implements MigrationExecutor<PropertyMigrationDescriptor>
{
    private static final String STRING_TYPE = "String";

    private static final String TEXT_AREA_TYPE = "TextArea";

    private static final String NUMBER_TYPE = "Number";

    private static final String EMAIL_TYPE = "EMail";

    @Inject
    private Provider<XWikiContext> xWikiContextProvider;

    @Inject
    private Logger logger;

    @Inject
    private DocumentReferenceResolver<String> documentReferenceResolver;

    private XWikiContext xWikiContext;

    private XWiki xwiki;

    private PropertyMigrationDescriptor migrationDescriptor;

    private PropertyMigrationParameters migrationParameters;

    private DocumentReference classReference;

    private PropertyClass oldPropertyType;

    private PropertyClass newPropertyType;

    private PropertyConversionCheckerProvider provider = new PropertyConversionCheckerProvider();

    @Inject
    @Named("context")
    private ComponentManager componentManager;

    static class PropertyConversionCheckerProvider
    {
        private List<PropertyConversionChecker> checkList;

        PropertyConversionCheckerProvider()
        {
            this.checkList = new ArrayList<PropertyConversionChecker>();
        }

        public List<PropertyConversionChecker> getList()
        {
            return this.checkList;
        }
    }

    private class PropertyConversionChecker
    {
        private List<String> allSupportedProperties;

        private String propertyType;

        PropertyConversionChecker(String propertyType)
        {
            this.propertyType = propertyType;
        }

        public List<String> getAllSupportedProperties()
        {
            return this.allSupportedProperties;
        }

        public String getPropertyType()
        {
            return this.propertyType;
        }
    }

    @Override
    public MigrationStatus execute(PropertyMigrationDescriptor migrationDescriptor) throws MigrationException
    {
        this.migrationDescriptor = migrationDescriptor;
        migrationParameters = (PropertyMigrationParameters) this.migrationDescriptor.getMigrationParameters();

        xWikiContext = xWikiContextProvider.get();
        xwiki = xWikiContext.getWiki();

        populizePropertyConversionChecker();

        // STEP 1 : we get two properties (old property and new property could be document meta data)
        classReference = documentReferenceResolver.resolve(migrationParameters.getTargetClass());
        if (!xwiki.exists(classReference, xWikiContext)) {
            logger.error("The new class reference does not exists! Aborting ...");
            throw new MigrationException("Failed to migrate the XClasses : the new class does not exist.");
        }

        BaseClass c = new BaseClass();

        try {
            c = xwiki.getDocument(classReference, xWikiContext).getXClass();
        } catch (XWikiException e) {
            e.printStackTrace();
        }

        // get two propertyClasses, then compare name
        for (Object o : c.getProperties()) {
            PropertyClass propertyClass = (PropertyClass) o;
            if (propertyClass.getName().equals(migrationParameters.getOldProperty())) {
                oldPropertyType = propertyClass;
            }
            if (propertyClass.getName().equals(migrationParameters.getNewProperty())) {
                newPropertyType = propertyClass;
            }
        }
        // STEP 2 : check compatibility (from anything to String (Compatible))
        if (areTwoPropertiesCompatible(oldPropertyType, newPropertyType)) {
            // STEP 3: start migration
            // TODO : isRemovedOldProperty?
            try {
                migrateProperty(xwiki.getDocument(classReference, xWikiContext));
            } catch (XWikiException e) {
                throw new MigrationException(
                    String.format("Failed to migrate the old property [%s]", migrationParameters.getOldProperty()));
            }
        } else {
            return MigrationStatus.FAILURE;
        }

        return MigrationStatus.SUCCESS;
    }

    private void populizePropertyConversionChecker()
    {
        // There are examples of how the conversion check should be initialized
        // In the future, we will support more types, and they can be defined here
        logger.info("Initializing Checkers for property conversion ...");

        PropertyConversionChecker pStringChecker = new PropertyConversionChecker(STRING_TYPE);
        pStringChecker.getAllSupportedProperties().add(STRING_TYPE);

        PropertyConversionChecker pTextAreaChecker = new PropertyConversionChecker(TEXT_AREA_TYPE);
        pTextAreaChecker.getAllSupportedProperties().add(STRING_TYPE);

        PropertyConversionChecker pNumberChecker = new PropertyConversionChecker(NUMBER_TYPE);
        pNumberChecker.getAllSupportedProperties().add(STRING_TYPE);

        PropertyConversionChecker pEMailChecker = new PropertyConversionChecker(EMAIL_TYPE);
        pEMailChecker.getAllSupportedProperties().add(STRING_TYPE);

        provider.getList().add(pStringChecker);
        provider.getList().add(pTextAreaChecker);
        provider.getList().add(pNumberChecker);
        provider.getList().add(pEMailChecker);
    }

    private boolean areTwoPropertiesCompatible(PropertyClass oldPropertyType, PropertyClass newPropertyType)
        throws MigrationException
    {
        logger.info("checking compatibility for property conversion ...");

        PropertyConversionChecker localChecker = null;

        // case 1 : same type
        if (oldPropertyType.getName().equals(newPropertyType.getName())) {
            return true;
        }
        // case 2 : different types
        // get the proper checker
        for (PropertyConversionChecker checker : provider.getList()) {
            if (oldPropertyType.getName().equals(checker.getPropertyType())) {
                localChecker = checker;
            }
        }
        // check if conversion between the target type and the resource type is supported
        try {
            for (String supportedType : localChecker.getAllSupportedProperties()) {
                if (newPropertyType.getName() == supportedType) {
                    return true;
                }
            }
        } catch (NullPointerException e) {
            throw new MigrationException(
                String.format("old Property type:  [%s] is not supported.", oldPropertyType.getName()), e);
        }

        return false;
    }

    private void migrateProperty(XWikiDocument document) throws MigrationException
    {
        logger.info("Migrating a property from type [{}] to new type [{}].", oldPropertyType.getName(),
            newPropertyType.getName());

        // set the new field with the old property class
        document.setProperty(classReference, newPropertyType.getName(),
            oldPropertyType.fromString(migrationParameters.getOldProperty()));
    }

}
