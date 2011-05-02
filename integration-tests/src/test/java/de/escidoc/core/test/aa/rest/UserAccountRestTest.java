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
package de.escidoc.core.test.aa.rest;

import de.escidoc.core.common.exceptions.remote.application.invalid.XmlSchemaValidationException;
import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.remote.application.notfound.UserAccountNotFoundException;
import de.escidoc.core.test.EscidocRestSoapTestBase;
import de.escidoc.core.test.aa.UserAccountTest;
import de.escidoc.core.test.common.client.servlet.Constants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Testsuite for the UserAccount with REST transport.
 *
 * @author Torsten Tetteroo
 */
@RunWith(JUnit4.class)
public class UserAccountRestTest extends UserAccountTest {

    /**
     * Constructor.
     *
     * @throws Exception e
     */
    public UserAccountRestTest() throws Exception {
        super(Constants.TRANSPORT_REST);
    }

    /**
     * Test declining creation of UserAccount with set attribute objid in XML data.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testAACua4_3_rest() throws Exception {

        Document newUserAccountDoc =
            EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH,
                "escidoc_useraccount_for_create.xml");
        Attr newNode = createAttributeNode(newUserAccountDoc, USER_ACCOUNT_NS_URI, null, "objid", "escidoc:1");
        addAttribute(newUserAccountDoc, XPATH_USER_ACCOUNT, newNode);
        insertUniqueLoginName(newUserAccountDoc);
        final String newUserAccountXML = toString(newUserAccountDoc, false);

        try {
            create(newUserAccountXML);
            EscidocRestSoapTestBase.failMissingException(XmlSchemaValidationException.class);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.assertExceptionType(XmlSchemaValidationException.class, e);
        }
    }

    /**
     * Test successful creation of user account with set read-only values.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testAACua10_rest() throws Exception {

        Document createdDocument = createSuccessfully("escidoc_useraccount_for_create_rest_read_only.xml");

        assertEquals("Creation date and last modification date are different. ", assertCreationDateExists("",
            createdDocument), getLastModificationDateValue(createdDocument));

        final String objid = getObjidValue(createdDocument);
        Document retrievedDocument = retrieveSuccessfully(objid);
        assertXmlEquals("Retrieved document not the same like the created one", createdDocument, retrievedDocument);
    }

    /**
     * Test successful update of user account with changed read-only values.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testAAUua7_rest() throws Exception {

        Document createdDocument = createSuccessfully("escidoc_useraccount_for_create.xml");
        final String id = getObjidValue(createdDocument);
        final String createdXml = toString(createdDocument, false);

        // root attributes
        final Document toBeUpdatedDocument =
            (Document) substitute(createdDocument, XPATH_USER_ACCOUNT_XLINK_HREF, "Some Href");
        substitute(toBeUpdatedDocument, XPATH_USER_ACCOUNT_XLINK_TITLE, "Some Title");
        substitute(toBeUpdatedDocument, XPATH_USER_ACCOUNT_XLINK_TYPE, "simple");
        substitute(toBeUpdatedDocument, XPATH_USER_ACCOUNT_XML_BASE, "http://some.base.uri");

        // resources
        substitute(createdDocument, XPATH_USER_ACCOUNT_RESOURCES_XLINK_HREF, "Some Href");
        substitute(toBeUpdatedDocument, XPATH_USER_ACCOUNT_RESOURCES_XLINK_TITLE, "Some Title");
        substitute(toBeUpdatedDocument, XPATH_USER_ACCOUNT_RESOURCES_XLINK_TYPE, "simple");

        // resources.current-grants
        substitute(createdDocument, XPATH_USER_ACCOUNT_CURRENT_GRANTS_XLINK_HREF, "Some Href");
        substitute(toBeUpdatedDocument, XPATH_USER_ACCOUNT_CURRENT_GRANTS_XLINK_TITLE, "Some Title");
        substitute(toBeUpdatedDocument, XPATH_USER_ACCOUNT_CURRENT_GRANTS_XLINK_TYPE, "simple");

        // creation-date
        substitute(toBeUpdatedDocument, XPATH_USER_ACCOUNT_CREATION_DATE, getNowAsTimestamp());

        // created-by
        substitute(toBeUpdatedDocument, XPATH_USER_ACCOUNT_CREATED_BY_XLINK_HREF, "Some Href");
        substitute(toBeUpdatedDocument, XPATH_USER_ACCOUNT_CREATED_BY_XLINK_TITLE, "Some Title");
        // type is fixed to simple, cannot be changed

        // modified-by
        substitute(toBeUpdatedDocument, XPATH_USER_ACCOUNT_MODIFIED_BY_XLINK_HREF, "Some Href");
        substitute(toBeUpdatedDocument, XPATH_USER_ACCOUNT_MODIFIED_BY_XLINK_TITLE, "Some Title");

        // active
        substitute(toBeUpdatedDocument, XPATH_USER_ACCOUNT_ACTIVE, "false");

        final String toBeUpdatedXml = toString(toBeUpdatedDocument, false);
        final String beforeUpdateTimestamp = getNowAsTimestamp();

        String updatedXml = null;
        try {
            updatedXml = update(id, toBeUpdatedXml);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.failException("Updating with changed read only values failed. ", e);
        }
        final Document updatedDocument =
            assertUserAccount(updatedXml, createdXml, startTimestamp, beforeUpdateTimestamp, true);

        final Document retrievedDocument = retrieveSuccessfully(id);
        assertXmlEquals("Retrieved document not the same like the created one", updatedDocument, retrievedDocument);
    }

    /**
     * Test successful update of user account without read-only values.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testAAUua10_rest() throws Exception {

        Document createdDocument = createSuccessfully("escidoc_useraccount_for_create.xml");
        final String id = getObjidValue(createdDocument);
        final String createdXml = toString(createdDocument, false);

        // root attributes
        final Document toBeUpdatedDocument = (Document) deleteAttribute(createdDocument, XPATH_USER_ACCOUNT_XLINK_HREF);
        deleteAttribute(toBeUpdatedDocument, XPATH_USER_ACCOUNT_XLINK_TITLE);
        deleteAttribute(toBeUpdatedDocument, XPATH_USER_ACCOUNT_XLINK_TYPE);
        deleteAttribute(toBeUpdatedDocument, XPATH_USER_ACCOUNT_XML_BASE);

        // resources
        deleteNodes(createdDocument, XPATH_USER_ACCOUNT_RESOURCES);

        // creation-date
        deleteNodes(toBeUpdatedDocument, XPATH_USER_ACCOUNT_CREATION_DATE);

        // created-by
        deleteNodes(toBeUpdatedDocument, XPATH_USER_ACCOUNT_CREATED_BY);

        // modified-by
        deleteNodes(toBeUpdatedDocument, XPATH_USER_ACCOUNT_MODIFIED_BY);

        // active
        deleteNodes(toBeUpdatedDocument, XPATH_USER_ACCOUNT_ACTIVE);

        final String toBeUpdatedXml = toString(toBeUpdatedDocument, false);
        final String beforeUpdateTimestamp = getNowAsTimestamp();

        String updatedXml = null;
        try {
            updatedXml = update(id, toBeUpdatedXml);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.failException("Updating with changed read only values failed. ", e);
        }
        final Document updatedDocument =
            assertUserAccount(updatedXml, createdXml, startTimestamp, beforeUpdateTimestamp, true);

        final Document retrievedDocument = retrieveSuccessfully(id);
        assertXmlEquals("Retrieved document not the same like the created one", updatedDocument, retrievedDocument);
    }

    /**
     * Test successfully updating an UserAccount without providing modified-by xlink title.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testAAUua11_rest() throws Exception {

        Document toBeUpdatedDocument = createSuccessfully("escidoc_useraccount_for_create.xml");
        final String createdXml = toString(toBeUpdatedDocument, false);
        deleteAttribute(toBeUpdatedDocument, XPATH_USER_ACCOUNT_MODIFIED_BY, XLINK_PREFIX_ESCIDOC + ":" + NAME_TITLE);
        final String id = getObjidValue(toBeUpdatedDocument);

        final String toBeUpdatedXml = toString(toBeUpdatedDocument, false);
        final String beforeUpdateTimestamp = getNowAsTimestamp();

        String updatedXml = null;
        try {
            updatedXml = update(id, toBeUpdatedXml);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.failException("Updating with changed read only values failed. ", e);
        }
        final Document updatedDocument =
            assertUserAccount(updatedXml, createdXml, startTimestamp, beforeUpdateTimestamp, true);

        Document retrievedDocument = retrieveSuccessfully(id);
        assertXmlEquals("Retrieved document not the same like the created one", updatedDocument, retrievedDocument);

    }

    /**
     * Test declining updating an UserAccount without providing modified-by xlink type.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testAAUua11_2_rest() throws Exception {

        Document toBeUpdatedDocument = createSuccessfully("escidoc_useraccount_for_create.xml");
        final String createdXml = toString(toBeUpdatedDocument, false);
        deleteAttribute(toBeUpdatedDocument, XPATH_USER_ACCOUNT_MODIFIED_BY, XLINK_PREFIX_ESCIDOC + ":" + NAME_TYPE);
        final String id = getObjidValue(toBeUpdatedDocument);

        final String toBeUpdatedXml = toString(toBeUpdatedDocument, false);
        final String beforeUpdateTimestamp = getNowAsTimestamp();

        String updatedXml = null;
        try {
            updatedXml = update(id, toBeUpdatedXml);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.failException("Updating with changed read only values failed. ", e);
        }
        final Document updatedDocument =
            assertUserAccount(updatedXml, createdXml, startTimestamp, beforeUpdateTimestamp, true);

        Document retrievedDocument = retrieveSuccessfully(id);
        assertXmlEquals("Retrieved document not the same like the created one", updatedDocument, retrievedDocument);

    }

    /**
     * Test declining updating an UserAccount without providing modified-by xlink href.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testAAUua11_3_rest() throws Exception {

        Document toBeUpdatedDocument = createSuccessfully("escidoc_useraccount_for_create.xml");
        final String createdXml = toString(toBeUpdatedDocument, false);
        deleteAttribute(toBeUpdatedDocument, XPATH_USER_ACCOUNT_MODIFIED_BY, XLINK_PREFIX_ESCIDOC + ":" + NAME_HREF);
        final String id = getObjidValue(toBeUpdatedDocument);

        final String toBeUpdatedXml = toString(toBeUpdatedDocument, false);
        final String beforeUpdateTimestamp = getNowAsTimestamp();

        String updatedXml = null;
        try {
            updatedXml = update(id, toBeUpdatedXml);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.failException("Updating with changed read only values failed. ", e);
        }
        final Document updatedDocument =
            assertUserAccount(updatedXml, createdXml, startTimestamp, beforeUpdateTimestamp, true);

        Document retrievedDocument = retrieveSuccessfully(id);
        assertXmlEquals("Retrieved document not the same like the created one", updatedDocument, retrievedDocument);

    }

    /**
     * Test successful retrieval of the resources of an UserAccount.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testAARvr1_rest() throws Exception {

        Document createdDocument = createSuccessfully("escidoc_useraccount_for_create.xml");
        String objid = getObjidValue(createdDocument);

        String resourcesXml = null;
        try {
            resourcesXml = retrieveResources(objid);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.failException(e);
        }
        assertNotNull("No data from retrieveResources. ", resourcesXml);
        assertXmlValidUserAccount(resourcesXml);
        Document resourcesDocument = EscidocRestSoapTestBase.getDocument(resourcesXml);
        assertXmlExists("No resources element", resourcesDocument, XPATH_RESOURCES);
        assertXmlExists("no resources xlink:type", resourcesDocument, XPATH_RESOURCES_XLINK_TYPE);
        assertXmlExists("no resources xlink:title", resourcesDocument, XPATH_RESOURCES_XLINK_TITLE);
        assertXmlExists("no resources xlink:href", resourcesDocument, XPATH_RESOURCES_XLINK_HREF);
        assertXmlExists("no resources xml:base", resourcesDocument, XPATH_RESOURCES_BASE);

        assertXmlExists("No current grants resource element", resourcesDocument, XPATH_RESOURCES_CURRENT_GRANTS);
        assertXmlEquals("", resourcesDocument, XPATH_RESOURCES_CURRENT_GRANTS_XLINK_TITLE, "Current Grants");
        assertXmlExists("no current grants xlink:type", resourcesDocument, XPATH_RESOURCES_CURRENT_GRANTS_XLINK_TYPE);
        assertXmlExists("no current grants xlink:href", resourcesDocument, XPATH_RESOURCES_CURRENT_GRANTS_XLINK_HREF);

        Node resourcesNode = selectSingleNode(resourcesDocument, XPATH_RESOURCES);
        final NodeList childNodes = resourcesNode.getChildNodes();
        // expected nodes = 3: text: current-grants, preferences, attributes
        assertEquals("Unexpected number of children of resources, ", 4, childNodes.getLength());
    }

    /**
     * Test declining the retrieval of the resources of an UserAccount with unknown id.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testAARvr2_rest() throws Exception {

        try {
            retrieveResources(UNKNOWN_ID);
            EscidocRestSoapTestBase.failMissingException(UserAccountNotFoundException.class);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.assertExceptionType("Wrong exception. ", UserAccountNotFoundException.class, e);
        }
    }

    /**
     * Test declining the retrieval of the resources of an UserAccount with id of an existing object of another resource
     * type.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testAARvr2_2_rest() throws Exception {

        try {
            retrieveResources(CONTEXT_ID);
            EscidocRestSoapTestBase.failMissingException(UserAccountNotFoundException.class);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.assertExceptionType("Wrong exception. ", UserAccountNotFoundException.class, e);
        }
    }

    /**
     * Test declining the retrieval of the resources of an UserAccount with missing parameter id.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testAARvr3_rest() throws Exception {
        try {
            retrieveResources(null);
            EscidocRestSoapTestBase.failMissingException(MissingMethodParameterException.class);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.assertExceptionType("Wrong exception. ", MissingMethodParameterException.class, e);
        }
    }

}
