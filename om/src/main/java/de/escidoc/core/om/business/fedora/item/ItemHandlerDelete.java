/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE
 * or http://www.escidoc.de/license.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at license/ESCIDOC.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
 * fuer wissenschaftlich-technische Information mbH and Max-Planck-
 * Gesellschaft zur Foerderung der Wissenschaft e.V.  
 * All rights reserved.  Use is subject to license terms.
 */
package de.escidoc.core.om.business.fedora.item;

import de.escidoc.core.common.business.Constants;
import de.escidoc.core.common.business.PropertyMapKeys;
import de.escidoc.core.common.business.fedora.resources.Container;
import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
import de.escidoc.core.common.exceptions.application.notfound.ItemNotFoundException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.application.violated.LockingException;
import de.escidoc.core.common.exceptions.system.FedoraSystemException;
import de.escidoc.core.common.exceptions.system.IntegritySystemException;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
import de.escidoc.core.common.servlet.invocation.BeanMethod;
import de.escidoc.core.common.servlet.invocation.MethodMapper;
import de.escidoc.core.common.util.service.BeanLocator;
import de.escidoc.core.common.util.service.UserContext;
import de.escidoc.core.common.util.stax.StaxParser;
import de.escidoc.core.common.util.stax.handler.foxml.ComponentIdsInItemFoxmlHandler;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Contains methods pertaining deletion of an item. Is extended at least by FedoraItemHandler.
 *
 * @author Michael Schneider
 */
public class ItemHandlerDelete extends ItemHandlerCreate {

    /**
     * Removes an item from repository.
     *
     * @param id The item ID.
     * @throws ItemNotFoundException  If the item can not be found in the repository.
     * @throws LockingException       If the item is locked and the current user is not the one who locked it.
     * @throws InvalidStatusException If the item can not be deleted because it is released.
     * @throws SystemException        Thrown in case of an internal system error.
     * @throws AuthorizationException If further needed access rights are not given.
     * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
     * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
     * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
     * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
     * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
     */
    protected void remove(final String id) throws ItemNotFoundException, LockingException, InvalidStatusException,
        SystemException, AuthorizationException, FedoraSystemException, TripleStoreSystemException,
        WebserverSystemException, IntegritySystemException, XmlParserSystemException {

        // TODO move precondition checks to service method (? FRS)
        setItem(id);
        checkLocked();
        // check if never released
        final String status = getItem().getProperty(PropertyMapKeys.PUBLIC_STATUS);
        if (!status.equals(Constants.STATUS_PENDING) && !status.equals(Constants.STATUS_IN_REVISION)) {
            throw new InvalidStatusException("Item " + getItem().getId() + " is in status " + status
                + ". Can not delete.");
        }

        // remove member entries referring this
        final List<String> containers = getTripleStoreUtility().getContainers(getItem().getId());
        for (final String parent : containers) {
            try {
                final Container container = new Container(parent);
                // call removeMember with current user context (access rights)
                final String param =
                    "<param last-modification-date=\"" + container.getLastModificationDate() + "\"><id>"
                        + getItem().getId() + "</id></param>";

                final MethodMapper methodMapper =
                    (MethodMapper) BeanLocator.getBean("Common.spring.ejb.context", "common.CommonMethodMapper");
                final BeanMethod method =
                    methodMapper.getMethod("/ir/container/" + parent + "/members/remove", null, null, "POST", param);
                method.invokeWithProtocol(UserContext.getHandle(),
                    de.escidoc.core.om.business.fedora.deviation.Constants.USE_SOAP_REQUEST_PROTOCOL);
            }
            catch (final InvocationTargetException e) {
                // unpack Exception from reflection API
                final Throwable cause = e.getCause();
                if (cause instanceof Error) {
                    throw (Error) cause;
                }
                else if (cause instanceof AuthorizationException) {
                    throw (AuthorizationException) cause;
                }
                else {
                    throw new SystemException("An error occured removing member entries for item " + getItem().getId()
                        + ". Container can not be deleted.", cause); // Ignore FindBugs
                }
            }
            catch (final Exception e) {
                throw new SystemException("An error occured removing member entries for item " + getItem().getId()
                    + ". Container can not be deleted.", e);
            }
        }

        // delete every component, even those referred from old versions
        final byte[] foxml = getFedoraUtility().getObjectFoxml(getItem().getId());
        final ByteArrayInputStream in = new ByteArrayInputStream(foxml);

        final StaxParser sp = new StaxParser();
        final ComponentIdsInItemFoxmlHandler cih = new ComponentIdsInItemFoxmlHandler(sp);
        sp.addHandler(cih);
        try {
            sp.parse(in);
            sp.clearHandlerChain();
        }
        catch (final Exception e) {
            throw new WebserverSystemException(e);
        }
        final List<String> componentIds = cih.getComponentIds();
        for (final String componentId : componentIds) {
            getFedoraUtility().deleteObject(componentId, false);
        }

        getFedoraUtility().deleteObject(getItem().getId(), true);
    }

}
