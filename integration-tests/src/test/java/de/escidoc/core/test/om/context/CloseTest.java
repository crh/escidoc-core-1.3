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

import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidStatusException;
import de.escidoc.core.test.EscidocRestSoapTestBase;
import de.escidoc.core.test.common.client.servlet.Constants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.Document;

/**
 * Test the mock implementation of the item resource.
 *
 * @author Michael Schneider
 */
@RunWith(value = Parameterized.class)
public class CloseTest extends ContextTestBase {

    private String path = TEMPLATE_CONTEXT_PATH;

    /**
     * @param transport The transport identifier.
     */
    public CloseTest(final int transport) {
        super(transport);
    }

    /**
     * Set up servlet test.
     *
     * @throws Exception If anything fails.
     */
    @Before
    public void setUp() throws Exception {
        this.path += "/" + getTransport(false);
    }

    /**
     * Successfully test closing a context.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOmContextClose() throws Exception {

        Document context = EscidocRestSoapTestBase.getTemplateAsDocument(this.path, "context_create.xml");
        substitute(context, "/context/properties/name", getUniqueName("PubMan Context "));
        String template = toString(context, false);
        String created = create(template);
        assertXmlValidContext(created);

        Document createdDoc = EscidocRestSoapTestBase.getDocument(created);
        String id = getObjidValue(createdDoc);
        String lastModified = getLastModificationDateValue(createdDoc);

        // open Context
        String resultXml = open(id, getTaskParam(lastModified));
        String opened = retrieve(id);
        assertXmlValidResult(resultXml);
        Document resultDoc = EscidocRestSoapTestBase.getDocument(resultXml);
        String lmdResultOpen = getLastModificationDateValue(resultDoc);

        createdDoc = EscidocRestSoapTestBase.getDocument(opened);
        lastModified = getLastModificationDateValue(createdDoc);

        // close Context
        resultXml = close(id, getTaskParam(lastModified));
        assertXmlValidResult(resultXml);
        resultDoc = EscidocRestSoapTestBase.getDocument(resultXml);
        String lmdResultClose = getLastModificationDateValue(resultDoc);

        assertTimestampIsEqualOrAfter("Wrong timestamp after close, update", lmdResultClose, lmdResultOpen);

        String closed = retrieve(id);
        Document closedDoc = EscidocRestSoapTestBase.getDocument(closed);
        assertXmlValidContext(closed);

        assertXmlEquals("Context closing error: Wrong status!", closed, "/context/properties/public-status",
            CONTEXT_STATUS_CLOSED);
        assertNotEquals("Comment not changed", selectSingleNode(createdDoc,
            "/context/properties/public-status-comment/text()").getNodeValue(), selectSingleNode(closedDoc,
            "/context/properties/public-status-comment/text()").getNodeValue());
        assertTimestampIsEqualOrAfter("Context opening error: last-modification-date has wrong value!",
            getLastModificationDateValue(EscidocRestSoapTestBase.getDocument(closed)), lastModified);

        assertCreatedBy("created-by not as expected!", createdDoc, closedDoc);
        assertModifiedBy("modified-by not as expected!", createdDoc, closedDoc);
    }

    /**
     * Test closing a context and create new Items in this Context.
     *
     * @throws Exception If anything fails.
     */
    @Test(expected = InvalidStatusException.class)
    public void testOmContextClose2() throws Exception {

        Document context = EscidocRestSoapTestBase.getTemplateAsDocument(this.path, "context_create.xml");
        substitute(context, "/context/properties/name", getUniqueName("PubMan Context "));
        String template = toString(context, false);
        String created = create(template);
        assertXmlValidContext(created);
        Document createdDoc = EscidocRestSoapTestBase.getDocument(created);
        String id = getObjidValue(createdDoc);
        String lastModified = getLastModificationDateValue(createdDoc);
        open(id, getTaskParam(lastModified));
        String opened = retrieve(id);

        createdDoc = EscidocRestSoapTestBase.getDocument(opened);
        lastModified = getLastModificationDateValue(createdDoc);
        close(id, getTaskParam(lastModified));

        String xmlData =
            EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_ITEM_PATH + "/" + getTransport(false),
                "escidoc_item_198_for_create.xml");
        Document itemDoc = EscidocRestSoapTestBase.getDocument(xmlData);

        String contextId = null;
        if (getTransport() == Constants.TRANSPORT_REST) {
            contextId = selectSingleNode(createdDoc, "/context/@href").getNodeValue();
            substitute(itemDoc, "/item/properties/context/@href", contextId);

        }
        else {
            contextId = selectSingleNode(createdDoc, "/context/@objid").getNodeValue();
            substitute(itemDoc, "/item/properties/context/@objid", contextId);
        }

        handleXmlResult(getItemClient().create(toString(itemDoc, true)));

    }

    /**
     * Test closing a context with exising items, close the Context and alter the Items afterwards.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOmContextClose3() throws Exception {

        Document context = EscidocRestSoapTestBase.getTemplateAsDocument(this.path, "context_create.xml");
        substitute(context, "/context/properties/name", getUniqueName("PubMan Context "));
        String template = toString(context, false);
        String created = create(template);
        assertXmlValidContext(created);
        Document createdDoc = EscidocRestSoapTestBase.getDocument(created);
        String contextId = getObjidValue(createdDoc);
        String lastModified = getLastModificationDateValue(createdDoc);
        open(contextId, getTaskParam(lastModified));
        String opened = retrieve(contextId);

        String xmlData =
            EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_ITEM_PATH + "/" + getTransport(false),
                "escidoc_item_198_for_create.xml");
        Document itemDoc = EscidocRestSoapTestBase.getDocument(xmlData);

        String contextIdRef = null;
        if (getTransport() == Constants.TRANSPORT_REST) {
            contextIdRef = selectSingleNode(createdDoc, "/context/@href").getNodeValue();
            substitute(itemDoc, "/item/properties/context/@href", contextIdRef);

        }
        else {
            contextIdRef = selectSingleNode(createdDoc, "/context/@objid").getNodeValue();
            substitute(itemDoc, "/item/properties/context/@objid", contextIdRef);
        }

        String itemXml = handleXmlResult(getItemClient().create(toString(itemDoc, true)));
        itemDoc = EscidocRestSoapTestBase.getDocument(itemXml);
        String itemId = getObjidValue(itemDoc);

        // close Context ------
        createdDoc = EscidocRestSoapTestBase.getDocument(retrieve(contextId));
        lastModified = getLastModificationDateValue(createdDoc);
        close(contextId, getTaskParam(lastModified));

        // alter Item ----
        itemXml = addCtsElement(itemXml);
        itemXml = handleXmlResult(getItemClient().update(itemId, itemXml));

        // check Item ----
    }

    /**
     * Add something to the Item.
     *
     * @return new XML representation of Item
     */
    private String addCtsElement(String xml) throws Exception {
        Document doc = EscidocRestSoapTestBase.getDocument(xml);
        doc =
            (Document) addAfter(doc, "/item/properties/content-model-specific/nix", createElementNode(doc, null, null,
                "nox", "modified"));
        String newXml = toString(doc, true);
        return newXml;
    }

}
