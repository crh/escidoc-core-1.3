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
package de.escidoc.core.test.om.context;

import de.escidoc.core.common.exceptions.remote.application.notfound.AdminDescriptorNotFoundException;
import de.escidoc.core.common.exceptions.remote.application.notfound.ContextNotFoundException;
import de.escidoc.core.test.EscidocRestSoapTestBase;
import de.escidoc.core.test.common.client.servlet.Constants;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.Document;

/**
 * Test the task oriented method retrieveContexts.
 *
 * @author Michael Schneider
 */
@RunWith(value = Parameterized.class)
public class AdminDescriptorsTest extends ContextTestBase {

    private String path = "";

    private static String contextId = null;

    private static String contextXml = null;

    private static final String DEFAULT_ADMIN_DESCRIPTOR_NAME = "admin-descriptor";

    /**
     * @param transport The transport identifier.
     */
    public AdminDescriptorsTest(final int transport) {
        super(transport);
        this.path += "/" + getTransport(false);
    }

    /**
     * Set up servlet test.
     *
     * @throws Exception If anything fails.
     */
    @Before
    public void setUpOnce() throws Exception {

        if (contextId == null) {
            Document context =
                EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_CONTEXT_PATH + this.path, "context_create.xml");
            substitute(context, "/context/properties/name", getUniqueName("PubMan Context "));
            String template = toString(context, false);
            contextXml = create(template);
            assertXmlValidContext(contextXml);
            assertCreatedContext(contextXml, template, startTimestamp);

            contextId = getObjidValue(EscidocRestSoapTestBase.getDocument(contextXml));

            // open Context
            String lastModificationDate = getLastModificationDateValue(EscidocRestSoapTestBase.getDocument(contextXml));
            this.getContextClient().open(contextId,
                getTheLastModificationParam(true, contextId, "comment", lastModificationDate));

            String filename = "escidoc_item_198_for_create.xml";
            if (getTransport() == Constants.TRANSPORT_REST) {
                createItem(toString(substitute(EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/"
                    + getTransport(false), filename), "/item/properties/context/@href", "/ir/context/" + contextId),
                    true));
            }
            else {
                createItem(toString(EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/"
                    + getTransport(false), filename), true));
            }
        }
    }

    /**
     * Test creating an item in the mock framework.
     *
     * @param itemXml The xml representation of the item.
     * @return The created item.
     * @throws Exception If anything fails.
     */
    protected String createItem(final String itemXml) throws Exception {

        return handleXmlResult(getItemClient().create(itemXml));
    }

    /**
     * Test retrieving AdminDescriptors of an existing Context.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOmReCoAdmDescs1() throws Exception {

        String admDescs = retrieveAdminDescriptors(contextId);
        assertXmlValidContext(admDescs);
    }

    /**
     * Test retrieving AdminDescriptors of a not existing Context.
     *
     * @throws Exception If anything fails.
     */
    @Test(expected = ContextNotFoundException.class)
    public void testOmReAdmDescs2() throws Exception {

        retrieveAdminDescriptors("escidoc:UnknownContext");
    }

    /**
     * Test retrieving an AdminDescriptor of an existing Context.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOmReCoAdmDesc1() throws Exception {

        String admDesc = retrieveAdminDescriptor("escidoc:persistent3", "admin-descriptor");
        assertXmlValidContext(admDesc);
    }

    /**
     * Test retrieving an non-existing AdminDescriptor of an existing Context.
     * <p/>
     * See issue INFR-1105.
     *
     * @throws Exception If anything fails.
     */
    @Test(expected = AdminDescriptorNotFoundException.class)
    public void retrieveNonExisitingAdmDesc01() throws Exception {

        retrieveAdminDescriptor(contextId, "Test");
    }

    /**
     * Test updating one admin descriptor.
     * 
     * @throws Exception If anything fails.
     */
    @Test
    @Ignore("Method not implemented yet")
    public void updateAdminDescriptor() throws Exception {

        final String newContent = "<blafasel>&amp;'§$%/()</blafasel>";
        String updatedAdm = updateAdminDescriptor(contextId, DEFAULT_ADMIN_DESCRIPTOR_NAME, newContent);
        String adm = retrieveAdminDescriptor(contextId, DEFAULT_ADMIN_DESCRIPTOR_NAME);
        assertXmlValidContext(adm);
        assertXmlEquals("Admin Descriptor content differs", getDocument(adm), "/admin-descriptor/*[1]", newContent);
    }

}
