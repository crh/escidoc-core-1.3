/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
 * only (the "License"). You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
 * the specific language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
 * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
 * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
 * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
 * terms.
 */

package de.escidoc.core.common.util.service;

import de.escidoc.core.aa.service.interfaces.PolicyDecisionPointInterface;
import de.escidoc.core.aa.service.interfaces.UserGroupHandlerInterface;
import de.escidoc.core.aa.service.interfaces.UserManagementWrapperInterface;
import de.escidoc.core.adm.service.interfaces.AdminHandlerInterface;
import de.escidoc.core.cmm.service.interfaces.ContentModelHandlerInterface;
import de.escidoc.core.common.business.fedora.TripleStoreUtility;
import de.escidoc.core.common.business.indexing.GsearchHandler;
import de.escidoc.core.common.business.indexing.IndexingHandler;
import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.om.service.interfaces.ContainerHandlerInterface;
import de.escidoc.core.om.service.interfaces.ContentRelationHandlerInterface;
import de.escidoc.core.om.service.interfaces.ContextHandlerInterface;
import de.escidoc.core.om.service.interfaces.FedoraDescribeDeviationHandlerInterface;
import de.escidoc.core.om.service.interfaces.ItemHandlerInterface;
import de.escidoc.core.oum.service.interfaces.OrganizationalUnitHandlerInterface;
import de.escidoc.core.tme.service.interfaces.JhoveHandlerInterface;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;

/**
 * Class supporting locating of spring beans and the resource handler's EJBs.
 *
 * @author Torsten Tetteroo
 */
public final class BeanLocator {

    public static final String COMMON_FACTORY_ID = "Common.spring.ejb.context";

    public static final String AA_FACTORY_ID = "Aa.spring.ejb.context";

    public static final String ADM_FACTORY_ID = "Adm.spring.ejb.context";

    public static final String CMM_FACTORY_ID = "Cmm.spring.ejb.context";

    public static final String OM_FACTORY_ID = "Om.spring.ejb.context";

    public static final String OUM_FACTORY_ID = "Oum.spring.ejb.context";

    public static final String SM_FACTORY_ID = "Sm.spring.ejb.context";

    public static final String ST_FACTORY_ID = "St.spring.ejb.context";

    public static final String TME_FACTORY_ID = "Tme.spring.ejb.context";

    /**
     * Private constructor, prevents creation of instances.
     */
    private BeanLocator() {
    }

    public static BeanFactory getBeanFactory(final String beanContextId) {
        final BeanFactoryLocator beanFactoryLocator = SingletonBeanFactoryLocator.getInstance();
        return beanFactoryLocator.useBeanFactory(beanContextId).getFactory();
    }

    public static Object getBean(final String beanFactoryId, final String beanId) throws WebserverSystemException {

        try {
            return getBeanFactory(beanFactoryId).getBean(beanId);
        }
        catch (final Exception e) {
            throw new WebserverSystemException(e.getMessage() + " for BeanFactory " + beanFactoryId, e);
        }
    }

    /**
     * Gets the type of the specified bean from the specified bean factory.
     *
     * @param beanFactoryId The id of the factory bean.
     * @param beanId        The id of the bean.
     * @return Returns the bean.
     * @throws WebserverSystemException Thrown in case of an internal error during bean creation.
     */
    public static Class<?> getBeanType(final String beanFactoryId, final String beanId) throws WebserverSystemException {

        try {
            return getBeanFactory(beanFactoryId).getType(beanId);
        }
        catch (final Exception e) {
            throw new WebserverSystemException(e.getMessage(), e);
        }
    }

    /**
     * Locates the admin handler EJB bean.
     *
     * @return Returns the located admin handler EJB bean.
     * @throws WebserverSystemException Thrown in case of an internal error during bean creation.
     */
    public static AdminHandlerInterface locateAdminHandler() throws WebserverSystemException {

        return (AdminHandlerInterface) getBean(ADM_FACTORY_ID, "service.AdminHandler");
    }

    /**
     * Locates the policy decision point (PDP) EJB bean.
     *
     * @return Returns the located policy decision point EJB bean.
     * @throws WebserverSystemException Thrown in case of an internal error during bean creation.
     */
    public static PolicyDecisionPointInterface locatePolicyDecisionPoint() throws WebserverSystemException {

        return (PolicyDecisionPointInterface) getBean(AA_FACTORY_ID, "service.PolicyDecisionPoint");
    }

    /**
     * Locates the container handler EJB bean.
     *
     * @return Returns the located container handler EJB bean.
     * @throws WebserverSystemException Thrown in case of an internal error during bean creation.
     */
    public static ContainerHandlerInterface locateContainerHandler() throws WebserverSystemException {

        return (ContainerHandlerInterface) getBean(OM_FACTORY_ID, "service.ContainerHandler");
    }

    /**
     * Locates the context handler EJB bean.
     *
     * @return Returns the located context handler EJB bean.
     * @throws WebserverSystemException Thrown in case of an internal error during bean creation.
     */
    public static ContextHandlerInterface locateContextHandler() throws WebserverSystemException {

        return (ContextHandlerInterface) getBean(OM_FACTORY_ID, "service.ContextHandler");
    }

    /**
     * Locates the Content Model handler EJB bean.
     *
     * @return Returns the located Content Model handler EJB bean.
     * @throws WebserverSystemException Thrown in case of an internal error during bean creation.
     */
    public static ContentModelHandlerInterface locateContentModelHandler() throws WebserverSystemException {

        return (ContentModelHandlerInterface) getBean(CMM_FACTORY_ID, "service.ContentModelHandler");
    }

    /**
     * Locates the content relation handler EJB bean.
     *
     * @return Returns the located content relation handler EJB bean.
     * @throws WebserverSystemException Thrown in case of an internal error during bean creation.
     */
    public static ContentRelationHandlerInterface locateContentRelationHandler() throws WebserverSystemException {

        return (ContentRelationHandlerInterface) getBean(OM_FACTORY_ID, "service.ContentRelationHandler");
    }

    /**
     * Locates the gsearch handler.
     *
     * @return Returns the located gsearch handler.
     * @throws WebserverSystemException Thrown in case of an internal error during bean creation.
     */
    public static GsearchHandler locateGsearchHandler() throws WebserverSystemException {

        return (GsearchHandler) getBean(COMMON_FACTORY_ID, "common.business.indexing.GsearchHandler");
    }

    /**
     * Locates the indexing handler.
     *
     * @return Returns the located indexing handler.
     * @throws WebserverSystemException Thrown in case of an internal error during bean creation.
     */
    public static IndexingHandler locateIndexingHandler() throws WebserverSystemException {

        return (IndexingHandler) getBean(COMMON_FACTORY_ID, "common.business.indexing.IndexingHandler");
    }

    /**
     * Locates the TripleStoreUtility.
     *
     * @return Returns the located TripleStoreUtility.
     * @throws WebserverSystemException Thrown in case of an internal error during bean creation.
     */
    public static TripleStoreUtility locateTripleStoreUtility() throws WebserverSystemException {

        return (TripleStoreUtility) getBean(COMMON_FACTORY_ID, "business.TripleStoreUtility");
    }

    /**
     * Locates the item handler EJB bean.
     *
     * @return Returns the located item handler EJB bean.
     * @throws WebserverSystemException Thrown in case of an internal error during bean creation.
     */
    public static ItemHandlerInterface locateItemHandler() throws WebserverSystemException {

        return (ItemHandlerInterface) getBean(OM_FACTORY_ID, "service.ItemHandler");
    }

    /**
     * Locates the organizational unit handler EJB bean.
     *
     * @return Returns the located organizational unit handler wrapper EJB bean.
     * @throws WebserverSystemException Thrown in case of an internal error during bean creation.
     */
    public static OrganizationalUnitHandlerInterface locateOrganizationalUnitHandler() throws WebserverSystemException {

        return (OrganizationalUnitHandlerInterface) getBean(OUM_FACTORY_ID, "service.OrganizationalUnitHandler");
    }

    /**
     * Locates the user management wrapper EJB bean.
     *
     * @return Returns the located user management wrapper EJB bean.
     * @throws WebserverSystemException Thrown in case of an internal error during bean creation.
     */
    public static UserManagementWrapperInterface locateUserManagementWrapper() throws WebserverSystemException {

        return (UserManagementWrapperInterface) getBean(AA_FACTORY_ID, "service.UserManagementWrapper");
    }

    /**
     * Locates the fedora describe deviation handler EJB bean.
     *
     * @return Returns the located fedora describe deviation handler EJB bean.
     * @throws WebserverSystemException Thrown in case of an internal error during bean creation.
     */
    public static FedoraDescribeDeviationHandlerInterface locateFedoraDescribeDeviationHandler()
        throws WebserverSystemException {

        return (FedoraDescribeDeviationHandlerInterface) getBean(OM_FACTORY_ID,
            "service.FedoraDescribeDeviationHandler");
    }

    /**
     * Locates the jhove handler EJB bean.
     *
     * @return Returns the located jhove handler EJB bean.
     * @throws WebserverSystemException Thrown in case of an internal error during bean creation.
     */
    public static JhoveHandlerInterface locateJhoveHandler() throws WebserverSystemException {

        return (JhoveHandlerInterface) getBean(TME_FACTORY_ID, "service.JhoveHandler");
    }

    /**
     * Locates the user group handler EJB bean.
     *
     * @return Returns the located user group handler EJB bean.
     * @throws WebserverSystemException Thrown in case of an internal error during bean creation.
     */
    public static UserGroupHandlerInterface locateUserGroupHandler() throws WebserverSystemException {

        return (UserGroupHandlerInterface) getBean(AA_FACTORY_ID, "service.UserGroupHandler");
    }
}
