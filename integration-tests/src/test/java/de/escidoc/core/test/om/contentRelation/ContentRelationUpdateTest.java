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
package de.escidoc.core.test.om.contentRelation;

import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidContentException;
import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.remote.application.violated.OptimisticLockingException;
import de.escidoc.core.test.EscidocRestSoapTestBase;
import de.escidoc.core.test.common.client.servlet.Constants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Test content relation create implementation.
 *
 * @author Steffen Wagner
 */
@RunWith(value = Parameterized.class)
public class ContentRelationUpdateTest extends ContentRelationTestBase {

    private String relationXml = null;

    private String relationId = null;

    /**
     * @param transport The transport identifier.
     */
    public ContentRelationUpdateTest(final int transport) {
        super(transport);
    }

    /**
     * Set up servlet test.
     *
     * @throws Exception If anything fails.
     */
    @Before
    public void setUp() throws Exception {
        String contentRelationXml = getExampleTemplate("content-relation-01.xml");
        relationXml = create(contentRelationXml);
        relationId = getObjidValue(relationXml);
    }

    /**
     * Update with empty XML.
     *
     * @throws Exception Thrown if no or wrong exception is thrown
     */
    @Test
    public void testUpdateWithEmptyXML() throws Exception {

        try {
            update(this.relationId, "");
        }
        catch (final Exception e) {
            Class<?> ec = MissingMethodParameterException.class;
            EscidocRestSoapTestBase.assertExceptionType(ec.getName() + " expected.", ec, e);
        }
    }

    /**
     * Delete md-record and add new with same name again.
     *
     * @throws Exception Thrown if behaviour differs from expected
     */
    @Test
    public void testAddMdRecordAfterDelete() throws Exception {

        // create ContentRelation and remove md-records
        Document relationCreated = getDocument(relationXml);
        NodeList mdRecords = selectNodeList(relationCreated, "/content-relation/md-records/md-record");
        String mdRecordPath = null;
        if (mdRecords.getLength() > 1) {
            mdRecordPath = "/content-relation/md-records/md-record[@name='escidoc']";
        }
        else {
            mdRecordPath = "/content-relation/md-records";
        }
        Node relationWithoutEscidocMdRecord = deleteElement(relationCreated, mdRecordPath);
        String relationWithoutEscidocMdRecordXml = toString(relationWithoutEscidocMdRecord, true);

        // update ContentRelation with missing md-records
        String updatedXml = update(this.relationId, relationWithoutEscidocMdRecordXml);
        assertXmlValidContentRelation(updatedXml);

        // check if md-records element is deleted
        Document updatedDocument = getDocument(updatedXml);
        Node notExistedMdRecord = selectSingleNode(updatedDocument, mdRecordPath);
        assertNull("Escidoc md-record must be deleted", notExistedMdRecord);

        // add new md-record element
        Element mdRecordsNew =
            updatedDocument.createElementNS("http://www.escidoc.de/schemas/metadatarecords/0.5",
                "escidocMetadataRecords:md-records");
        Element mdRecord =
            updatedDocument.createElementNS("http://www.escidoc.de/schemas/metadatarecords/0.5",
                "escidocMetadataRecords:md-record");
        mdRecordsNew.appendChild(mdRecord);
        mdRecord.setAttribute("name", "md2");
        mdRecord.setAttribute("schema", "bla");
        Element mdRecordContent = updatedDocument.createElement("md");
        mdRecord.appendChild(mdRecordContent);
        mdRecordContent.setTextContent("more then bla");
        Node resources = selectSingleNode(updatedDocument, "/content-relation/resources");
        selectSingleNode(updatedDocument, "/content-relation").insertBefore(mdRecordsNew, resources);
        String relationWithNewMdRecord = toString(updatedDocument, false);

        // update with new md-record
        updatedXml = update(this.relationId, relationWithNewMdRecord);

        // check md-record element
        assertXmlValidContentRelation(updatedXml);
        Document updatedRelationDocument = getDocument(updatedXml);
        Node escidocMdRecord = selectSingleNode(updatedRelationDocument, mdRecordPath);
        assertNotNull(escidocMdRecord);

    }

    /**
     * Test adding a second md-record to ContentRelation. One md-record already exists.
     *
     * @throws Exception Thrown if adding one md-record failed.
     */
    @Test
    public void testAddMdRecord() throws Exception {

        // add one more md-record
        Document relationCreated = getDocument(relationXml);
        NodeList mdRecordsCreated = selectNodeList(relationCreated, "/content-relation/md-records/md-record");
        int numberMdRecordsCreated = mdRecordsCreated.getLength();
        Element mdRecord =
            relationCreated.createElementNS("http://www.escidoc.de/schemas/metadatarecords/0.5",
                "escidocMetadataRecords:md-record");
        mdRecord.setAttribute("name", "md2");
        mdRecord.setAttribute("schema", "bla");
        Element mdRecordContent = relationCreated.createElement("md");
        mdRecord.appendChild(mdRecordContent);
        mdRecordContent.setTextContent("bla");
        selectSingleNode(relationCreated, "/content-relation/md-records").appendChild(mdRecord);
        String relationWithNewMdRecord = toString(relationCreated, false);

        String updatedXml = update(this.relationId, relationWithNewMdRecord);

        // check updated
        assertXmlValidContentRelation(updatedXml);
        Document updatedRelationDocument = getDocument(updatedXml);
        NodeList mdRecordsUpdated = selectNodeList(updatedRelationDocument, "/content-relation/md-records/md-record");
        int numberMdRecordsUpdated = mdRecordsUpdated.getLength();
        assertEquals("the content relation should have one additional" + " md-record after update ",
            numberMdRecordsUpdated, numberMdRecordsCreated + 1);

    }

    /**
     * Test to add a second md-record.
     *
     * @throws Exception Thrown if adding of additional md-record failed.
     */
    @Test
    public void testAddSecondEscidocMdRecord() throws Exception {
        Document relationCreated = getDocument(relationXml);

        Element mdRecord =
            relationCreated.createElementNS("http://www.escidoc.de/schemas/metadatarecords/0.5",
                "escidocMetadataRecords:md-record");
        mdRecord.setAttribute("name", "escidoc");
        mdRecord.setAttribute("schema", "bla");
        Element mdRecordContent = relationCreated.createElement("md");
        mdRecord.appendChild(mdRecordContent);
        mdRecordContent.setTextContent("updated");
        selectSingleNode(relationCreated, "/content-relation/md-records").appendChild(mdRecord);
        String relationWithNewMdRecord = toString(relationCreated, false);

        try {
            update(this.relationId, relationWithNewMdRecord);
            fail("No exception on update content relation with" + " two md-records with the same name.");
        }
        catch (final Exception e) {
            Class<?> ec = InvalidContentException.class;
            EscidocRestSoapTestBase.assertExceptionType(ec.getName() + " expected.", ec, e);
        }

    }

    /**
     * Test delete md-record.
     *
     * @throws Exception Thrown if deletion of md-record failed.
     */
    @Test
    public void testDeleteMdRecord() throws Exception {
        Document relationCreated = getDocument(relationXml);
        NodeList mdRecords = selectNodeList(relationCreated, "/content-relation/md-records/md-record");
        String mdRecordPath = null;
        if (mdRecords.getLength() > 1) {
            mdRecordPath = "/content-relation/md-records/md-record[@name='escidoc']";
        }
        else {
            mdRecordPath = "/content-relation/md-records";
        }
        Node relationWithoutEscidocMdRecord = deleteElement(relationCreated, mdRecordPath);
        String relationWithoutEscidocMdRecordXml = toString(relationWithoutEscidocMdRecord, true);
        String updatedXml = update(this.relationId, relationWithoutEscidocMdRecordXml);
        assertXmlValidContentRelation(updatedXml);

        Document updatedDocument = getDocument(updatedXml);

        Node notExistedMdRecord = selectSingleNode(updatedDocument, mdRecordPath);
        assertNull("Escidoc md-record must be deleted", notExistedMdRecord);
    }

    /**
     * Test update description of Content Relation.
     *
     * @throws Exception Thrown if description is not updated or other (unexpected) values are changed.
     */
    @Test
    public void testUpdateDescription() throws Exception {

        // change description
        Document relationCreated = getDocument(relationXml);
        Node description = selectSingleNode(relationCreated, "/content-relation/properties/description");
        String updatedDescription = "updated description";
        description.setTextContent(updatedDescription);
        String relationToUdate = toString(relationCreated, false);

        // update
        String updatedRelationXml = update(this.relationId, relationToUdate);

        // check values of updated ContentRelation
        assertXmlValidContentRelation(updatedRelationXml);
        Document updatedRelationDocument = getDocument(updatedRelationXml);
        String descriptionValue =
            selectSingleNode(updatedRelationDocument, "/content-relation/properties/description").getTextContent();
        assertEquals(updatedDescription, descriptionValue);

        // retrieve
        String retrieveXml = retrieve(this.relationId);

        // check values of retrieved ContentRelation
        assertXmlValidContentRelation(retrieveXml);
        Document retrieveDoc = getDocument(retrieveXml);
        descriptionValue = selectSingleNode(retrieveDoc, "/content-relation/properties/description").getTextContent();
        assertEquals(updatedDescription, descriptionValue);
    }

    /**
     * Test update subject of Content Relation.
     *
     * @throws Exception Thrown if description is not updated or other (unexpected) values are changed.
     */
    @Test
    public void testUpdateSubject() throws Exception {

        // change subject
        Document relationCreated = getDocument(relationXml);

        String newSubject = createItemFromTemplate("item_without_component.xml");

        String oldSubject = null;
        Node subject = null;
        if (Constants.TRANSPORT_REST == getTransport()) {
            subject = selectSingleNode(relationCreated, "/content-relation/subject/@href");
            newSubject = "/ir/item/" + newSubject;

            oldSubject = selectSingleNode(relationCreated, "/content-relation/subject/@href").getNodeValue();
        }
        else {
            subject = selectSingleNode(relationCreated, "/content-relation/subject/@objid");
            oldSubject = selectSingleNode(relationCreated, "/content-relation/subject/@objid").getNodeValue();
        }

        subject.setNodeValue(newSubject);

        String relationToUdate = toString(relationCreated, false);

        // update
        String updatedRelationXml = update(this.relationId, relationToUdate);

        // check values of updated ContentRelation
        assertXmlValidContentRelation(updatedRelationXml);
        Document updatedRelationDoc = getDocument(updatedRelationXml);

        String subjectValue = null;

        if (Constants.TRANSPORT_REST == getTransport()) {
            subjectValue = selectSingleNode(updatedRelationDoc, "/content-relation/subject/@href").getNodeValue();
        }
        else {
            subjectValue = selectSingleNode(updatedRelationDoc, "/content-relation/subject/@objid").getNodeValue();
        }
        assertEquals(oldSubject, subjectValue);

        // retrieve
        String retrieveXml = retrieve(this.relationId);

        // check values of retrieved ContentRelation
        assertXmlValidContentRelation(retrieveXml);
        Document retrieveDoc = getDocument(retrieveXml);
        if (Constants.TRANSPORT_REST == getTransport()) {
            subjectValue = selectSingleNode(retrieveDoc, "/content-relation/subject/@href").getNodeValue();
        }
        else {
            subjectValue = selectSingleNode(retrieveDoc, "/content-relation/subject/@objid").getNodeValue();
        }
        assertEquals(oldSubject, subjectValue);

        String lmdCreate = getLastModificationDateValue(relationCreated);
        String lmdUpdate = getLastModificationDateValue(updatedRelationDoc);
        String lmdRetrieve = getLastModificationDateValue(retrieveDoc);
        assertEquals("resource modified", lmdCreate, lmdUpdate);
        assertEquals("resource modified", lmdCreate, lmdRetrieve);
    }

    /**
     * Test update object of Content Relation.
     *
     * @throws Exception Thrown if description is not updated or other (unexpected) values are changed.
     */
    @Test
    public void testUpdateObject() throws Exception {

        // change subject
        Document relationCreated = getDocument(relationXml);

        String newSubject = createItemFromTemplate("item_without_component.xml");

        String oldSubject = null;
        Node subject = null;
        if (Constants.TRANSPORT_REST == getTransport()) {
            subject = selectSingleNode(relationCreated, "/content-relation/object/@href");
            newSubject = "/ir/item/" + newSubject;

            oldSubject = selectSingleNode(relationCreated, "/content-relation/object/@href").getNodeValue();
        }
        else {
            subject = selectSingleNode(relationCreated, "/content-relation/object/@objid");
            oldSubject = selectSingleNode(relationCreated, "/content-relation/object/@objid").getNodeValue();
        }

        subject.setNodeValue(newSubject);

        String relationToUdate = toString(relationCreated, false);

        // update
        String updatedRelationXml = update(this.relationId, relationToUdate);

        // check values of updated ContentRelation
        assertXmlValidContentRelation(updatedRelationXml);
        Document updatedRelationDoc = getDocument(updatedRelationXml);

        String subjectValue = null;

        if (Constants.TRANSPORT_REST == getTransport()) {
            subjectValue = selectSingleNode(updatedRelationDoc, "/content-relation/object/@href").getNodeValue();
        }
        else {
            subjectValue = selectSingleNode(updatedRelationDoc, "/content-relation/object/@objid").getNodeValue();
        }
        assertEquals(oldSubject, subjectValue);

        // retrieve
        String retrieveXml = retrieve(this.relationId);

        // check values of retrieved ContentRelation
        assertXmlValidContentRelation(retrieveXml);
        Document retrieveDoc = getDocument(retrieveXml);
        if (Constants.TRANSPORT_REST == getTransport()) {
            subjectValue = selectSingleNode(retrieveDoc, "/content-relation/object/@href").getNodeValue();
        }
        else {
            subjectValue = selectSingleNode(retrieveDoc, "/content-relation/object/@objid").getNodeValue();
        }
        assertEquals(oldSubject, subjectValue);

        String lmdCreate = getLastModificationDateValue(relationCreated);
        String lmdUpdate = getLastModificationDateValue(updatedRelationDoc);
        String lmdRetrieve = getLastModificationDateValue(retrieveDoc);
        assertEquals("resource modified", lmdCreate, lmdUpdate);
        assertEquals("resource modified", lmdCreate, lmdRetrieve);
    }

    /**
     * Check if during update optimistic locking is checked.
     * 
     * @throws Exception
     */
    @Test(expected = OptimisticLockingException.class)
    public void checkOptimisticLocking() throws Exception {

        Document relationCreated = getDocument(relationXml);
        Node lmd = selectSingleNode(relationCreated, "/content-relation/@last-modification-date");
        lmd.setTextContent("2006-12-21T13:15:23.485Z");
        String relationToUdate = toString(relationCreated, false);

        // update
        update(this.relationId, relationToUdate);
    }

}