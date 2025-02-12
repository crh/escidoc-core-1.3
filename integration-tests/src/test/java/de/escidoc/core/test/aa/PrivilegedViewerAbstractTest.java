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

import de.escidoc.core.test.EscidocRestSoapTestBase;
import de.escidoc.core.test.common.client.servlet.Constants;
import de.escidoc.core.test.security.client.PWCallback;
import org.junit.After;
import org.junit.Before;
import org.w3c.dom.Document;

import java.util.HashMap;

/**
 * Test suite for the role PrivilegedViewer.
 *
 * @author Michael Hoppe
 */
public class PrivilegedViewerAbstractTest extends GrantTestBase {

    private static final String HANDLE = PWCallback.TEST_HANDLE;

    private static final String LOGINNAME = HANDLE;

    private static final String PASSWORD = PWCallback.PASSWORD;

    protected static String grantCreationUserOrGroupId = null;

    private static int methodCounter = 0;

    protected static final String ALLOWED_CONTEXT = CONTEXT_ID;

    protected static final String DISALLOWED_CONTEXT = CONTEXT_ID1;

    /**
     * The constructor.
     *
     * @param transport     The transport identifier.
     * @param handlerCode   handlerCode of either UserAccountHandler or UserGroupHandler.
     * @param userOrGroupId userOrGroupId for grantCreation.
     * @throws Exception If anything fails.
     */
    public PrivilegedViewerAbstractTest(final int transport, final int handlerCode, final String userOrGroupId)
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
        if (methodCounter == 0) {
            //revoke all Grants
            revokeAllGrants(grantCreationUserOrGroupId);
            //create grant privileged viewer for user grantCreationUserOrGroupId 
            //with scope on default-context
            doTestCreateGrant(null, grantCreationUserOrGroupId, Constants.CONTEXT_BASE_URI + "/" + CONTEXT_ID,
                ROLE_HREF_PRIVILEGED_VIEWER, null);
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
        if (methodCounter == getTestAnnotationsCount()) {
            revokeAllGrants(grantCreationUserOrGroupId);
            methodCounter = 0;
        }
    }

    /**
     * insert data into system for the tests.
     *
     * @param status    the status the item has to have.
     * @param contextId the contextId the item has to have.
     * @return HashMap with itemId and componentIds
     * @throws Exception If anything fails.
     */
    protected HashMap<String, String> prepare(final String status, final String contextId) throws Exception {
        HashMap<String, String> createdIds = new HashMap<String, String>();
        String itemXml = prepareItem(PWCallback.DEFAULT_HANDLE, status, contextId, false, false);
        Document document = EscidocRestSoapTestBase.getDocument(itemXml);
        createdIds.put("itemId", getObjidValue(document));
        createdIds.put("publicComponentId", extractComponentId(document, VISIBILITY_PUBLIC));
        createdIds.put("privateComponentId", extractComponentId(document, VISIBILITY_PRIVATE));
        return createdIds;

    }

}
