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
package de.escidoc.core.test.om.container.soap;

import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidContentException;
import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidXmlException;
import de.escidoc.core.common.exceptions.remote.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.remote.application.missing.MissingMdRecordException;
import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.remote.application.notfound.ContextNotFoundException;
import de.escidoc.core.common.exceptions.remote.application.notfound.ReferencedResourceNotFoundException;
import de.escidoc.core.common.exceptions.remote.application.notfound.RelationPredicateNotFoundException;
import de.escidoc.core.test.EscidocRestSoapTestBase;
import de.escidoc.core.test.common.client.servlet.Constants;
import de.escidoc.core.test.om.container.ContainerTestBase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Item tests with SOAP transport.
 *
 * @author Michael Schneider
 */
public class ContainerCreateSoapTest extends ContainerTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerCreateSoapTest.class);

    /**
     * Constructor.
     */
    public ContainerCreateSoapTest() {
        super(Constants.TRANSPORT_SOAP);
    }

    /**
     * Test successfully creating container.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOM_CCO_1_1() throws Exception {
        // Document container =
        // getTemplateAsDocument(TEMPLATE_CONTAINER_PATH,
        // "create_container_WithoutMembers_Soapv1.1.xml");
        String container = getContainerTemplate("create_container_WithoutMembers_v1.1.xml");
        // Node containerWithoutAdminDescriptor =
        // deleteElement(container, "/container/admin-descriptor");
        // String containerWithoutAdminDescriptorXml =
        // toString(containerWithoutAdminDescriptor, false);
        // assertXmlValidContainer(containerWithoutAdminDescriptorXml);
        // final String theContainerXml =
        // create(containerWithoutAdminDescriptorXml);
        assertXmlValidContainer(container);
        String theContainerXml = null;
        try {
            theContainerXml = create(container);
        }
        catch (final Exception e) {
            failException(e);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("the container " + theContainerXml);
        }
        assertXmlValidContainer(theContainerXml);

        Document document = EscidocRestSoapTestBase.getDocument(theContainerXml);
        getObjidValue(document);

        Node itemRef = selectSingleNode(document, "/container/struct-map/member-ref-list/item-ref");
        assertNull(itemRef);
        Node containerRef = selectSingleNode(document, "/container/struct-map/member-ref-list/container-ref");
        assertNull(containerRef);

        Node modifiedDate = selectSingleNode(document, "/container/@last-modification-date");
        assertNotNull(modifiedDate);
        assertFalse("modified date is not set", modifiedDate.getTextContent().equals(""));
        assertXmlEquals("status value is wrong", document, "/container/properties/public-status", "pending");
        assertXmlEquals("pid value is wrong", document, "/container/properties/pid", "hdl:123/container456");
        Node createdDate = selectSingleNode(document, "/container/properties/creation-date");
        assertNotNull(createdDate);
        assertFalse("created date is not set", createdDate.getTextContent().equals(""));
        assertXmlEquals("lock-status value is wrong", document, "/container/properties/lock-status", "unlocked");
        assertXmlEquals("current version number is wrong", document, "/container/properties/version/number", "1");
        assertXmlEquals("current version status is wrong", document, "/container/properties/version/status", "pending");
        // assertXmlEquals("current version valid-status is wrong", document,
        // "/container/properties/version/valid-status", "valid");
        assertXmlEquals("current version date is wrong", document, "/container/properties/version/date", modifiedDate
            .getTextContent());
        assertXmlEquals("latest version number is wrong", document, "/container/properties/latest-version/number", "1");
        assertXmlEquals("latest version date is wrong", document, "/container/properties/latest-version/date",
            modifiedDate.getTextContent());

        Node creatorId = selectSingleNode(document, "/container/properties/created-by/@objid");

        assertNotNull(creatorId.getTextContent());
        // Node toc = selectSingleNode(document, "/container/toc");
        // assertNull(toc);
        // Node adminDescriptor =
        // selectSingleNode(document, "/container/admin-descriptor");
        // assertNull(adminDescriptor);

    }

    /**
     * Test successfully creating container.
     */
    @Test
    public void testOM_CCO_1_2() throws Exception {

        String xmlData1 = getContainerTemplate("create_container_WithoutMembers_v1.1.xml");

        assertXmlValidContainer(xmlData1);
        final String theContainerXml1 = create(xmlData1);
        assertXmlValidContainer(theContainerXml1);

        String subContainerId1 = getObjidValue(theContainerXml1);

        final String theContainerXml2 = create(xmlData1);
        assertXmlValidContainer(theContainerXml2);

        String subContainerId2 = getObjidValue(theContainerXml2);

        String xmlData = getContainerTemplate("create_container_v1.1-forContainer.xml");

        String xmlWithContainer1 = xmlData.replaceAll("##CONTAINERID1##", subContainerId1);
        String xmlWithContainer2 = xmlWithContainer1.replaceAll("##CONTAINERID2##", subContainerId2);
        Document document = EscidocRestSoapTestBase.getDocument(xmlWithContainer2);
        NodeList members =
            selectNodeList(document, "/container/struct-map/member-ref-list/member/container-ref/@objid");

        final String theContainerXml = create(xmlWithContainer2);
        assertXmlValidContainer(theContainerXml);
        final Document createdDocument = EscidocRestSoapTestBase.getDocument(theContainerXml);
        NodeList membersAfterCreate =
            selectNodeList(createdDocument, "/container/struct-map/member-ref-list/member/container-ref/@objid");

        List membersList = nodeList2List(members);
        List membersListAfterCreate = nodeList2List(membersAfterCreate);

        assertListContentEqual("Member list does not contain the same IDs as struct map.", membersList,
            membersListAfterCreate);

        // Node adminDescriptor =
        // selectSingleNode(createdDocument, "/container/admin-descriptor");
        // assertNotNull("No admin descriptor in container", adminDescriptor);

        Node itemRef = selectSingleNode(createdDocument, "/container/struct-map/item");
        assertNull(itemRef);
    }

    /**
     * Test declining creating container with missing Context objid
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOM_CCO_2_3() throws Exception {

        String conTemp = getContainerTemplate("create_container_WithoutMembers_v1.1.xml");
        Document container = EscidocRestSoapTestBase.getDocument(conTemp);
        // Node containerWithoutAdminDescriptor =
        // deleteElement(container, "/container/admin-descriptor");
        // String containerWithoutAdminDescriptorXml =
        // toString(containerWithoutAdminDescriptor, false);
        Node containerWithMissingContextObjid = deleteAttribute(container, "/container/properties/context", "objid");
        String containerWithMissingContextObjidXml = toString(containerWithMissingContextObjid, false);

        try {
            String xml = create(containerWithMissingContextObjidXml);
            fail("No exception occured on create with missing context href.");

        }
        catch (final Exception e) {
            Class ec = XmlCorruptedException.class;
            EscidocRestSoapTestBase.assertExceptionType(ec.getName() + " expected.", ec, e);
            return;
        }

    }

    /**
     * Test declining creating container (input parameter container xml is missing).
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOM_CCO_2_1() throws Exception {

        try {

            create(null);
        }
        catch (final MissingMethodParameterException e) {

            return;
        }
        fail("Not expected exception");
    }

    /**
     * Test declining creating container with non existing context.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOM_CCO_3() throws Exception {

        String conTemp = getContainerTemplate("create_container_WithoutMembers_v1.1.xml");
        Document container = EscidocRestSoapTestBase.getDocument(conTemp);
        Node containerWithWrongContextId = substitute(container, "/container/properties/context/@objid", "bla");
        String containerWithWrongContextIdXml = toString(containerWithWrongContextId, false);
        try {
            String xml = create(containerWithWrongContextIdXml);
        }
        catch (final ContextNotFoundException e) {
            return;
        }
        fail("Not expected exception");

    }

    /**
     * Test declining creating container with id of the context, which responses to another object type than context
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOM_CCO_4() throws Exception {

        String conTemp = getContainerTemplate("create_container_WithoutMembers_v1.1.xml");
        Document container = EscidocRestSoapTestBase.getDocument(conTemp);
        Node containerWithWrongContextObjectType =
            substitute(container, "/container/properties/context/@objid", "escidoc:persistent4");
        String containerWithWrongContextObjectTypeXml = toString(containerWithWrongContextObjectType, false);
        try {
            String xml = create(containerWithWrongContextObjectTypeXml);
        }
        catch (final ContextNotFoundException e) {
            return;
        }
        fail("Not expected exception");

    }

    /**
     * Test declining creating container without any md-record.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOM_CCO_5() throws Exception {

        String conTemp = getContainerTemplate("create_container_WithoutMembers_v1.1.xml");
        Document container = EscidocRestSoapTestBase.getDocument(conTemp);

        Node attributeMdRecordName = selectSingleNode(container, "/container/md-records/md-record[1]/@name");
        String nameValue = attributeMdRecordName.getTextContent();
        Node containerWithoutEscidocMetadata = null;
        if (nameValue.equals("escidoc")) {
            containerWithoutEscidocMetadata = deleteElement(container, "/container/md-records/md-record[1]");
        }
        String containerWithoutEscidocMetadataXml = toString(containerWithoutEscidocMetadata, true);

        try {
            String xml = create(containerWithoutEscidocMetadataXml);
        }
        catch (final InvalidXmlException e) {
            return;
        }
        fail("Not expected exception");
    }

    /**
     * Test declining creating container with missing Escidoc Internal Metadata Set.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOMCi2e() throws Exception {

        String conTemp = getContainerTemplate("create_container_WithoutMembers_v1.1.xml");
        Document xmlContainer = EscidocRestSoapTestBase.getDocument(conTemp);

        // Node attribute =
        // selectSingleNode(xmlItemWithoutComponents,
        // "/item/md-records/md-record/[@name = 'escidoc']/@name");
        // "/item/md-records/md-record/publication");
        Node xmlContainerWithoutInternalMetadata =
            substitute(xmlContainer, "/container/md-records/md-record[@name = 'escidoc']/@name", "bla");
        String xmlContainerWithoutInternalMetadataXml = toString(xmlContainerWithoutInternalMetadata, true);

        Class ec = MissingMdRecordException.class;
        try {
            String xml = create(xmlContainerWithoutInternalMetadataXml);
            EscidocRestSoapTestBase.failMissingException(ec);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.assertExceptionType(ec, e);
        }
    }

    /**
     * Test successfully creating container with two relations.
     */
    @Test
    public void testRelations() throws Exception {

        String createdContainerId1 = createContainerFromTemplate("create_container_WithoutMembers_v1.1.xml");
        String createdContainerId2 = createContainerFromTemplate("create_container_WithoutMembers_v1.1.xml");

        String containerForCreateWithRelationsXml =
            getContainerTemplate("create_container_WithoutMembers_v1.1_WithRelations.xml");

        containerForCreateWithRelationsXml =
            containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID1##", createdContainerId1);
        containerForCreateWithRelationsXml =
            containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID2##", createdContainerId2);
        Document containerForCreateWithRelations =
            EscidocRestSoapTestBase.getDocument(containerForCreateWithRelationsXml);

        NodeList relations = selectNodeList(containerForCreateWithRelations, "/container/relations/relation");

        String xml = create(containerForCreateWithRelationsXml);

        NodeList relationsAfterCreate =
            selectNodeList(EscidocRestSoapTestBase.getDocument(xml), "/container/relations/relation");

        assertXmlValidContainer(xml);
        assertEquals("Number of relations is wrong ", relations.getLength(), relationsAfterCreate.getLength());

    }

    /**
     * Test declining creating container with relations, whose targets references non existing resources.
     */
    @Test
    public void testRelationsWithWrongTarget() throws Exception {

        String createdContainerId1 = "bla1";
        String createdContainerId2 = "bla2";

        String containerForCreateWithRelationsXml =
            getContainerTemplate("create_container_WithoutMembers_v1.1_WithRelations.xml");

        containerForCreateWithRelationsXml =
            containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID1##", createdContainerId1);
        containerForCreateWithRelationsXml =
            containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID2##", createdContainerId2);

        try {
            create(containerForCreateWithRelationsXml);
            fail("No exception occured on container create with relations, which "
                + " references non existing targets.");
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.assertExceptionType("ReferencedResourceNotFoundException expected.",
                ReferencedResourceNotFoundException.class, e);
        }

    }

    /**
     * Test declining creating container with relations, whose target ids containing a version number.
     */
    @Test
    public void testRelationsWithTargetContainingVersionNumber() throws Exception {
        String createdContainerId1 = "escidoc:123:2";
        String createdContainerId2 = "escidoc:123:3";

        String containerForCreateWithRelationsXml =
            getContainerTemplate("create_container_WithoutMembers_v1.1_WithRelations.xml");

        containerForCreateWithRelationsXml =
            containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID1##", createdContainerId1);
        containerForCreateWithRelationsXml =
            containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID2##", createdContainerId2);

        try {
            create(containerForCreateWithRelationsXml);
            fail("No exception occured on container crate with relations, which "
                + " target ids containing a version number.");
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.assertExceptionType("InvalidContentException expected.",
                InvalidContentException.class, e);
        }
    }

    /**
     * Test declining creating container with relations with non existing predicate.
     */
    @Test
    public void testRelationsWithWrongPredicate() throws Exception {

        String createdContainerId1 = createContainerFromTemplate("create_container_WithoutMembers_v1.1.xml");
        String createdContainerId2 = createContainerFromTemplate("create_container_WithoutMembers_v1.1.xml");

        String containerForCreateWithRelationsXml =
            getContainerTemplate("create_container_WithoutMembers_v1.1_WithRelations.xml");

        containerForCreateWithRelationsXml =
            containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID1##", createdContainerId1);
        containerForCreateWithRelationsXml =
            containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID2##", createdContainerId2);

        Document containerForCreateWithRelations =
            EscidocRestSoapTestBase.getDocument(containerForCreateWithRelationsXml);
        Node relationPredicate =
            selectSingleNode(containerForCreateWithRelations, "/container/relations/relation[1]/@predicate");
        relationPredicate.setNodeValue("http://www.bla.de#bla");

        String containerXml = toString(containerForCreateWithRelations, true);

        try {
            create(containerXml);
            fail("No exception occured on container create with relations, which "
                + " references non existing predicate.");
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.assertExceptionType("RelationPredicateNotFoundException expected.",
                RelationPredicateNotFoundException.class, e);
        }

    }

    /**
     * Test declinig creating an container without specifying the content model id, using data provided for issue 365.
     *
     * @throws Exception Thrown if anythinf fails.
     */
    @Test
    public void testOM_CCO_issue365() throws Exception {

        final Class ec = XmlCorruptedException.class;

        Document toBeCreatedDocument =
            EscidocRestSoapTestBase.getDocument(getContainerTemplate("create_container_WithoutMembers_v1.1.xml"));
        deleteElement(toBeCreatedDocument, XPATH_CONTAINER_CONTENT_MODEL);
        addAfter(toBeCreatedDocument, XPATH_CONTAINER_CONTEXT, createElementNode(toBeCreatedDocument, SREL_NS_URI,
            "srel", NAME_CONTENT_MODEL, null));

        String toBeCreatedXml = toString(toBeCreatedDocument, true);
        try {
            create(toBeCreatedXml);
            EscidocRestSoapTestBase.failMissingException(
                "Creating container with empty content-model element not declined.", ec);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.assertExceptionType(
                "Creating container with empty content-model element not declined" + ", properly", ec, e);
        }

    }

    /**
     * Test successfully creating of an container with 2 md-records.
     */
    @Test
    public void testCreateContainerWith2Mdrecords() throws Exception {

        Document xmlContainer =
            EscidocRestSoapTestBase
                .getDocument(getContainerTemplate("create_container_2_Md_Records_WithoutMembers_v1.1.xml"));
        NodeList mdrecords = selectNodeList(xmlContainer, "/container/md-records/md-record");
        // Node containerWithoutAdminDescriptor =
        // deleteElement(xmlContainer, "/container/admin-descriptor");
        // String containerWithoutAdminDescriptorXml =
        // toString(containerWithoutAdminDescriptor, false);
        String container = getContainerTemplate("create_container_2_Md_Records_WithoutMembers_v1.1.xml");
        assertXmlValidContainer(container);

        final String createdXml = create(container);

        assertXmlValidContainer(createdXml);
        final Document createdDocument = EscidocRestSoapTestBase.getDocument(createdXml);

        NodeList mdrecordsAfterCreate = selectNodeList(createdDocument, "/container/md-records/md-record");
        assertEquals(mdrecords.getLength(), mdrecordsAfterCreate.getLength());

    }

}
