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
package de.escidoc.core.test.om.container;

import de.escidoc.core.test.EscidocRestSoapTestBase;
import org.apache.http.HttpResponse;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URL;

/**
 * Test the mock implementation of the Container resource.
 *
 * @author Michael Schneider
 */
@RunWith(value = Parameterized.class)
public class ContainerReleaseForOaiTest extends ContainerTestBase {

    private String theContainerXml;

    private String theContainerId;

    private String theItemId;

    private String theSubcontainerId;

    /**
     * @param transport The transport identifier.
     */
    public ContainerReleaseForOaiTest(final int transport) {
        super(transport);
    }

    /**
     * Successfully release of 20 containers with sub-container (member).
     * <p/>
     * TODO check the member release procedure
     */
    @Test
    public void testRelease20Mal() throws Exception {
        for (int i = 0; i < 20; i++) {
            if (i > 0) {
                setUp();
            }
            String lmd = null;
            String itemLmd = null;
            String subContainerId = null;

            // prepare a Item child to release
            String resultXml = submitItemHelp(this.theItemId);
            assertXmlValidResult(resultXml);
            String lmd1 = getLastModificationDateValue(getDocument(resultXml));
            itemLmd = prepareItemPid(this.theItemId, lmd1);
            assertTimestampIsEqualOrAfter("Wrong last modification date", itemLmd, lmd1);

            // prepare a Container child to release
            String xmlData = getContainerTemplate("create_container_WithoutMembers_v1.1.xml");
            xmlData = xmlData.replaceAll("<prop:pid>hdl:123/container456</prop:pid>", "");

            xmlData = createContainer(this.theContainerId, xmlData);
            subContainerId = getObjidValue(xmlData);
            String lmdSubCont = getLastModificationDateValue(getDocument(xmlData));

            lmdSubCont = prepareContainerPid(subContainerId, lmdSubCont);
            String param = getTheLastModificationParam(false, this.theContainerId, "", lmdSubCont);
            submit(subContainerId, param);

            // prepare the Container it self to release
            param = getTheLastModificationParam(false, theContainerId, "");
            resultXml = submit(theContainerId, param);
            assertXmlValidResult(resultXml);
            lmd = getLastModificationDateValue(getDocument(resultXml));

            // release the Container
            String containerLmd = getTheLastModificationDate(this.theContainerId);
            containerLmd = prepareContainerPid(this.theContainerId, containerLmd);
            param = getTheLastModificationParam(false, this.theContainerId, "", containerLmd);
            resultXml = release(theContainerId, param);
            assertXmlValidResult(resultXml);
            lmd = getLastModificationDateValue(getDocument(resultXml));
            assertTimestampIsEqualOrAfter("Wrong last modification date", lmd, containerLmd);

            // check the Container and children
            String containerXml = retrieve(theContainerId);
            assertXmlEquals("Container Status not as expected", EscidocRestSoapTestBase.getDocument(containerXml),
                "/container/properties/public-status", "released");

            String subContainerXml = retrieve(subContainerId);
            assertXmlEquals("Container Status not as expected", EscidocRestSoapTestBase.getDocument(subContainerXml),
                "/container/properties/public-status", "released");
            if (i > 0) {
                tearDown();
            }
        }

    }

    /**
     * Set up servlet test.
     *
     * @throws Exception If anything fails.
     */
    @Before
    public void setUp() throws Exception {
        this.theItemId = createItemFromTemplate("escidoc_item_198_for_create.xml");

        String xmlData = getContainerTemplate("create_container_WithoutMembers_v1.1.xml");
        theContainerXml = create(xmlData);

        this.theSubcontainerId = getObjidValue(theContainerXml);

        String xmlData1 = getContainerTemplate("create_container_v1.1-forItemAndforContainer.xml");

        String xmlWithItem = xmlData1.replaceAll("##ITEMID##", theItemId);
        String xmlWithItemAndContainer = xmlWithItem.replaceAll("##CONTAINERID##", theSubcontainerId);
        theContainerXml = create(xmlWithItemAndContainer);

        this.theContainerId = getObjidValue(theContainerXml);

    }

    /**
     * Clean up after test.
     *
     * @throws Exception If anything fails.
     */
    @Override
    @After
    public void tearDown() throws Exception {

        super.tearDown();
        theContainerXml = null;

        theContainerId = null;

        theSubcontainerId = null;
        // TODO purge object from Fedora
    }

    /**
     * Submit the Item. the Item is retrieved before call submit method to determine the last-modification-date.
     *
     * @param itemId The id of the item.
     * @return The return value of the submit method.
     * @throws Exception Thrown if submitting failed.
     */
    private String submitItemHelp(final String itemId) throws Exception {

        String lmd =
            getLastModificationDateValue(EscidocRestSoapTestBase.getDocument(handleXmlResult(getItemClient().retrieve(
                itemId))));

        return submitItemHelp(itemId, lmd);
    }

    /**
     * Submit the Item.
     *
     * @param itemId The id of the item.
     * @param lmd    The last-modification-date of the Item.
     * @return The return value of the submit method.
     * @throws Exception Thrown if submitting failed.
     */
    private String submitItemHelp(final String itemId, final String lmd) throws Exception {

        String param = getTaskParam(lmd);

        Object result = getItemClient().submit(itemId, param);
        if (result instanceof HttpResponse) {
            HttpResponse httpRes = (HttpResponse) result;
            assertHttpStatusOfMethod("", httpRes);
        }

        return handleXmlResult(result);
    }

}
