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
package de.escidoc.core.test.aa;

import de.escidoc.core.common.exceptions.remote.application.security.AuthorizationException;
import de.escidoc.core.test.EscidocRestSoapTestBase;
import de.escidoc.core.test.common.client.servlet.Constants;
import de.escidoc.core.test.security.client.PWCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Test suite for the role Collaborator.
 *
 * @author Michael Hoppe
 */
public class CollaboratorAbstractTest extends GrantTestBase {

    protected static final String HANDLE = PWCallback.TEST_HANDLE;

    protected static final String LOGINNAME = HANDLE;

    protected static final String PASSWORD = PWCallback.PASSWORD;

    protected static String grantCreationUserOrGroupId = null;

    protected static int methodCounter = 0;

    protected static String contextId = null;

    protected static String contextHref = null;

    protected static String itemId = null;

    protected static String itemHref = null;

    protected static String containerId = null;

    protected static String containerHref = null;

    protected static String containerId2 = null;

    protected static String containerHref2 = null;

    protected static String publicComponentId = null;

    protected static String publicComponentHref = null;

    protected static String privateComponentId = null;

    protected static String privateComponentHref = null;

    /**
     * The constructor.
     *
     * @param transport     The transport identifier.
     * @param handlerCode   handlerCode of either UserAccountHandler or UserGroupHandler.
     * @param userOrGroupId userOrGroupId for grantCreation.
     * @throws Exception If anything fails.
     */
    public CollaboratorAbstractTest(final int transport, final int handlerCode, final String userOrGroupId)
        throws Exception {
        super(transport, handlerCode);
        grantCreationUserOrGroupId = userOrGroupId;
    }

    /**
     * Set up servlet test.
     *
     * @throws Exception If anything fails.
     */
    @Before
    public void initialize() throws Exception {
        revokeAllGrants(grantCreationUserOrGroupId);
        if (methodCounter == 0) {
            prepare();
        }
    }

    /**
     * Clean up after servlet test.
     *
     * @throws Exception If anything fails.
     */
    @After
    public void deinitialize() throws Exception {
        methodCounter++;
        revokeAllGrants(grantCreationUserOrGroupId);
    }

    /**
     * insert data into system for the tests.
     *
     * @throws Exception If anything fails.
     */
    protected void prepare() throws Exception {

        //create container
        String containerXml = prepareContainer(PWCallback.DEFAULT_HANDLE, STATUS_PENDING, null, false, false);
        Document containerDocument = EscidocRestSoapTestBase.getDocument(containerXml);
        containerId = getObjidValue(containerDocument);
        containerHref = Constants.CONTAINER_BASE_URI + "/" + containerId;

        //create container
        containerXml = prepareContainer(PWCallback.DEFAULT_HANDLE, STATUS_PENDING, null, false, false);
        Document containerDocument2 = EscidocRestSoapTestBase.getDocument(containerXml);
        containerId2 = getObjidValue(containerDocument2);
        containerHref2 = Constants.CONTAINER_BASE_URI + "/" + containerId2;

        //create item in status pending
        // in context /ir/context/escidoc:persistent3
        String itemXml = prepareItem(PWCallback.DEFAULT_HANDLE, STATUS_PENDING, null, false, false);
        Document document = EscidocRestSoapTestBase.getDocument(itemXml);

        //save ids
        contextId = extractContextId(document);
        contextHref = Constants.CONTEXT_BASE_URI + "/" + contextId;
        itemId = getObjidValue(document);
        itemHref = Constants.ITEM_BASE_URI + "/" + itemId;
        publicComponentId = extractComponentId(document, VISIBILITY_PUBLIC);
        publicComponentHref = itemHref + "/" + Constants.SUB_COMPONENT + "/" + publicComponentId;
        privateComponentId = extractComponentId(document, VISIBILITY_PRIVATE);
        privateComponentHref = itemHref + "/" + Constants.SUB_COMPONENT + "/" + privateComponentId;

        //add container2 to container
        String lastModificationDate = getLastModificationDateValue(containerDocument);
        String taskParam =
            "<param last-modification-date=\"" + lastModificationDate + "\"><id>" + containerId2 + "</id></param>";
        getContainerClient().addMembers(containerId, taskParam);

        //add item to container2
        lastModificationDate = getLastModificationDateValue(containerDocument2);
        taskParam = "<param last-modification-date=\"" + lastModificationDate + "\"><id>" + itemId + "</id></param>";
        getContainerClient().addMembers(containerId2, taskParam);

        //update item to create new version
        itemXml = itemXml.replaceAll("semiconductor surfaces", "semiconductor surfaces u");
        itemXml = update(ITEM_HANDLER_CODE, itemId, itemXml);

    }

    /**
     * Test collaborator with scope on item.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testRetrieveItemWithItemScope() throws Exception {
        doTestRetrieveWithRole(grantCreationUserOrGroupId, ROLE_HREF_COLLABORATOR, itemHref, HANDLE, ITEM_HANDLER_CODE,
            itemId, true, null);
    }

    /**
     * Test collaborator with scope on component.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testRetrieveItemWithContentScope() throws Exception {
        doTestRetrieveWithRole(grantCreationUserOrGroupId, ROLE_HREF_COLLABORATOR, privateComponentHref, HANDLE,
            ITEM_HANDLER_CODE, itemId, true, null);
    }

    /**
     * Test declining collaborator with scope on container.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testRetrieveItemWithContainerScopeDecline() throws Exception {
        doTestRetrieveWithRole(grantCreationUserOrGroupId, ROLE_HREF_COLLABORATOR, containerHref2, HANDLE,
            ITEM_HANDLER_CODE, itemId, true, AuthorizationException.class);
    }

    /**
     * Test collaborator with scope on container.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testRetrieveContainerWithContainerScope() throws Exception {
        doTestRetrieveWithRole(grantCreationUserOrGroupId, ROLE_HREF_COLLABORATOR, containerHref, HANDLE,
            CONTAINER_HANDLER_CODE, containerId, true, null);
    }

    /**
     * Test declining collaborator with scope on container.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testRetrieveContainerWithParentContainerScopeDecline() throws Exception {
        doTestRetrieveWithRole(grantCreationUserOrGroupId, ROLE_HREF_COLLABORATOR, containerHref, HANDLE,
            CONTAINER_HANDLER_CODE, containerId2, true, AuthorizationException.class);
    }

    /**
     * Test collaborator with scope on parent container, decline.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testRetrieveItemWithParentContainerScopeDecline() throws Exception {
        doTestRetrieveWithRole(grantCreationUserOrGroupId, ROLE_HREF_COLLABORATOR, containerHref, HANDLE,
            ITEM_HANDLER_CODE, itemId, true, AuthorizationException.class);
    }

    /**
     * Test collaborator with scope on context.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testRetrieveItemWithContextScope() throws Exception {
        doTestRetrieveWithRole(grantCreationUserOrGroupId, ROLE_HREF_COLLABORATOR, contextHref, HANDLE,
            ITEM_HANDLER_CODE, itemId, true, null);
    }

    /**
     * Test collaborator with scope on context.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testRetrieveParentContainerWithContextScope() throws Exception {
        doTestRetrieveWithRole(grantCreationUserOrGroupId, ROLE_HREF_COLLABORATOR, contextHref, HANDLE,
            CONTAINER_HANDLER_CODE, containerId, true, null);
    }

    /**
     * Test collaborator with scope on context.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testRetrieveContainerWithContextScope() throws Exception {
        doTestRetrieveWithRole(grantCreationUserOrGroupId, ROLE_HREF_COLLABORATOR, contextHref, HANDLE,
            CONTAINER_HANDLER_CODE, containerId2, true, null);
    }

    /**
     * Test collaborator with no scope.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testRetrieveItemWithNoScope() throws Exception {
        doTestRetrieveWithRole(grantCreationUserOrGroupId, ROLE_HREF_COLLABORATOR, null, HANDLE, ITEM_HANDLER_CODE,
            itemId, true, AuthorizationException.class);
    }

    /**
     * Test logging out a collaborator.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testAaCollaboratorLogout() throws Exception {

        doTestLogout(LOGINNAME, PASSWORD);
    }
}
