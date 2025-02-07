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

import de.escidoc.core.test.EscidocRestSoapTestBase;
import de.escidoc.core.test.common.client.servlet.Constants;
import de.escidoc.core.test.common.client.servlet.interfaces.ResourceHandlerClientInterface;
import de.escidoc.core.test.om.OmTestBase;
import org.apache.http.HttpResponse;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test the mock implementation of the context resource.
 *
 * @author Michael Schneider
 */
public class ContextTestBase extends OmTestBase {

    public static final String CONTEXT_TYPE_PUB_MAN = "PubMan";

    public static final String CONTEXT_TYPE_SWB = "SWB";

    /**
     * @param transport The transport identifier.
     */
    public ContextTestBase(final int transport) {
        super(transport);
    }

    /**
     * Successfully creates an context.
     *
     * @param templateName The name of the template to use.
     * @return Returns the XML representation of the created context.
     * @throws Exception Thrown if anything fails.
     */
    protected String createSuccessfully(final String templateName) throws Exception {

        final Document toBeCreatedDocument =
            EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_CONTEXT_PATH, templateName);
        setUniqueValue(toBeCreatedDocument, XPATH_CONTEXT_NAME);
        final String toBeCreatedXml = toString(toBeCreatedDocument, true);

        String createdXml = null;
        try {
            createdXml = create(toBeCreatedXml);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.failException("Create of OU failed. ", e);
        }
        return createdXml;
    }

    /**
     * Successfully creates a specified number of organizational units.
     *
     * @param templateName The name of the template to use.
     * @param number       The number of organizational units that shall be created.
     * @return Returns a string array containing the object ids of the created organizational units.
     * @throws Exception Thrown if anything fails.
     */
    protected String[] createSuccessfully(final String templateName, final int number) throws Exception {

        String[] ret = new String[number];
        for (int i = 0; i < number; i++) {
            final String createdXml = createSuccessfully(templateName);
            final Document createdDocument = EscidocRestSoapTestBase.getDocument(createdXml);
            ret[i] = getObjidValue(getTransport(), createdDocument);
        }
        return ret;
    }

    /**
     * Successfully creates an context with given type.
     *
     * @param templateName The name of the template to use.
     * @param type The type of the context.
     * @return Returns the XML representation of the created context.
     * @throws Exception Thrown if anything fails.
     */
    protected String createSuccessfullyWithType(final String templateName, final String type) throws Exception {

        final Document toBeCreatedDocument =
            EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_CONTEXT_PATH, getTransport(false) + "/"
                + templateName);
        setUniqueValue(toBeCreatedDocument, XPATH_CONTEXT_PROPERTIES_NAME);
        substitute(toBeCreatedDocument, XPATH_CONTEXT_PROPERTIES_TYPE, type);
        final String toBeCreatedXml = toString(toBeCreatedDocument, true);

        String createdXml = null;
        try {
            createdXml = create(toBeCreatedXml);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.failException("Create of Context failed. ", e);
        }
        return createdXml;
    }

    /**
     * Assert that the organizational unit has all required elements.
     *
     * @param toBeAssertedXml         The created/updated organizational unit.
     * @param originalXml             The template organizational unit used to create/update the organizational unit. If
     *                                this parameter is <code>null</code>, no check with the original data is
     *                                performed.
     * @param timestampBeforeCreation A timestamp before the creation has been started. This is used to check the
     *                                creation date.
     * @param timestampBeforeLastModification
     *                                A timestamp before the last modification has been started. This is used to check
     *                                the last modification date.
     * @param assertCreationDate      Flag to indicate if the creation-date and created-by values shall be asserted
     *                                (<code>true</code>) or not (<code>false</code>).
     * @return Returns the document representing the provided xml data.
     * @throws Exception If anything fails.
     */
    public Document assertContext(
        final String toBeAssertedXml, final String originalXml, final String timestampBeforeCreation,
        final String timestampBeforeLastModification, final boolean assertCreationDate) throws Exception {

        assertXmlValidContext(toBeAssertedXml);
        Document document = EscidocRestSoapTestBase.getDocument(toBeAssertedXml);

        // String[] rootValues = assertRootElement("Invalid Context root
        // element. ",
        // document, XPATH_CONTEXT,
        // Constants.CONTEXT_BASE_URI, timestampBeforeCreation);
        // final String id = rootValues[0];

        // TODO enable if pid generation is supported by the framework
        // assertXMLExist("OU error: pid was not
        // set!",
        // created, "/organizational-unit/pid");

        // assert resources (in case of REST)
        // if (getTransport() == Constants.TRANSPORT_REST) {
        // assertReferencingElement("Assert of resources failed. ",
        // document,
        // XPATH_ORGANIZATIONAL_UNIT_RESOURCES, null);
        // assertReferencingElement("Assert of resource children failed. ",
        // document, XPATH_ORGANIZATIONAL_UNIT_RESOURCES + "/"
        // + "children[@href=\"/oum/organizational-unit/" + id
        // + "/resources/children\"]", null);
        // assertReferencingElement("Assert of resource parents failed. ",
        // document, XPATH_ORGANIZATIONAL_UNIT_RESOURCES + "/"
        // + "parents[@href=\"/oum/organizational-unit/" + id
        // + "/resources/parents\"]", null);
        // assertReferencingElement("Assert of resource path-list failed. ",
        // document, XPATH_ORGANIZATIONAL_UNIT_RESOURCES + "/"
        // + "path-list[@href=\"/oum/organizational-unit/" + id
        // + "/resources/path-list\"]", null);
        // }

        // assert properties
        // assertPropertiesElementUnversioned("Asserting OU properties failed.
        // ",
        // document, XPATH_ORGANIZATIONAL_UNIT_PROPERTIES,
        // timestampBeforeCreation);

        // public-status
        // assertXmlExists("OU error: status was not set!", document,
        // XPATH_ORGANIZATIONAL_UNIT_PUBLIC_STATUS);
        // assertXmlEquals("OU error: status was not set!", document,
        // XPATH_ORGANIZATIONAL_UNIT_PUBLIC_STATUS,
        // ORGANIZATIONAL_UNIT_STATUS_OPENED);

        // has-children
        // assertXmlExists("OU error: " + " Missing has-children", document,
        // XPATH_ORGANIZATIONAL_UNIT_HAS_CHILDREN);

        if (originalXml != null) {
            Document originalDocument = EscidocRestSoapTestBase.getDocument(originalXml);

            if (assertCreationDate) {
                final String expectedCreationDate = getCreationDateValue(originalDocument);
                if (expectedCreationDate != null) {

                    // creation-date
                    // assertXmlEquals("OU error: " + "creation date mismatch,
                    // ",
                    // document, XPATH_ORGANIZATIONAL_UNIT_CREATION_DATE,
                    // expectedCreationDate);

                    // created-by
                    assertCreatedBy("OU error: " + "created-by invalid", originalDocument, document);
                }
            }

            // abbreviation
            // assertXmlEquals("OU error: " + "abbreviation mismatch, ",
            // originalDocument, document,
            // XPATH_ORGANIZATIONAL_UNIT_ABBREVIATION);

            // name
            // assertXmlEquals("OU error: " + "name mismatch, ",
            // originalDocument,
            // document, XPATH_ORGANIZATIONAL_UNIT_NAME);

            // uri
            // assertXmlEquals("OU error: " + "uri mismatch, ",
            // originalDocument,
            // document, XPATH_ORGANIZATIONAL_UNIT_URI);

            // type
            // assertXmlEquals("OU error: " + "type mismatch, ",
            // originalDocument,
            // document, XPATH_ORGANIZATIONAL_UNIT_ORGANIZATION_TYPE);

            // description
            // assertXmlEquals("OU error: " + "description mismatch, ",
            // originalDocument, document,
            // XPATH_ORGANIZATIONAL_UNIT_DESCRIPTION);

            // external id
            // assertXmlEquals("OU error: " + "external-id mismatch, ",
            // originalDocument, document,
            // XPATH_ORGANIZATIONAL_UNIT_EXTERNAL_ID);

            // postcode
            // assertXmlEquals("OU error: " + "postcode mismatch, ",
            // originalDocument, document, XPATH_ORGANIZATIONAL_UNIT_POSTCODE);

            // country
            // assertXmlEquals("OU error: " + "country mismatch, ",
            // originalDocument, document, XPATH_ORGANIZATIONAL_UNIT_COUNTRY);

            // region
            // assertXmlEquals("OU error: " + "region mismatch, ",
            // originalDocument, document, XPATH_ORGANIZATIONAL_UNIT_REGION);

            // address
            // assertXmlEquals("OU error: " + "address mismatch, ",
            // originalDocument, document, XPATH_ORGANIZATIONAL_UNIT_ADDRESS);

            // city
            // assertXmlEquals("OU error: " + "city mismatch, ",
            // originalDocument,
            // document, XPATH_ORGANIZATIONAL_UNIT_CITY);

            // telephone
            // assertXmlEquals("OU error: " + "telephone mismatch, ",
            // originalDocument, document, XPATH_ORGANIZATIONAL_UNIT_TELEPHONE);

            // fax
            // assertXmlEquals("OU error: " + "fax mismatch, ",
            // originalDocument,
            // document, XPATH_ORGANIZATIONAL_UNIT_FAX);

            // email
            // assertXmlEquals("OU error: " + "email mismatch, ",
            // originalDocument, document, XPATH_ORGANIZATIONAL_UNIT_EMAIL);

            // longitude
            // assertXmlEquals("OU error: " + "location-longitude mismatch, ",
            // originalDocument, document,
            // XPATH_ORGANIZATIONAL_UNIT_GEO_COORDINATE_LONGITUDE);

            // latitude
            // assertXmlEquals("OU error: " + "location-latitude mismatch, ",
            // originalDocument, document,
            // XPATH_ORGANIZATIONAL_UNIT_GEO_COORDINATE_LATITUDE);
        }

        return document;
    }

    /**
     * @return Returns the contextClient
     */
    @Override
    public ResourceHandlerClientInterface getClient() {

        return getContextClient();
    }

    /**
     * Handle log messages.
     *
     * @param message The log message.
     */
    protected void log(final String message) {
    }

    /**
     * Assert that the created Context has all required elements.
     *
     * @param xmlCreatedContext       The created context.
     * @param xmlTemplateContext      The template context used to create the context.
     * @param timestampBeforeCreation A timestamp before the creation of the context.
     * @throws Exception If anything fails.
     */
    public void assertCreatedContext(
        final String xmlCreatedContext, final String xmlTemplateContext, final String timestampBeforeCreation)
        throws Exception {

        assertContext(xmlCreatedContext, xmlTemplateContext, timestampBeforeCreation, timestampBeforeCreation);
    }

    /**
     * Assert that the retrieved Context has all required elements.
     *
     * @param toBeAssertedXml         The context xml data that shall be asserted.
     * @param xmlTemplateContext      The template context used to create the context.
     * @param timestampBeforeCreation A timestamp before the creation of the context.
     * @param timestampBeforeLastModification
     *                                A timestamp before the last modification of the context.
     * @throws Exception If anything fails.
     */
    public void assertContext(
        final String toBeAssertedXml, final String xmlTemplateContext, final String timestampBeforeCreation,
        final String timestampBeforeLastModification) throws Exception {

        /**
         * nearly identical method exists in EscidocTestBase
         * assertXmlCreatedContext()
         */

        final String msg = "Asserting retrieved context failed. ";

        assertXmlValidContext(toBeAssertedXml);

        Document toBeAssertedDocument = EscidocRestSoapTestBase.getDocument(toBeAssertedXml);
        Document template = EscidocRestSoapTestBase.getDocument(xmlTemplateContext);

        // assert root element
        // String[] values =
        assertRootElement(msg + "Root element failed. ", toBeAssertedDocument, XPATH_CONTEXT,
            Constants.CONTEXT_BASE_URI, timestampBeforeLastModification);
        // final String id = values[0];
        // title
        // assertXmlEquals("Context error: Context title was changed!",
        // template,
        // toBeAssertedDocument, XPATH_CONTEXT + PART_XLINK_TITLE);

        // assert common properties (created-by, creation-date,
        // modified-by)
        assertPropertiesElementUnversioned("Properties failed. ", toBeAssertedDocument, XPATH_CONTEXT_PROPERTIES,
            timestampBeforeCreation);

        // assert properties
        assertXmlEquals("Context error: name was changed!", template, toBeAssertedDocument,
            XPATH_CONTEXT_PROPERTIES_NAME);
        // assertXmlEquals("Context creation error: description was changed!",
        // template, toBeAssertedDocument,
        // XPATH_CONTEXT_PROPERTIES_DESCRIPTION);
        NodeList expectedDescription = selectNodeList(template, XPATH_CONTEXT_PROPERTIES_DESCRIPTION);
        NodeList toBeAssertedDescription = selectNodeList(toBeAssertedDocument, XPATH_CONTEXT_PROPERTIES_DESCRIPTION);
        String message = "Context creation error: description was changed!";
        if (expectedDescription.getLength() > 0) {
            String expDescription = expectedDescription.item(0).getTextContent();
            if ((expDescription != null) && (!expDescription.equals(""))) {
                assertXmlEquals(message, template, toBeAssertedDocument, XPATH_CONTEXT_PROPERTIES_DESCRIPTION);
            }
            else if (toBeAssertedDescription.getLength() > 0) {
                String toAssertDescription = toBeAssertedDescription.item(0).getTextContent();
                if (toAssertDescription != null) {
                    if (!toAssertDescription.equals("")) {
                        throw new Exception(message);
                    }
                }
            }
        }
        else {
            if (toBeAssertedDescription.getLength() > 0) {
                String toAssertDescription = toBeAssertedDescription.item(0).getTextContent();
                if (toAssertDescription != null) {
                    if (!toAssertDescription.equals("")) {
                        throw new Exception(message);
                    }
                }
            }
        }

        // assert resources. This must not exist, as it is not defined in SOAP
        // and it is empty and therefore must not exists in REST, too.
        // FIXME now exists resources for context in REST case!
        // assertXmlNotExists(msg + "Unexpected resources. ",
        // toBeAssertedDocument, XPATH_CONTEXT_RESOURCES);

        // // admin-descriptors
        // NodeList expectedAdminDesc =
        // selectNodeList(template, XPATH_CONTEXT_ADMIN_DESCRIPTOR);
        // NodeList toBeAssertedADs =
        // selectNodeList(toBeAssertedDocument,
        // XPATH_CONTEXT_ADMIN_DESCRIPTOR);
        // int length = expectedAdminDesc.getLength();
        // assertEquals(msg + "Number of AdminDescriptors different. ", length,
        // toBeAssertedADs.getLength());
        //        
        // for (int i = 0; i < length; i++) {
        // final String adminDescriptorXpath =
        // XPATH_CONTEXT_ADMIN_DESCRIPTOR + "[" + (i + 1) + "]";
        // assertXmlEquals(
        // "Context error: admin-descriptor description was changed!",
        // template, toBeAssertedDocument, adminDescriptorXpath);
        // }

        // OUS
        NodeList expectedOus = selectNodeList(template, XPATH_CONTEXT_PROPERTIES_ORGANIZATIONAL_UNIT);
        NodeList toBeAssertedOus = selectNodeList(toBeAssertedDocument, XPATH_CONTEXT_PROPERTIES_ORGANIZATIONAL_UNIT);
        int length = expectedOus.getLength();
        assertEquals(msg + "Number of OUs different. ", length, toBeAssertedOus.getLength());
        for (int i = 0; i < length; i++) {
            final String ouXpath = XPATH_CONTEXT_PROPERTIES_ORGANIZATIONAL_UNIT + "[" + (i + 1) + "]";
            assertReferencingGroupElements(msg + "Asserting " + (i + 1) + ". OU. ", template, toBeAssertedDocument,
                ouXpath, XPATH_CONTEXT_PROPERTIES_ORGANIZATIONAL_UNIT, Constants.ORGANIZATIONAL_UNIT_BASE_URI);
        }
    }

    /**
     * Test retrieving all contexts matching the filter criteria.
     *
     * @param filter The filters to select the contexts.
     * @return The context list.
     * @throws Exception If anything fails.
     */
    protected String retrieveContexts(final Map<String, String[]> filter) throws Exception {

        return handleXmlResult(getContextClient().retrieveContexts(filter));
    }

    /**
     * Retrieve the xml representation of all admin-descriptors of the context.
     *
     * @param id The id of the context.
     * @return The updated context.
     * @throws Exception If anything fails.
     */
    protected String retrieveAdminDescriptors(final String id) throws Exception {

        return handleXmlResult(getContextClient().retrieveAdminDescriptors((String) checkParameter(id)));
    }

    /**
     * Retrieve the xml representation of all members of the context matching the filter criteria.
     *
     * @param id     The id of the context.
     * @param filter The filters to select the members.
     * @return The members of context (with matching filter criteria).
     * @throws Exception If anything fails.
     */
    protected String retrieveMembers(final String id, final Map<String, String[]> filter) throws Exception {

        return handleXmlResult(getContextClient().retrieveMembers((String) checkParameter(id), filter));
    }

    /**
     * Retrieve the xml representation of an admin descriptor.
     *
     * @param id   The id of the context.
     * @param name The name of the admin-descriptor.
     * @return The XML representation of AdminDescriptor.
     * @throws Exception If anything fails.
     */
    public String retrieveAdminDescriptor(final String id, final String name) throws Exception {

        return handleXmlResult(getContextClient().retrieveAdminDescriptor((String) checkParameter(id),
            (String) checkParameter(name)));
    }

    /**
     * Update the XML an admin descriptor.
     * 
     * @param id
     *            The id of the context.
     * @param name
     *            The name of the admin-descriptor.
     * @param content
     *            The content as XML (string)
     * @return The XML representation of AdminDescriptor.
     * @throws Exception
     *             If anything fails.
     */
    public String updateAdminDescriptor(final String id, final String name, final String content) throws Exception {

        return handleXmlResult(getContextClient().updateAdminDescriptor((String) checkParameter(id),
            (String) checkParameter(name), content));
    }

    /**
     * Retrieve the xml representation of the context properties.
     *
     * @param id The id of the context.
     * @return The XML representation of Properties.
     * @throws Exception If anything fails.
     */
    public String retrieveProperties(final String id) throws Exception {

        return handleXmlResult(getContextClient().retrieveProperties((String) checkParameter(id)));
    }

    /**
     * Open a Context.
     *
     * @param id        The id of the context.
     * @param taskParam The task parameters including the last-modification-date.
     * @return The result XML (result.xsd)
     * @throws Exception If anything fails.
     */
    protected String open(final String id, final String taskParam) throws Exception {

        Object result = getContextClient().open(id, taskParam);
        if (result instanceof HttpResponse) {
            HttpResponse httpRes = (HttpResponse) result;
            assertHttpStatusOfMethod("", httpRes);
        }
        return handleXmlResult(result);
    }

    /**
     * Close a Context.
     *
     * @param id        The id of the context.
     * @param taskParam The task parameters including the last-modification-date.
     * @return The result XML (result.xsd)
     * @throws Exception If anything fails.
     */
    protected String close(final String id, final String taskParam) throws Exception {

        Object result = getContextClient().close(id, taskParam);
        if (result instanceof HttpResponse) {
            HttpResponse httpRes = (HttpResponse) result;
            assertHttpStatusOfMethod("", httpRes);
        }
        return handleXmlResult(result);
    }

    /**
     * Set param == null in case of REST to empty string.
     *
     * @param param .
     * @return .
     */
    protected Object checkParameter(final String param) {
        if ((getTransport() == Constants.TRANSPORT_REST) && (param == null)) {
            return "";
        }
        return param;
    }

}
