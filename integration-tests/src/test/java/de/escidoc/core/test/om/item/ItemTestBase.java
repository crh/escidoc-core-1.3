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
package de.escidoc.core.test.om.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.escidoc.core.test.EscidocRestSoapTestBase;
import de.escidoc.core.test.common.client.servlet.Constants;
import de.escidoc.core.test.common.client.servlet.interfaces.ResourceHandlerClientInterface;
import de.escidoc.core.test.common.resources.BinaryContent;
import de.escidoc.core.test.om.OmTestBase;
import etm.core.monitor.EtmPoint;

/**
 * Test the mock implementation of the item resource.
 *
 * @author Michael Schneider
 */
public class ItemTestBase extends OmTestBase {

    private static final String ITEM_URL = "http://localhost:8080/ir/item/";

    /**
     * @return Returns the itemClient
     */
    @Override
    public ResourceHandlerClientInterface getClient() {

        return getItemClient();
    }

    /**
     * @param transport The transport identifier.
     */
    public ItemTestBase(final int transport) {
        super(transport);
    }

    /**
     * Get a Item template. The template is pulled automatically from the rest/soap directory of the container template
     * basedir.
     *
     * @param templateName The name of the Item template (file).
     * @return The String representation of the template.
     * @throws Exception Thrown if anything fails.
     */
    public String getItemTemplate(final String templateName) throws Exception {

        return EscidocRestSoapTestBase
            .getTemplateAsString(TEMPLATE_ITEM_PATH + "/" + getTransport(false), templateName);

    }

    /**
     * Adds a content relation to an item.
     *
     * @param id        The ID of the item.
     * @param taskParam The param required to add the relation.
     * @return The response representation.
     * @throws Exception If an error occurs.
     */
    public String addContentRelations(final String id, final String taskParam) throws Exception {
        Object result = getItemClient().addContentRelations(id, taskParam);
        if (result instanceof HttpResponse) {
            HttpResponse httpRes = (HttpResponse) result;
            assertHttpStatusOfMethod("", httpRes);
        }
        return handleXmlResult(result);
    }

    /**
     * Remove a content relation from an item.
     *
     * @param id        The ID of the item.
     * @param taskParam The param required to remove the relation.
     * @return The response representation.
     * @throws Exception If an error occures.
     */
    public String removeContentRelations(final String id, final String taskParam) throws Exception {

        Object result = getItemClient().removeContentRelations(id, taskParam);
        if (result instanceof HttpResponse) {
            HttpResponse httpRes = (HttpResponse) result;
            assertHttpStatusOfMethod("", httpRes);
        }
        return handleXmlResult(result);
    }

    /**
     * Test retrieving the properties of an Item of the mock framework.
     *
     * @param id The id of the item.
     * @return The retrieved properties.
     * @throws Exception If anything fails.
     */
    public String retrieveProperties(final String id) throws Exception {

        return handleXmlResult(getItemClient().retrieveProperties(id));
    }

    /**
     * Test retrieving the metadata records of an Item of the mock framework.
     *
     * @param id The id of the item.
     * @return The retrieved components.
     * @throws Exception If anything fails.
     */
    public String retrieveComponents(final String id) throws Exception {

        return handleXmlResult(getItemClient().retrieveComponents(id));
    }

    /**
     * Retrieve the properties of a component.
     *
     * @param id          The id of the item.
     * @param componentId The id of the component.
     * @return The properties of the component.
     * @throws Exception If anything fails.
     */
    public String retrieveComponentProperties(final String id, final String componentId) throws Exception {

        return handleXmlResult(getItemClient().retrieveComponentProperties(id, componentId));
    }

    /**
     * Test retrieving the metadata records of an Item of the mock framework.
     *
     * @param id          The id of the item.
     * @param componentId The id of the component.
     * @return The retrieved components.
     * @throws Exception If anything fails.
     */
    public String retrieveComponent(final String id, final String componentId) throws Exception {

        EtmPoint point = ETM_MONITOR.createPoint("EscidocTestBase:retrieveComponent");
        try {

            return handleXmlResult(getItemClient().retrieveComponent(id, componentId));
        }
        finally {
            point.collect();
        }
    }

    /**
     * Retrieves item content via REST.
     *
     * @param id          The ID of the item.
     * @param componentId The ID of the component inside the item.
     * @return The content representation.
     * @throws Exception If an error occures.
     */
    public String retrieveContentRest(final String id, final String componentId) throws Exception {
        int transport = getItemClient().getTransport();

        String result = null;
        try {
            getItemClient().setTransport(Constants.TRANSPORT_REST);

            result = retrieveContent(id, componentId);
        }
        catch (final Exception e) {
            throw e;
        }
        finally {
            getItemClient().setTransport(transport);
        }

        return result;
    }

    /**
     * Retrieve transformed Content of an Item.
     *
     * @param id                    The ID of the item.
     * @param componentId           The ID of the component inside the item.
     * @param transformationService Transformation Service.
     * @param transformParam        Transformation param.
     * @return The transformed content.
     * @throws Exception If an error occures.
     */
    public String retrieveContent(
        final String id, final String componentId, final String transformationService, final String transformParam)
        throws Exception {
        Object result = getItemClient().retrieveContent(id, componentId, transformationService, transformParam);
        String xmlResult = null;
        if (result instanceof HttpResponse) {
            HttpResponse httpRes = (HttpResponse) result;
            assertHttpStatusOfMethod("", httpRes);
            xmlResult = EntityUtils.toString(httpRes.getEntity(), HTTP.UTF_8);

        }
        else if (result instanceof String) {
            xmlResult = (String) result;
        }
        return xmlResult;
    }

    /**
     * Retrieve the method response body as stream. (REST)
     *
     * @param id          The id of the Item.
     * @param componentId The id of the Component.
     * @return InputStream of the binary content
     * @throws Exception Thrown in case of failure.
     */
    public BinaryContent retrieveBinaryContent(final String id, final String componentId) throws Exception {

        getItemClient().setTransport(Constants.TRANSPORT_REST);

        Object result = getItemClient().retrieveContent(id, componentId);

        BinaryContent binContent = null;

        if (result instanceof HttpResponse) {
            HttpResponse httpRes = (HttpResponse) result;

            if (httpRes.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
                binContent = new BinaryContent();
                binContent.setContent(httpRes.getEntity().getContent());
                Header contentType = httpRes.getFirstHeader("Content-Type");
                if (contentType != null) {
                    binContent.setMimeType(contentType.getValue());
                }
                // binContent.setFileName(fileName);
            }
        }

        return binContent;
    }

    /**
     * Retrieve the method response body as stream. (REST)
     *
     * @param id                    The id of the Item.
     * @param componentId           The id of the Component.
     * @param transformationService The transformation service.
     * @param transformParam        The transformation parameter.
     * @return InputStream of the binary content
     * @throws Exception Thrown in case of failure.
     */
    public BinaryContent retrieveBinaryContent(
        final String id, final String componentId, final String transformationService, final String transformParam)
        throws Exception {

        getItemClient().setTransport(Constants.TRANSPORT_REST);

        Object result = getItemClient().retrieveContent(id, componentId, transformationService, transformParam);

        BinaryContent binContent = null;

        if (result instanceof HttpResponse) {
            HttpResponse httpRes = (HttpResponse) result;

            if (httpRes.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
                binContent = new BinaryContent();
                binContent.setContent(httpRes.getEntity().getContent());
                Header contentType = httpRes.getFirstHeader("Content-Type");
                binContent.setMimeType(contentType.getValue());
                // binContent.setFileName(fileName);
            }
        }

        return binContent;
    }

    /**
     * Retrieves the Content from an Items component.
     *
     * @param id          The ID of the item.
     * @param componentId The ID of the component inside the item.
     * @return The content representation.
     * @throws Exception If an error occures.
     */
    public String retrieveContent(final String id, final String componentId) throws Exception {
        Object result = getItemClient().retrieveContent(id, componentId);
        String xmlResult = null;
        if (result instanceof HttpResponse) {
            HttpResponse httpRes = (HttpResponse) result;
            assertHttpStatusOfMethod("", httpRes);
            xmlResult = EntityUtils.toString(httpRes.getEntity(), HTTP.UTF_8);

        }
        else if (result instanceof String) {
            xmlResult = (String) result;
        }
        return xmlResult;
    }

    /**
     * Test retrieving the lost of Item IDs.
     *
     * @param filter TODO
     * @return The retrieved components.
     * @throws Exception If anything fails.
     */
    public String retrieveItems(final Map<String, String[]> filter) throws Exception {

        return handleXmlResult(getItemClient().retrieveItems(filter));
    }

    /**
     * Test deleting a component of an Item of the mock framework.
     *
     * @param id          The id of the item.
     * @param componentId The id of the component.
     * @throws Exception If anything fails.
     */
    public void deleteComponent(final String id, final String componentId) throws Exception {

        getItemClient().deleteComponent(id, componentId);
    }

    /**
     * Test updating a component of an Item of the mock framework.
     *
     * @param id           The id of the item.
     * @param componentId  The id of the component.
     * @param componentXml The xml representation of the component.
     * @return The updated components.
     * @throws Exception If anything fails.
     */
    public String updateComponent(final String id, final String componentId, final String componentXml)
        throws Exception {

        return handleXmlResult(getItemClient().updateComponent(id, componentId, componentXml));
    }

    /**
     * Test retrieving the metadata records of an Item of the mock framework.
     *
     * @param id        The id of the item.
     * @param component TThe xml representation of the component.
     * @return The created component.
     * @throws Exception If anything fails.
     */
    public String createComponent(final String id, final String component) throws Exception {

        return handleXmlResult(getItemClient().createComponent(id, component));
    }

    /**
     * Test retrieving the metadata records of an Item of the mock framework.
     *
     * @param id The id of the item.
     * @return The retrieved metadata records.
     * @throws Exception If anything fails.
     */
    public String retrieveMetadataRecords(final String id) throws Exception {

        return handleXmlResult(getItemClient().retrieveMdRecords(id));
    }

    /**
     * Test retrieving the content streams of an Item.
     *
     * @param id The id of the item.
     * @return The retrieved content streams.
     * @throws Exception If anything fails.
     */
    public String retrieveContentStreams(final String id) throws Exception {

        return handleXmlResult(getItemClient().retrieveContentStreams(id));
    }

    /**
     * Test retrieving the content stream content of an Item.
     *
     * @param id                The id of the item.
     * @param contentStreamName The name of the content stream
     * @return The retrieved content streams.
     * @throws Exception If anything fails.
     */
    public String retrieveContentStreamContent(final String id, final String contentStreamName) throws Exception {

        return handleXmlResult(getItemClient().retrieveContentStreamContent(id, contentStreamName));
    }

    /**
     * Test retrieving the content stream of an Item.
     *
     * @param id                The id of the item.
     * @param contentStreamName The name of the content stream
     * @return The retrieved content streams.
     * @throws Exception If anything fails.
     */
    public String retrieveContentStream(final String id, final String contentStreamName) throws Exception {

        return handleXmlResult(getItemClient().retrieveContentStream(id, contentStreamName));
    }

    /**
     * Test retrieving the metadata records of an Item of the mock framework.
     *
     * @param id           The id of the item.
     * @param mdRecordname The name of the md-record.
     * @return The retrieved metadata record.
     * @throws Exception If anything fails.
     */
    public String retrieveMetadataRecord(final String id, final String mdRecordname) throws Exception {

        return handleXmlResult(getItemClient().retrieveMdRecord(id, mdRecordname));
    }

    /**
     * Test updating a metadata record of an Item of the mock framework.
     *
     * @param id           The id of the item.
     * @param mdRecordname The name of the metadata record.
     * @param mdRecord     The updated metadata record.
     * @return The created metadata record.
     * @throws Exception If anything fails.
     */
    public String updateMetadataRecord(final String id, final String mdRecordname, final String mdRecord)
        throws Exception {

        return handleXmlResult(getItemClient().updateMdRecord(id, mdRecordname, mdRecord));
    }

    /**
     * Test retrieving the resources of an Item of the mock framework.
     *
     * @param id The id of the item.
     * @return The retrieved resources.
     * @throws Exception If anything fails.
     */
    @Override
    public String retrieveResources(final String id) throws Exception {

        return handleXmlResult(getItemClient().retrieveResources(id));
    }

    /**
     * Test retrieving the version history of an Item.
     *
     * @param id The id of the item.
     * @return The retrieved version history.
     * @throws Exception If anything fails.
     */
    public String retrieveVersionHistory(final String id) throws Exception {

        return handleXmlResult(getItemClient().retrieveVersionHistory(id));
    }

    /**
     * Test retrieving the relations of an Item.
     *
     * @param id The id of the item.
     * @return The list of relations.
     * @throws Exception If anything fails.
     */
    public String retrieveRelations(final String id) throws Exception {

        return handleXmlResult(getItemClient().retrieveRelations(id));
    }

    /**
     * Test retrieving parents of an Item.
     *
     * @param id The id of the item.
     * @return The retrieved parents.
     * @throws Exception If anything fails.
     */
    protected String retrieveParents(final String id) throws Exception {

        return handleXmlResult(getItemClient().retrieveParents(id));
    }

    /**
     * Test submiting an item.
     *
     * @param id    The id of the item.
     * @param param The param indicating the last-modifiaction-date of the Item.
     * @return result XML with (at least) last modification date of the resource.
     * @throws Exception If anything fails.
     */
    public String submit(final String id, final String param) throws Exception {

        Object result = getItemClient().submit(id, param);
        if (result instanceof HttpResponse) {
            HttpResponse httpRes = (HttpResponse) result;
            assertHttpStatusOfMethod("", httpRes);
        }

        return handleXmlResult(result);
    }

    /**
     * Test releasing an item.
     *
     * @param id    The id of the item.
     * @param param The param indicating the last-modifiaction-date of the Item.
     * @return result XML with (at least) last modification date of the resource.
     * @throws Exception If anything fails.
     */
    public String release(final String id, final String param) throws Exception {

        // return releaseWithPid(id, param);
        return releaseWithoutPid(id, param);
    }

    /**
     * Test releasing an item and assign (if necessary the version and/or object PIDs).
     *
     * @param id The id of the item.
     * @return XML result structure with at least last-modification-date
     * @throws Exception If anything fails.
     */
    public String releaseWithPid(final String id) throws Exception {

        Document itemDoc = null;
        String lmd = null;
        String pidXml = null;

        if (!getItemClient().getPidConfig("cmm.Item.objectPid.releaseWithoutPid", "false")
            || !getItemClient().getPidConfig("cmm.Item.versionPid.releaseWithoutPid", "false")) {
            itemDoc = EscidocRestSoapTestBase.getDocument(retrieve(id));
            lmd = getLastModificationDateValue(itemDoc);
        }

        // assign objectPid
        if (!getItemClient().getPidConfig("cmm.Item.objectPid.releaseWithoutPid", "false")) {
            // prevent re-assigning
            Node pid = selectSingleNode(itemDoc, XPATH_ITEM_OBJECT_PID);
            if (pid == null) {
                String itemId = getObjidWithoutVersion(id);
                String pidParam = getPidParam2(new DateTime(lmd, DateTimeZone.UTC), new URL(ITEM_URL + itemId));
                pidXml = assignObjectPid(id, pidParam);
                assertXmlValidResult(pidXml);
                Document pidDoc = EscidocRestSoapTestBase.getDocument(pidXml);
                lmd = getLastModificationDateValue(pidDoc);
            }
        }

        // assign versionPid
        if (!getItemClient().getPidConfig("cmm.Item.versionPid.releaseWithoutPid", "false")) {

            // prevent re-assigning
            Node pid = selectSingleNode(itemDoc, XPATH_ITEM_VERSION_PID);
            if (pid == null) {
                String versionNumber = getVersionNumber(id);
                String versionId = id;
                if (versionNumber == null) {
                    versionId = getLatestVersionObjidValue(itemDoc);
                }
                String pidParam = getPidParam2(new DateTime(lmd, DateTimeZone.UTC), new URL(ITEM_URL + versionId));
                pidXml = assignVersionPid(versionId, pidParam);
                assertXmlValidResult(pidXml);

                // assignVersionPid does not alter the last-modification-date
                Document pidDoc = EscidocRestSoapTestBase.getDocument(pidXml);
                lmd = getLastModificationDateValue(pidDoc);
            }
        }

        String param = getTaskParam(lmd);

        Object result = getItemClient().release(id, param);
        if (result instanceof HttpResponse) {
            HttpResponse httpRes = (HttpResponse) result;
            assertHttpStatusOfMethod("", httpRes);
        }

        return handleXmlResult(result);
    }

    /**
     * Test releasing an item.
     *
     * @param id    The id of the item.
     * @param param The param indicating the last-modifiaction-date of the Item.
     * @return result XML with (at least) last modification date of the resource.
     * @throws Exception If anything fails.
     */
    public String releaseWithoutPid(final String id, final String param) throws Exception {

        Object result = getItemClient().release(id, param);
        if (result instanceof HttpResponse) {
            HttpResponse httpRes = (HttpResponse) result;
            assertHttpStatusOfMethod("", httpRes);
        }

        return handleXmlResult(result);
    }

    /**
     * Test revising an item.
     *
     * @param id    The id of the item.
     * @param param The param indicating the last-modifiaction-date of the Item.
     * @return result XML with (at least) last modification date of the resource.
     * @throws Exception If anything fails.
     */
    public String revise(final String id, final String param) throws Exception {

        Object result = getItemClient().revise(id, param);
        if (result instanceof HttpResponse) {
            HttpResponse httpRes = (HttpResponse) result;
            assertHttpStatusOfMethod("", httpRes);
        }
        return handleXmlResult(result);
    }

    /**
     * Test withdrawing an item from the mock framework.
     *
     * @param id    The id of the item.
     * @param param The param indicating the last-modifiaction-date of the Item.
     * @return result XML with (at least) last modification date of the resource.
     * @throws Exception If anything fails.
     */
    public String withdraw(final String id, final String param) throws Exception {

        Object result = getItemClient().withdraw(id, param);
        if (result instanceof HttpResponse) {
            HttpResponse httpRes = (HttpResponse) result;
            assertHttpStatusOfMethod("", httpRes);
        }
        return handleXmlResult(result);
    }

    /**
     * Test locking an item in the mock framework.
     *
     * @param id    The id of the item.
     * @param param The param indicating the last-modifiaction-date of the Item.
     * @return result XML with (at least) last modification date of the resource.
     * @throws Exception If anything fails.
     */
    public String lock(final String id, final String param) throws Exception {

        Object result = getItemClient().lock(id, param);
        if (result instanceof HttpResponse) {
            HttpResponse httpRes = (HttpResponse) result;
            assertHttpStatusOfMethod("", httpRes);
        }
        return handleXmlResult(result);
    }

    /**
     * Test unlocking an item in the mock framework.
     *
     * @param id    The id of the item.
     * @param param The param indicating the last-modifiaction-date of the Item.
     * @return result XML with (at least) last modification date of the resource.
     * @throws Exception If anything fails.
     */
    public String unlock(final String id, final String param) throws Exception {

        Object result = getItemClient().unlock(id, param);
        if (result instanceof HttpResponse) {
            HttpResponse httpRes = (HttpResponse) result;
            assertHttpStatusOfMethod("", httpRes);
        }
        return handleXmlResult(result);
    }

    /**
     * Test moving an item to another context in the mock framework.
     *
     * @param id    The id of the item.
     * @param param The param indicating the last-modifiaction-date of the Item.
     * @return The retrieved item.
     * @throws Exception If anything fails.
     */
    public String moveToContext(final String id, final String param) throws Exception {

        return handleXmlResult(getItemClient().moveToContext(id, param));
    }

    /**
     * Add Element to content-model-specific.
     *
     * @param xml The xml to add the element to.
     * @return The xml with new element.
     * @throws Exception If an error occures.
     */
    protected String addCtsElement(final String xml) throws Exception {

        Document doc = EscidocRestSoapTestBase.getDocument(xml);
        doc =
            (Document) addAfter(doc, "/item/properties/content-model-specific/nix", createElementNode(doc, null, null,
                "nox", "modified"));
        String newXml = toString(doc, true);
        return newXml;
    }

    /**
     * Adds an Element to the given XML document.
     *
     * @param xml   A String representing the XML document.
     * @param xPath The XPath identifing the element after that the element should be added.
     * @return A String representing the changed XML document.
     * @throws Exception If an error occures.
     */
    protected String addElement(final String xml, final String xPath) throws Exception {

        Document doc = EscidocRestSoapTestBase.getDocument(xml);
        doc = (Document) addAfter(doc, xPath, createElementNode(doc, null, null, "nox", "modified"));
        String newXml = toString(doc, true);
        return newXml;
    }

    /**
     * Add a Component (from the template tree) to Item.
     *
     * @param itemXml The Item as XML where the Component is to add.
     * @return new Item representation (with new Component)
     * @throws Exception If an error occurs.
     */
    protected String addComponent(final String itemXml) throws Exception {

        // load component template
        Document component =
            EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/" + getTransport(false),
                "component_for_create.xml");

        return addComponent(itemXml, component);
    }

    /**
     * Add a Component to the Item.
     *
     * @param itemXml   The Item, where the Component is to add, as String.
     * @param component The Component, which is to add, as Document.
     * @return The new Item with included Component as String
     * @throws Exception Thrown if merging failed.
     */
    protected String addComponent(final String itemXml, final Document component) throws Exception {
        // FIXME using importNode and adoptNode should not be necessary
        Document curItem = EscidocRestSoapTestBase.getDocument(itemXml);

        Node newComponent = selectSingleNode(component, "/component");
        curItem.importNode(newComponent, true);

        Node components = selectSingleNode(curItem, "/item/components");
        if (components == null) {
            // add first components element
            selectSingleNode(curItem, "/item").appendChild(curItem.createElement("escidocComponents:components"));
            components = selectSingleNode(curItem, "/item/components");
        }

        components.appendChild(curItem.adoptNode(newComponent));

        curItem.normalize();

        String newXml = toString(curItem, false);
        return newXml;
    }

    /**
     * Assign a versionPID to a version of an Item.
     *
     * @param id    The id of the Item. If the id contains no version suffix than is the version PID assigned to the
     *              newest version of the Item.
     * @param param The PID parameter.
     * @return The assignment method response.
     * @throws Exception Thrown if anything fails.
     */
    public String assignVersionPid(final String id, final String param) throws Exception {

        return handleXmlResult(getItemClient().assignVersionPid(id, param));
    }

    /**
     * Assign a objectPID to the Item.
     *
     * @param id    The id of the Item.
     * @param param The PID parameter.
     * @return The assignment method response.
     * @throws Exception Thrown if anything fails.
     */
    public String assignObjectPid(final String id, final String param) throws Exception {

        return handleXmlResult(getItemClient().assignObjectPid(id, param));
    }

    /**
     * Assign a PID to the Content of an Item.
     *
     * @param id          The id of the Item.
     * @param componentId The id of the component.
     * @param param       The PID parameter.
     * @return The assignment method response.
     * @throws Exception Thrown if anything fails.
     */
    public String assignContentPid(final String id, final String componentId, final String param) throws Exception {
        return handleXmlResult(getItemClient().assignContentPid(id, componentId, param));
    }

    /**
     * Assert that the created Item has all required elements.
     *
     * @param xmlCreatedItem          The created item.
     * @param xmlTemplateItem         The template item used to create the context.
     * @param timestampBeforeCreation A timestamp before the creation of the context.
     * @throws Exception If anything fails.
     */
    public void assertCreatedItem(
        final String xmlCreatedItem, final String xmlTemplateItem, final String timestampBeforeCreation)
        throws Exception {

        assertItem(xmlCreatedItem, xmlTemplateItem, timestampBeforeCreation, timestampBeforeCreation);
    }

    /**
     * Assert that the retrieved Item has all required elements.
     *
     * @param toBeAssertedXml         The item xml data that shall be asserted.
     * @param xmlTemplateContext      The template item used to create the context.
     * @param timestampBeforeCreation A timestamp before the creation of the item.
     * @param timestampBeforeLastModification
     *                                A timestamp before the last modification of the item.
     * @throws Exception If anything fails.
     */
    public void assertItem(
        final String toBeAssertedXml, final String xmlTemplateContext, final String timestampBeforeCreation,
        final String timestampBeforeLastModification) throws Exception {

        final String msg = "Asserting retrieved item failed. ";

        assertXmlValidItem(toBeAssertedXml);

        Document toBeAssertedDocument = EscidocRestSoapTestBase.getDocument(toBeAssertedXml);
        // Document template =
        // EscidocRestSoapTestBase.getDocument(xmlTemplateContext);

        // assert root element
        // String[] values =
        assertRootElement(msg + "Root element failed. ", toBeAssertedDocument, XPATH_ITEM, Constants.ITEM_BASE_URI,
            timestampBeforeLastModification);
        // final String id = values[0];

        // assert common properties (created-by, creation-date,
        // modified-by)
        assertPropertiesElement("Properties failed. ", toBeAssertedDocument, XPATH_ITEM_PROPERTIES,
            timestampBeforeCreation);

        // assert properties
        // objectPID
        Node objectPid = selectSingleNode(toBeAssertedDocument, "/item/properties/pid");
        if (objectPid != null) {
            String pid = objectPid.getTextContent();
            if (!pid.startsWith("hdl:")) {
                // next check
                if (pid.startsWith("${")) {
                    throw new Exception("Object PID approximately wrong filled by template");
                }
            }
        }

        // versionPID (current version)
        Node versionPid = selectSingleNode(toBeAssertedDocument, "/item/properties/version/pid");
        if (versionPid != null) {
            String pid = versionPid.getTextContent();
            if (!pid.startsWith("hdl:")) {
                // next check
                if (pid.startsWith("${")) {
                    throw new Exception("Version PID approximately " + "wrong filled by template");
                }
            }
        }

        // latestReleasePID
        Node latestReleasePid = selectSingleNode(toBeAssertedDocument, "/item/properties/latest-release/pid");
        if (latestReleasePid != null) {
            String pid = latestReleasePid.getTextContent();
            if (!pid.startsWith("hdl:")) {
                // next check
                if (pid.startsWith("${")) {
                    throw new Exception("Latest Release PID approximately " + "wrong filled by template");
                }
            }
        }

        // latestVersionPID
        Node latestVersionPid = selectSingleNode(toBeAssertedDocument, "/item/properties/latest-version/pid");
        if (latestVersionPid != null) {
            String pid = latestVersionPid.getTextContent();
            if (!pid.startsWith("hdl:")) {
                // next check
                if (pid.startsWith("${")) {
                    throw new Exception("Latest Version PID approximately " + "wrong filled by template");
                }
            }
        }
    }

    public String[] createItemWithExternalBinaryContent(final String storage) throws Exception {

        Document item =
            getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
        String storageBeforeCreate = storage;
        String urlBeforeCreate = selectSingleNode(item, "/item/components/component[2]/content/@href").getNodeValue();
        Document newItem =
            (Document) substitute(item, "/item/components/component[2]/content/@storage", storageBeforeCreate);
        Node itemWithoutSecondComponent = deleteElement(newItem, "/item/components/component[1]");
        String xmlData = toString(itemWithoutSecondComponent, false);
        // System.out.println("item " + xmlData);
        String theItemXml = create(xmlData);
        String theItemId = getObjidValue(EscidocRestSoapTestBase.getDocument(theItemXml));
        assertXmlValidItem(xmlData);
        String componentId;
        Document createdItem = getDocument(theItemXml);
        if (getTransport(true).equals("REST")) {
            String componentHrefValue =
                selectSingleNode(createdItem, "/item/components/component/@href").getNodeValue();
            componentId = getObjidFromHref(componentHrefValue);
        }
        else {
            componentId = selectSingleNode(createdItem, "/item/components/component/@objid").getNodeValue();
        }
        String urlAfterCreate =
            selectSingleNode(createdItem, "/item/components/component/content/@href").getNodeValue();
        String storageAfterCtreate =
            selectSingleNode(createdItem, "/item/components/component/content/@storage").getNodeValue();
        assertEquals("The attribute 'storage' has a wrong valuue", storageBeforeCreate, storageAfterCtreate);
        // String retrievedItem = retrieve(theItemId);
        // System.out.println("item " + retrievedItem);

        return new String[] { theItemId, componentId };
    }

    /**
     * Prepares an item for a test.<br> The item is created and set into the specified state.
     *
     * @param creatorUserHandle    The eSciDoc user handle of the creator.
     * @param status               The status to set for the item. If this is <code>null</code>, no item is created and
     *                             <code>null</code> is returned.
     * @param contextId            context to create container in
     * @param createVersionsBefore If this flag is set to <code>true</code>, before each status change, the item is
     *                             updated to create a new version.
     * @param createVersionsAfter  If this flag is set to <code>true</code>, after each status change, the item is
     *                             updated to create a new version, if this is allowed. Currently, this is not allowed
     *                             for objects in state release or withdrawn.
     * @return Returns the XML representation of the created item. In case of withdrawn item, the released item is
     *         returned.
     * @throws Exception If anything fails.
     */
    protected String prepareItem(
        final String status, final String contextId, final boolean createVersionsBefore,
        final boolean createVersionsAfter) throws Exception {

        if (status == null) {
            return null;
        }
        String createdXml = null;
        try {
            createdXml = create(prepareItemData(contextId));
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.failException(e);
        }
        assertNotNull(createdXml);

        if (!STATUS_PENDING.equals(status)) {
            Document document = EscidocRestSoapTestBase.getDocument(createdXml);
            final String objidValue = getObjidValue(document);
            if (createVersionsBefore) {
                createdXml = createdXml.replaceAll("Schindlmayr", "Schindlmayr u");
                createdXml = update(objidValue, createdXml);
                document = EscidocRestSoapTestBase.getDocument(createdXml);
            }
            submit(objidValue, getTaskParam(getLastModificationDateValue(document)));
            createdXml = retrieve(objidValue);
            if (createVersionsAfter) {
                createdXml = createdXml.replaceAll("Schindlmayr", "Schindlmayr u");
                createdXml = update(objidValue, createdXml);
            }
            if (!STATUS_SUBMITTED.equals(status)) {
                if (createVersionsBefore) {
                    createdXml = createdXml.replaceAll("Schindlmayr", "Schindlmayr u");
                    createdXml = update(objidValue, createdXml);
                }
                document = EscidocRestSoapTestBase.getDocument(createdXml);
                releaseWithPid(objidValue);
                createdXml = retrieve(objidValue);
                if (!STATUS_RELEASED.equals(status)) {
                    if (createVersionsBefore) {
                        createdXml = createdXml.replaceAll("Schindlmayr", "Schindlmayr u");
                        createdXml = update(objidValue, createdXml);
                    }
                    if (STATUS_WITHDRAWN.equals(status)) {
                        document = EscidocRestSoapTestBase.getDocument(createdXml);
                        final String taskParam =
                            getWithdrawTaskParam(getLastModificationDateValue(document), "Some withdraw comment");
                        withdraw(getObjidValue(document), taskParam);
                        createdXml = retrieve(objidValue);
                    }
                }
            }
        }

        return createdXml;
    }

    /**
     * Prepares the data for an item.
     *
     * @param contextId context to create container in
     * @return Returns the xml representation of an item.
     * @throws Exception If anything fails.
     */
    protected String prepareItemData(final String contextId) throws Exception {

        final String templateName = "escidoc_item_198_for_create.xml";
        Document itemDoc =
            EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/" + getTransport(false), templateName);
        if (contextId != null && !contextId.equals("")) {
            if (getTransport() == Constants.TRANSPORT_REST) {
                String contextHref = Constants.CONTEXT_BASE_URI + "/" + contextId;
                substitute(itemDoc, "/item/properties/context/@href", contextHref);
            }
            else {
                substitute(itemDoc, "/item/properties/context/@objid", contextId);
            }
        }
        return toString(itemDoc, false);
    }

}
