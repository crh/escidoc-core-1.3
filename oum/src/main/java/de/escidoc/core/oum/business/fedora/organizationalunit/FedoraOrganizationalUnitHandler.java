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
/**
 *
 */
package de.escidoc.core.oum.business.fedora.organizationalunit;

import de.escidoc.core.common.business.Constants;
import de.escidoc.core.common.business.fedora.EscidocBinaryContent;
import de.escidoc.core.common.business.fedora.TripleStoreUtility;
import de.escidoc.core.common.business.fedora.Utility;
import de.escidoc.core.common.business.fedora.resources.Predecessor;
import de.escidoc.core.common.business.fedora.resources.PredecessorForm;
import de.escidoc.core.common.business.fedora.resources.ResourceType;
import de.escidoc.core.common.business.fedora.resources.listener.ResourceListener;
import de.escidoc.core.common.business.filter.LuceneRequestParameters;
import de.escidoc.core.common.business.filter.SRURequest;
import de.escidoc.core.common.business.filter.SRURequestParameters;
import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
import de.escidoc.core.common.exceptions.application.missing.MissingElementValueException;
import de.escidoc.core.common.exceptions.application.missing.MissingMdRecordException;
import de.escidoc.core.common.exceptions.application.notfound.MdRecordNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.OperationNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.OrganizationalUnitNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException;
import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
import de.escidoc.core.common.exceptions.application.violated.OrganizationalUnitHasChildrenException;
import de.escidoc.core.common.exceptions.application.violated.OrganizationalUnitHierarchyViolationException;
import de.escidoc.core.common.exceptions.system.EncodingSystemException;
import de.escidoc.core.common.exceptions.system.FedoraSystemException;
import de.escidoc.core.common.exceptions.system.IntegritySystemException;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
import de.escidoc.core.common.util.service.BeanLocator;
import de.escidoc.core.common.util.service.UserContext;
import de.escidoc.core.common.util.stax.StaxParser;
import de.escidoc.core.common.util.stax.handler.MultipleExtractor2;
import de.escidoc.core.common.util.stax.handler.OptimisticLockingHandler;
import de.escidoc.core.common.util.stax.handler.TaskParamHandler;
import de.escidoc.core.common.util.xml.XmlUtility;
import de.escidoc.core.common.util.xml.factory.XmlTemplateProvider;
import de.escidoc.core.om.service.interfaces.ContentRelationHandlerInterface;
import de.escidoc.core.oum.business.handler.OrganizationalUnitMetadataHandler;
import de.escidoc.core.oum.business.handler.OrganizationalUnitParentsHandler;
import de.escidoc.core.oum.business.handler.OrganizationalUnitPredecessorsHandler;
import de.escidoc.core.oum.business.interfaces.OrganizationalUnitHandlerInterface;
import de.escidoc.core.oum.business.utility.OumUtility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Frank Schwichtenberg
 */
public class FedoraOrganizationalUnitHandler extends OrganizationalUnitHandlerUpdate
    implements OrganizationalUnitHandlerInterface {

    /**
     * SRU request.
     */
    private SRURequest sruRequest;

    private final Collection<ResourceListener> ouListeners = new ArrayList<ResourceListener>();

    private final ContentRelationHandlerInterface contentRelationHandler;

    /**
     * Construct a new FedoraOrganizationalUnitHandler. Get a reference to the ContentRelationHandler object.
     *
     * @throws WebserverSystemException getting the ContentRelationHandler failed
     */
    public FedoraOrganizationalUnitHandler() throws WebserverSystemException {
        this.contentRelationHandler = BeanLocator.locateContentRelationHandler();
    }

    /**
     * Register an ou listener.
     *
     * @param listener listener which will be added to the list
     */
    public void addOuListener(final ResourceListener listener) {
        ouListeners.add(listener);
    }

    /**
     * Unregister an ou listener.
     *
     * @param listener listener which will be removed from the list
     */
    public void removeItemListener(final ResourceListener listener) {
        ouListeners.remove(listener);
    }

    /**
     * Get the alternate form of an ou representation. If the current request came in via REST, then the SOAP form will
     * be returned here and vice versa.
     *
     * @return alternate form of the ou
     * @throws SystemException An internal error occurred.
     * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
     */
    private String getAlternateForm() throws SystemException, WebserverSystemException {
        String result = null;
        final boolean isRestAccess = UserContext.isRestAccess();

        try {
            if (isRestAccess) {
                UserContext.setRestAccess(false);
                result = getRenderer().render(getOrganizationalUnit());
            }
            else {
                UserContext.setRestAccess(true);
                result = getRenderer().render(getOrganizationalUnit());
            }
        }
        catch (final WebserverSystemException e) {
            throw new SystemException(e);
        }
        catch (final Exception e) {
            // should not happen here
            throw new SystemException(e);
        }
        finally {
            UserContext.setRestAccess(isRestAccess);
        }
        return result;
    }

    /**
     * Notify the listeners that an ou was created.
     *
     * @param id      ou id
     * @param xmlData complete ou XML
     * @throws SystemException One of the listeners threw an exception.
     * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
     */
    private void fireOuCreated(final String id, final String xmlData) throws SystemException, WebserverSystemException {
        final String restXml;
        final String soapXml;

        if (UserContext.isRestAccess()) {
            restXml = xmlData;
            soapXml = getAlternateForm();
        }
        else {
            restXml = getAlternateForm();
            soapXml = xmlData;
        }
        for (final ResourceListener ouListener : this.ouListeners) {
            ouListener.resourceCreated(id, restXml, soapXml);
        }
    }

    /**
     * Notify the listeners that an ou was deleted.
     *
     * @param id item id
     * @throws SystemException One of the listeners threw an exception.
     */
    private void fireOuDeleted(final String id) throws SystemException {
        for (final ResourceListener ouListener : this.ouListeners) {
            ouListener.resourceDeleted(id);
        }
    }

    /**
     * Notify the listeners that an item was modified.
     *
     * @param id      item id
     * @param xmlData complete item XML
     * @throws SystemException One of the listeners threw an exception.
     * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
     */
    private void fireOuModified(final String id, final String xmlData) throws SystemException, WebserverSystemException {
        final String restXml;
        final String soapXml;

        if (UserContext.isRestAccess()) {
            restXml = xmlData;
            soapXml = getAlternateForm();
        }
        else {
            restXml = getAlternateForm();
            soapXml = xmlData;
        }
        for (final ResourceListener ouListener : this.ouListeners) {
            ouListener.resourceModified(id, restXml, soapXml);
        }
    }

    /**
     * Ingest an organizational unit.
     *
     * @param xmlData XML containing the ou
     * @return the id of the created organizational unit.
     * @throws SystemException                e
     * @throws OrganizationalUnitNotFoundException
     *                                        e
     * @throws MissingAttributeValueException e
     * @throws MissingElementValueException   e
     * @throws InvalidStatusException         e
     * @throws MissingMdRecordException       If the required md-record is missing
     */
    @Override
    public String ingest(final String xmlData) throws InvalidStatusException, MissingElementValueException,
        MissingAttributeValueException, OrganizationalUnitNotFoundException, SystemException, MissingMdRecordException,
        XmlCorruptedException, EncodingSystemException, IntegritySystemException, FedoraSystemException,
        WebserverSystemException, TripleStoreSystemException, XmlParserSystemException {
        return doCreate(xmlData, false);
    }

    /**
     * See Interface for functional description.
     *
     * @throws InvalidStatusException         e
     * @throws MissingElementValueException   e
     * @throws MissingAttributeValueException e
     * @throws SystemException                e
     * @throws OrganizationalUnitNotFoundException
     *                                        e
     * @throws XmlCorruptedException          e
     * @throws XmlSchemaValidationException   e
     * @throws MissingMdRecordException       If the required md-record is missing
     */
    @Override
    public String create(final String xmlData) throws InvalidStatusException, MissingElementValueException,
        MissingAttributeValueException, SystemException, OrganizationalUnitNotFoundException, XmlCorruptedException,
        XmlSchemaValidationException, MissingMdRecordException, EncodingSystemException, IntegritySystemException,
        FedoraSystemException, WebserverSystemException, TripleStoreSystemException, XmlParserSystemException {
        return doCreate(xmlData, true);
    }

    /**
     * Wrapper for the create method.
     *
     * @param xml      Organizational Unit as eSciDoc XML representation.
     * @param isCreate True if the Organizational Unit use the create method. False if ingest is used. Create has higher
     *                 restrictions for public-status and requires a return of the whole created data strucutre.
     * @return Either the eSciDoc XML representation (if create method is used) or only the objid (if ingest method is
     *         used).
     * @throws InvalidStatusException         e
     * @throws MissingElementValueException   e
     * @throws MissingAttributeValueException e
     * @throws SystemException                e
     * @throws OrganizationalUnitNotFoundException
     *                                        e
     * @throws XmlCorruptedException          e
     * @throws MissingMdRecordException       If the required md-record is missing
     * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
     * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
     * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
     * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
     * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
     * @throws de.escidoc.core.common.exceptions.system.EncodingSystemException
     */
    public String doCreate(final String xml, final boolean isCreate) throws InvalidStatusException,
        MissingElementValueException, MissingAttributeValueException, SystemException,
        OrganizationalUnitNotFoundException, XmlCorruptedException, MissingMdRecordException, EncodingSystemException,
        IntegritySystemException, FedoraSystemException, WebserverSystemException, TripleStoreSystemException,
        XmlParserSystemException {

        final StaxParser sp = new StaxParser();
        final OrganizationalUnitParentsHandler parentsHandler = new OrganizationalUnitParentsHandler(sp);
        sp.addHandler(parentsHandler);

        final OrganizationalUnitPredecessorsHandler predecessorsHandler = new OrganizationalUnitPredecessorsHandler(sp);
        sp.addHandler(predecessorsHandler);

        final OrganizationalUnitMetadataHandler metadataHandler =
            new OrganizationalUnitMetadataHandler(sp, '/' + XmlUtility.NAME_ORGANIZATIONAL_UNIT);
        sp.addHandler(metadataHandler);
        final MultipleExtractor2 me = createMultipleExtractor(sp, metadataHandler.getMdRecordPath());
        sp.addHandler(me);

        parseIncomingXmlForCreate(xml, sp);

        final Map<String, Object> streams = me.getOutputStreams();

        final Map<String, Object> relsExtValues = new HashMap<String, Object>();
        final String[] creator = Utility.getCurrentUser();
        relsExtValues.put(XmlTemplateProvider.PUBLIC_STATUS, Constants.STATUS_OU_CREATED);

        relsExtValues.put(XmlTemplateProvider.CREATED_BY_ID, creator[0]);
        relsExtValues.put(XmlTemplateProvider.CREATED_BY_TITLE, creator[1]);
        relsExtValues.put(XmlTemplateProvider.MODIFIED_BY_ID, creator[0]);
        relsExtValues.put(XmlTemplateProvider.MODIFIED_BY_TITLE, creator[1]);
        relsExtValues.put(XmlTemplateProvider.TITLE, metadataHandler.getDcTitle());

        // add predecessors to RELS-EXT
        relsExtValues.put(XmlTemplateProvider.PREDECESSORS, getPredessorsMap(predecessorsHandler.getPredecessors(),
            null));

        // parents
        final List<String> parents = parentsHandler.getParentOus();
        checkCreateParentsConditions(parents);
        checkName(null, metadataHandler.getDcTitle(), parents);

        final String id = getIdProvider().getNextPid();
        final String escidocMdRecord;
        try {
            escidocMdRecord =
                ((Map<String, ByteArrayOutputStream>) streams.get(XmlUtility.NAME_MDRECORDS)).get("escidoc").toString(
                    XmlUtility.CHARACTER_ENCODING);
        }
        catch (final UnsupportedEncodingException e) {
            throw new EncodingSystemException(e.getMessage(), e);
        }
        String dcStream = "";
        if (escidocMdRecord != null) {
            dcStream = XmlUtility.createDC(metadataHandler.getEscidocMetadataRecordNameSpace(), escidocMdRecord, id);
        }
        final String orgUnitFoxml =
            getOrganizationalUnitFoxml(id, relsExtValues, parents, metadataHandler.getMetadataAttributes(),
                (Map<String, ByteArrayOutputStream>) streams.get(XmlUtility.NAME_MDRECORDS), dcStream);
        getFedoraUtility().storeObjectInFedora(orgUnitFoxml, true);

        String resultOrgUnit;

        try {
            resultOrgUnit = retrieve(id);
        }
        catch (final OrganizationalUnitNotFoundException e) {
            throw new IntegritySystemException("Error retrieving created organizational-unit with id " + id + '!', e);
        }

        fireOuCreated(id, resultOrgUnit);

        // reload all parent OUs in the DB cache to update the property
        // "has-children"
        for (final String parentId : parents) {
            fireOuModified(parentId, retrieve(parentId));
        }
        // reload all predecessor OUs in the DB cache to update the property
        // "successor"
        for (final Predecessor predecessor : predecessorsHandler.getPredecessors()) {
            fireOuModified(predecessor.getObjid(), retrieve(predecessor.getObjid()));
        }
        if (!isCreate) {
            resultOrgUnit = id;
        }

        return resultOrgUnit;
    }

    /**
     * See Interface for functional description.
     *
     * @throws OrganizationalUnitNotFoundException
     *                                e
     * @throws InvalidStatusException e
     * @throws OrganizationalUnitHasChildrenException
     *                                e
     * @throws SystemException        e
     * @see de.escidoc.core.oum.business.interfaces.OrganizationalUnitHandlerInterface#delete(java.lang.String)
     */
    @Override
    public void delete(final String id) throws OrganizationalUnitNotFoundException, InvalidStatusException,
        OrganizationalUnitHasChildrenException, SystemException, FedoraSystemException, WebserverSystemException,
        IntegritySystemException, TripleStoreSystemException {

        setOrganizationalUnit(id);
        checkInState("deleted", Constants.STATUS_OU_CREATED);
        checkWithoutChildren("deleted");

        getFedoraUtility().deleteObject(id, true);
        fireOuDeleted(id);

        // update property hasChildren of all parents in Lucene
        final List<String> parentIds = getOrganizationalUnit().getParents();

        if (parentIds != null) {
            for (final String parentId : parentIds) {
                fireOuModified(parentId, retrieve(parentId));
            }
        }
    }

    /**
     * See Interface for functional description.
     *
     * @throws OrganizationalUnitNotFoundException
     *                         e
     * @throws SystemException e
     * @see de.escidoc.core.oum.business.interfaces.OrganizationalUnitHandlerInterface#retrieve(java.lang.String)
     */
    @Override
    public String retrieve(final String id) throws OrganizationalUnitNotFoundException, SystemException,
        TripleStoreSystemException, IntegritySystemException, WebserverSystemException {

        setOrganizationalUnit(id);
        return getRenderer().render(getOrganizationalUnit());
    }

    /**
     * See Interface for functional description.
     *
     * @throws InvalidStatusException       e
     * @throws MissingElementValueException e
     * @throws OptimisticLockingException   e
     * @throws OrganizationalUnitHierarchyViolationException
     *                                      e
     * @throws OrganizationalUnitNotFoundException
     *                                      e
     * @throws SystemException              e
     * @see de.escidoc.core.oum.business.interfaces.OrganizationalUnitHandlerInterface#update(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public String update(final String id, final String xml) throws MissingElementValueException,
        OrganizationalUnitNotFoundException, SystemException, OptimisticLockingException,
        OrganizationalUnitHierarchyViolationException, InvalidStatusException, EncodingSystemException,
        IntegritySystemException, FedoraSystemException, TripleStoreSystemException, WebserverSystemException,
        XmlParserSystemException, XmlCorruptedException {

        setOrganizationalUnit(id);
        final List<String> parentsBeforeUpdate = getOrganizationalUnit().getParents();
        final List<Predecessor> predecessorsBeforeUpdate = getOrganizationalUnit().getPredecessors();

        final String startTimeStamp = getOrganizationalUnit().getLastFedoraModificationDate();
        final StaxParser sp = new StaxParser();
        final OptimisticLockingHandler optimisticLockingHandler =
            new OptimisticLockingHandler(getOrganizationalUnit().getId(), Constants.ORGANIZATIONAL_UNIT_OBJECT_TYPE,
                startTimeStamp);
        sp.addHandler(optimisticLockingHandler);

        final OrganizationalUnitMetadataHandler metadataHandler =
            new OrganizationalUnitMetadataHandler(sp, '/' + XmlUtility.NAME_ORGANIZATIONAL_UNIT);
        sp.addHandler(metadataHandler);
        final MultipleExtractor2 me = createMultipleExtractor(sp, metadataHandler.getMdRecordPath());
        sp.addHandler(me);

        final OrganizationalUnitPredecessorsHandler predecessorsHandler = new OrganizationalUnitPredecessorsHandler(sp);
        sp.addHandler(predecessorsHandler);

        final OrganizationalUnitParentsHandler parentsHandler = new OrganizationalUnitParentsHandler(sp);
        sp.addHandler(parentsHandler);

        parseIncomingXmlForUpdate(xml, sp);

        final List<String> parents = parentsHandler.getParentOus();
        final OumUtility oumUtility = new OumUtility();
        oumUtility.detectCycles(id, parents);
        checkUpdateParentsConditions(parents);

        final Map<String, Object> relsExtValues = new HashMap<String, Object>();
        final String[] creator = Utility.getCurrentUser();
        relsExtValues.put(XmlTemplateProvider.FRAMEWORK_BUILD_NUMBER, Utility.getBuildNumber());
        relsExtValues.put(XmlTemplateProvider.PUBLIC_STATUS, getOrganizationalUnit().getPublicStatus());
        relsExtValues.put(XmlTemplateProvider.CREATED_BY_ID, getOrganizationalUnit().getCreatedBy());
        relsExtValues.put(XmlTemplateProvider.CREATED_BY_TITLE, getOrganizationalUnit().getCreatedByTitle());
        relsExtValues.put(XmlTemplateProvider.MODIFIED_BY_ID, creator[0]);
        relsExtValues.put(XmlTemplateProvider.MODIFIED_BY_TITLE, creator[1]);
        relsExtValues.put(XmlTemplateProvider.TITLE, metadataHandler.getDcTitle());

        checkName(id, metadataHandler.getDcTitle(), parents);

        // predecessors
        relsExtValues
            .put(XmlTemplateProvider.PREDECESSORS, getPredessorsMap(predecessorsHandler.getPredecessors(), id));

        try {
            setMdRecords((Map<String, ByteArrayOutputStream>) me.getOutputStreams().get(XmlUtility.NAME_MDRECORDS),
                metadataHandler.getMetadataAttributes(), metadataHandler.getEscidocMetadataRecordNameSpace());
            // call getRelsExt, otherwise it is not initialized and unmodified
            // properties cause an update
            getOrganizationalUnit().getRelsExt();
            getOrganizationalUnit().setRelsExt(getOrganizationalUnitRelsExt(id, relsExtValues, parents));
            getOrganizationalUnit().persist();
        }
        catch (final StreamNotFoundException e) {
            throw new IntegritySystemException(e.getMessage(), e);
        }

        final String result;
        try {
            result = retrieve(getOrganizationalUnit().getId());
        }
        catch (final OrganizationalUnitNotFoundException e) {
            throw new IntegritySystemException("Error retrieving updated organizational-unit with id " + id + '!', e);
        }
        final String endTimeStamp = getOrganizationalUnit().getLastFedoraModificationDate();
        if (!startTimeStamp.equals(endTimeStamp)) {
            fireOuModified(getOrganizationalUnit().getId(), result);
            updateModifiedParents(parentsBeforeUpdate, parents);
            updateModifiedPredecessors(predecessorsBeforeUpdate, predecessorsHandler.getPredecessors());
        }

        return result;
    }

    /**
     * @param parentsBeforeUpdate
     * @param updatedParents
     * @throws OrganizationalUnitNotFoundException
     *                         e
     * @throws SystemException e
     * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
     * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
     * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
     */
    protected void updateModifiedParents(
        final Collection<String> parentsBeforeUpdate, final Collection<String> updatedParents)
        throws OrganizationalUnitNotFoundException, SystemException, WebserverSystemException,
        TripleStoreSystemException, IntegritySystemException {

        for (final String id : parentsBeforeUpdate) {
            if (!updatedParents.contains(id)) {
                fireOuModified(id, retrieve(id));
            }
        }
        for (final String id : updatedParents) {
            if (!parentsBeforeUpdate.contains(id)) {
                fireOuModified(id, retrieve(id));
            }
        }
    }

    /**
     * Update Predecessors within cache-db.
     *
     * @param predecessorBeforeUpdate List of predecessors before relation where updated.
     * @param updatedPredecessors     List of predecessors after update.
     * @throws OrganizationalUnitNotFoundException
     *                         Thrown if OU with id was not found.
     * @throws SystemException Thrown if update db-cache failed.
     * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
     * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
     * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
     */
    private void updateModifiedPredecessors(
        final Collection<Predecessor> predecessorBeforeUpdate, final Collection<Predecessor> updatedPredecessors)
        throws OrganizationalUnitNotFoundException, SystemException, WebserverSystemException,
        TripleStoreSystemException, IntegritySystemException {

        for (final Predecessor predecessor : predecessorBeforeUpdate) {
            if (!updatedPredecessors.contains(predecessor)) {
                fireOuModified(predecessor.getObjid(), retrieve(predecessor.getObjid()));
            }
        }
        for (final Predecessor predecessor : updatedPredecessors) {
            if (!predecessorBeforeUpdate.contains(predecessor)) {
                fireOuModified(predecessor.getObjid(), retrieve(predecessor.getObjid()));
            }
        }
    }

    /**
     * See Interface for functional description.
     *
     * @throws MissingElementValueException e
     * @throws OptimisticLockingException   e
     * @throws OrganizationalUnitNotFoundException
     *                                      e
     * @throws SystemException              e
     * @throws InvalidStatusException       e
     */
    @Override
    public String updateMdRecords(final String id, final String xml) throws MissingElementValueException,
        OptimisticLockingException, OrganizationalUnitNotFoundException, SystemException, InvalidStatusException,
        EncodingSystemException, IntegritySystemException, FedoraSystemException, TripleStoreSystemException,
        WebserverSystemException, XmlParserSystemException, XmlCorruptedException {

        setOrganizationalUnit(id);
        final String startTimeStamp = getOrganizationalUnit().getLastFedoraModificationDate();
        final StaxParser sp = new StaxParser();
        final OptimisticLockingHandler optimisticLockingHandler =
            new OptimisticLockingHandler(getOrganizationalUnit().getId(), Constants.ORGANIZATIONAL_UNIT_OBJECT_TYPE,
                startTimeStamp);
        sp.addHandler(optimisticLockingHandler);

        final OrganizationalUnitMetadataHandler metadataHandler = new OrganizationalUnitMetadataHandler(sp, "");
        sp.addHandler(metadataHandler);

        final MultipleExtractor2 me = createMultipleExtractor(sp, metadataHandler.getMdRecordPath());
        sp.addHandler(me);
        parseIncomingXmlForUpdate(xml, sp);
        checkName(id, metadataHandler.getDcTitle(), getOrganizationalUnit().getParents());

        final Map<String, Object> relsExtValues = new HashMap<String, Object>();
        final String[] creator = Utility.getCurrentUser();
        relsExtValues.put(XmlTemplateProvider.FRAMEWORK_BUILD_NUMBER, Utility.getBuildNumber());
        relsExtValues.put(XmlTemplateProvider.PUBLIC_STATUS, getOrganizationalUnit().getPublicStatus());
        relsExtValues.put(XmlTemplateProvider.CREATED_BY_ID, getOrganizationalUnit().getCreatedBy());
        relsExtValues.put(XmlTemplateProvider.CREATED_BY_TITLE, getOrganizationalUnit().getCreatedByTitle());
        relsExtValues.put(XmlTemplateProvider.MODIFIED_BY_ID, creator[0]);
        relsExtValues.put(XmlTemplateProvider.MODIFIED_BY_TITLE, creator[1]);
        relsExtValues.put(XmlTemplateProvider.TITLE, metadataHandler.getDcTitle());
        // add predecessors to RELS-EXT
        relsExtValues.put(XmlTemplateProvider.PREDECESSORS, getPredessorsMap(getOrganizationalUnit().getPredecessors(),
            null));

        try {

            setMdRecords((Map<String, ByteArrayOutputStream>) me.getOutputStreams().get(XmlUtility.NAME_MDRECORDS),
                metadataHandler.getMetadataAttributes(), metadataHandler.getEscidocMetadataRecordNameSpace());
            getOrganizationalUnit().setRelsExt(
                getOrganizationalUnitRelsExt(id, relsExtValues, getOrganizationalUnit().getParents()));
        }
        catch (final StreamNotFoundException e) {
            throw new IntegritySystemException(e.getMessage(), e);
        }
        getOrganizationalUnit().persist();

        final String result;
        try {
            result = retrieveMdRecords(getOrganizationalUnit().getId());
        }
        catch (final OrganizationalUnitNotFoundException e) {
            throw new IntegritySystemException("Error retrieving updated organizational-unit with id " + id + '!', e);
        }
        final String endTimeStamp = getOrganizationalUnit().getLastFedoraModificationDate();
        if (!startTimeStamp.equals(endTimeStamp)) {
            fireOuModified(getOrganizationalUnit().getId(), retrieve(getOrganizationalUnit().getId()));
        }
        return result;
    }

    /**
     * See Interface for functional description.
     *
     * @throws InvalidStatusException       e
     * @throws MissingElementValueException e
     * @throws OptimisticLockingException   e
     * @throws OrganizationalUnitHierarchyViolationException
     *                                      e
     * @throws OrganizationalUnitNotFoundException
     *                                      e
     * @throws SystemException              e
     */
    @Override
    public String updateParents(final String id, final String xml) throws MissingElementValueException,
        OptimisticLockingException, OrganizationalUnitHierarchyViolationException, OrganizationalUnitNotFoundException,
        SystemException, InvalidStatusException, EncodingSystemException, IntegritySystemException,
        FedoraSystemException, TripleStoreSystemException, WebserverSystemException, XmlParserSystemException,
        XmlCorruptedException {

        setOrganizationalUnit(id);
        final List<String> parentsBeforeUpdate = getOrganizationalUnit().getParents();
        final String startTimeStamp = getOrganizationalUnit().getLastFedoraModificationDate();
        final StaxParser sp = new StaxParser();
        final OptimisticLockingHandler optimisticLockingHandler =
            new OptimisticLockingHandler(getOrganizationalUnit().getId(), Constants.ORGANIZATIONAL_UNIT_OBJECT_TYPE,
                startTimeStamp);
        sp.addHandler(optimisticLockingHandler);
        final OrganizationalUnitParentsHandler parentsHandler = new OrganizationalUnitParentsHandler(sp);
        parentsHandler.setRootElement(XmlUtility.NAME_PARENTS);
        sp.addHandler(parentsHandler);

        parseIncomingXmlForUpdate(xml, sp);

        final List<String> parents = parentsHandler.getParentOus();
        final OumUtility oumUtility = new OumUtility();
        oumUtility.detectCycles(id, parents);
        checkName(id, getOrganizationalUnit().getName(), parents);

        final Map<String, Object> relsExtValues = new HashMap<String, Object>();
        final String buildNumber = Utility.getBuildNumber();
        relsExtValues.put(XmlTemplateProvider.FRAMEWORK_BUILD_NUMBER, buildNumber);
        relsExtValues.put(TripleStoreUtility.PROP_NAME, getOrganizationalUnit().getName());
        final String[] creator = Utility.getCurrentUser();
        relsExtValues.put(XmlTemplateProvider.PUBLIC_STATUS, getOrganizationalUnit().getPublicStatus());
        relsExtValues.put(XmlTemplateProvider.CREATED_BY_ID, getOrganizationalUnit().getCreatedBy());
        relsExtValues.put(XmlTemplateProvider.CREATED_BY_TITLE, getOrganizationalUnit().getCreatedByTitle());
        relsExtValues.put(XmlTemplateProvider.MODIFIED_BY_ID, creator[0]);
        relsExtValues.put(XmlTemplateProvider.MODIFIED_BY_TITLE, creator[1]);
        relsExtValues.put(TripleStoreUtility.PROP_TITLE, relsExtValues.get(TripleStoreUtility.PROP_NAME));

        // add predecessors to RELS-EXT
        relsExtValues.put(XmlTemplateProvider.PREDECESSORS, getPredessorsMap(getOrganizationalUnit().getPredecessors(),
            null));

        getOrganizationalUnit().setRelsExt(getOrganizationalUnitRelsExt(id, relsExtValues, parents));

        getOrganizationalUnit().persist();

        final String result;
        try {
            result = retrieveParents(getOrganizationalUnit().getId());
        }
        catch (final OrganizationalUnitNotFoundException e) {
            throw new IntegritySystemException("Error retrieving updated organizational-unit with id " + id + '!', e);
        }
        fireOuModified(getOrganizationalUnit().getId(), retrieve(getOrganizationalUnit().getId()));
        updateModifiedParents(parentsBeforeUpdate, parents);

        return result;
    }

    /**
     * See Interface for functional description.
     *
     * @throws OrganizationalUnitNotFoundException
     *                         e
     * @see de.escidoc.core.oum.business.interfaces.OrganizationalUnitHandlerInterface#retrieveProperties(java.lang.String)
     */
    @Override
    public String retrieveProperties(final String id) throws OrganizationalUnitNotFoundException,
        WebserverSystemException, TripleStoreSystemException, IntegritySystemException {

        setOrganizationalUnit(id);
        return getPropertiesXml();
    }

    /**
     * Retrieve a virtual resource by name.
     *
     * @param id           organizational unit id
     * @param resourceName name of the virtual resource
     * @return virtual resource as XML representation
     * @throws OperationNotFoundException thrown if there is no method configured for the given resource name
     * @throws OrganizationalUnitNotFoundException
     *                                    thrown if no organizational unit with that id exists
     * @throws SystemException            If an internal error occurred.
     */
    @Override
    public EscidocBinaryContent retrieveResource(final String id, final String resourceName)
        throws OperationNotFoundException, OrganizationalUnitNotFoundException, SystemException,
        WebserverSystemException, TripleStoreSystemException, IntegritySystemException {

        final EscidocBinaryContent content = new EscidocBinaryContent();
        content.setMimeType(Constants.DEFAULT_MIME_TYPE);

        try {
            if ("relations".equals(resourceName)) {
                content.setContent(new ByteArrayInputStream(retrieveContentRelations(id).getBytes(
                    XmlUtility.CHARACTER_ENCODING)));
            }
            else if ("parent-objects".equals(resourceName)) {
                content.setContent(new ByteArrayInputStream(retrieveParentObjects(id).getBytes(
                    XmlUtility.CHARACTER_ENCODING)));
            }
            else if ("successors".equals(resourceName)) {
                content.setContent(new ByteArrayInputStream(retrieveSuccessors(id).getBytes(
                    XmlUtility.CHARACTER_ENCODING)));
            }
            else if ("child-objects".equals(resourceName)) {
                content.setContent(new ByteArrayInputStream(retrieveChildObjects(id).getBytes(
                    XmlUtility.CHARACTER_ENCODING)));
            }
            else if ("path-list".equals(resourceName)) {
                content.setContent(new ByteArrayInputStream(retrievePathList(id)
                    .getBytes(XmlUtility.CHARACTER_ENCODING)));
            }
            else {
                throw new OperationNotFoundException("no virtual resource with name '" + resourceName + "' defined");
            }
        }
        catch (final UnsupportedEncodingException e) {
            throw new WebserverSystemException(e);
        }

        return content;
    }

    /**
     * See Interface for functional description.
     *
     * @throws OrganizationalUnitNotFoundException
     *                         e
     * @see de.escidoc.core.oum.business.interfaces.OrganizationalUnitHandlerInterface#retrieveResources(java.lang.String)
     */
    @Override
    public String retrieveResources(final String id) throws OrganizationalUnitNotFoundException,
        WebserverSystemException, TripleStoreSystemException, IntegritySystemException {

        setOrganizationalUnit(id);
        return getResourcesXml();
    }

    /**
     * See Interface for functional description.
     *
     * @throws OrganizationalUnitNotFoundException
     *                         e
     * @see de.escidoc.core.oum.business.interfaces.OrganizationalUnitHandlerInterface#retrieveMdRecords(java.lang.String)
     */
    @Override
    public String retrieveMdRecords(final String id) throws OrganizationalUnitNotFoundException,
        WebserverSystemException, TripleStoreSystemException, IntegritySystemException {

        setOrganizationalUnit(id);
        return getMdRecordsXml();
    }

    /**
     * See Interface for functional description.
     *
     * @throws MdRecordNotFoundException e
     * @throws OrganizationalUnitNotFoundException
     *                                   e
     */
    @Override
    public String retrieveMdRecord(final String id, final String name) throws MdRecordNotFoundException,
        OrganizationalUnitNotFoundException, WebserverSystemException, TripleStoreSystemException,
        IntegritySystemException {
        setOrganizationalUnit(id);
        final String mdRecord = getMdRecordXml(name);
        if (mdRecord.length() == 0) {
            throw new MdRecordNotFoundException("Md-record with a name " + name + " does not "
                + " exist in the organization unit with id " + id);
        }
        return getMdRecordXml(name);
    }

    /**
     * See Interface for functional description.
     *
     * @throws OrganizationalUnitNotFoundException
     *                         e
     * @throws SystemException e
     * @see de.escidoc.core.oum.business.interfaces.OrganizationalUnitHandlerInterface#retrieveParents(java.lang.String)
     */
    @Override
    public String retrieveParents(final String id) throws OrganizationalUnitNotFoundException, SystemException,
        TripleStoreSystemException, IntegritySystemException, WebserverSystemException {

        setOrganizationalUnit(id);
        return getParentsXml();
    }

    /**
     * See Interface for functional description.
     *
     * @throws OrganizationalUnitNotFoundException
     *                         e
     * @see de.escidoc.core.oum.business.interfaces.OrganizationalUnitHandlerInterface#retrieveChildObjects(java.lang.String)
     */
    @Override
    public String retrieveChildObjects(final String id) throws OrganizationalUnitNotFoundException,
        TripleStoreSystemException, IntegritySystemException, WebserverSystemException {
        final StringWriter result = new StringWriter();

        Utility.getInstance().checkIsOrganizationalUnit(id);
        sruRequest.searchRetrieve(result, new ResourceType[] { ResourceType.OU }, "\"/parents/parent/id\"=" + id,
            LuceneRequestParameters.DEFAULT_MAXIMUM_RECORDS, LuceneRequestParameters.DEFAULT_START_RECORD, null, null);
        return result.toString();
    }

    /**
     * See Interface for functional description.
     *
     * @throws OrganizationalUnitNotFoundException
     *                         e
     * @throws SystemException e
     * @see de.escidoc.core.oum.business.interfaces.OrganizationalUnitHandlerInterface#retrieveParentObjects(java.lang.String)
     */
    @Override
    public String retrieveParentObjects(final String id) throws OrganizationalUnitNotFoundException, SystemException,
        TripleStoreSystemException, IntegritySystemException, WebserverSystemException {
        final StringWriter result = new StringWriter();

        Utility.getInstance().checkIsOrganizationalUnit(id);
        setOrganizationalUnit(id);

        final StringBuilder filter = new StringBuilder();

        for (final String parent : getOrganizationalUnit().getParents()) {
            if (filter.length() > 0) {
                filter.append(" OR ");
            }
            filter.append("\"/id\"=").append(parent);
        }
        if (filter.length() == 0) {
            //restrict so 0 ous will be found
            filter.append("nonexistingField=nonexistingValue");
        }
        sruRequest.searchRetrieve(result, new ResourceType[] { ResourceType.OU }, filter.toString(),
            LuceneRequestParameters.DEFAULT_MAXIMUM_RECORDS, LuceneRequestParameters.DEFAULT_START_RECORD, null, null);
        return result.toString();
    }

    /**
     * See Interface for functional description.
     *
     * @param id objid of Organizational Unit
     * @return Path list of the OU
     * @throws OrganizationalUnitNotFoundException
     *                         e
     * @throws SystemException e
     * @see de.escidoc.core.oum.business.interfaces.OrganizationalUnitHandlerInterface#retrievePathList(java.lang.String)
     */
    @Override
    public String retrievePathList(final String id) throws OrganizationalUnitNotFoundException, SystemException,
        TripleStoreSystemException, IntegritySystemException, WebserverSystemException {

        setOrganizationalUnit(id);
        return getPathListXml();
    }

    /**
     * See Interface for functional description.
     *
     * @param parameters parameters from the SRU request
     * @return The list of Organizational Units matching filter parameter.
     * @see de.escidoc.core.oum.business.interfaces.OrganizationalUnitHandlerInterface#retrieveOrganizationalUnits(de.escidoc.core.common.business.filter.SRURequestParameters)
     */
    @Override
    public String retrieveOrganizationalUnits(final SRURequestParameters parameters) throws WebserverSystemException {
        final StringWriter result = new StringWriter();

        if (parameters.isExplain()) {
            sruRequest.explain(result, ResourceType.OU);
        }
        else {
            sruRequest.searchRetrieve(result, new ResourceType[] { ResourceType.OU }, parameters);
        }
        return result.toString();
    }

    /**
     * See Interface for functional description.
     *
     * @throws InvalidStatusException     e
     * @throws OptimisticLockingException e
     * @throws OrganizationalUnitNotFoundException
     *                                    e
     */
    @Override
    public String close(final String id, final String taskParam) throws OrganizationalUnitNotFoundException,
        InvalidStatusException, SystemException, OptimisticLockingException, XmlCorruptedException,
        EncodingSystemException, FedoraSystemException, TripleStoreSystemException, WebserverSystemException,
        IntegritySystemException {

        setOrganizationalUnit(id);
        final TaskParamHandler taskParamHandler = XmlUtility.parseTaskParam(taskParam);
        checkUpToDate(taskParamHandler.getLastModificationDate());
        checkInState(Constants.STATUS_OU_CLOSED, Constants.STATUS_OU_OPENED);
        checkWithoutChildrenOrChildrenClosed(Constants.STATUS_OU_CLOSED);
        updateState(Constants.STATUS_OU_CLOSED);
        getOrganizationalUnit().persist();

        fireOuModified(getOrganizationalUnit().getId(), retrieve(getOrganizationalUnit().getId()));

        return getUtility().prepareReturnXmlFromLastModificationDate(getOrganizationalUnit().getLastModificationDate());
    }

    /**
     * See Interface for functional description.
     *
     * @throws InvalidStatusException     e
     * @throws OptimisticLockingException e
     * @throws OrganizationalUnitNotFoundException
     *                                    e
     */
    @Override
    public String open(final String id, final String taskParam) throws OrganizationalUnitNotFoundException,
        InvalidStatusException, SystemException, OptimisticLockingException, XmlCorruptedException,
        EncodingSystemException, FedoraSystemException, TripleStoreSystemException, WebserverSystemException,
        IntegritySystemException {

        setOrganizationalUnit(id);
        final TaskParamHandler taskParamHandler = XmlUtility.parseTaskParam(taskParam);
        checkUpToDate(taskParamHandler.getLastModificationDate());
        checkInState(Constants.STATUS_OU_OPENED, Constants.STATUS_OU_CREATED);
        checkParentsInState(Constants.STATUS_OU_OPENED, Constants.STATUS_OU_OPENED);
        updateState(Constants.STATUS_OU_OPENED);
        getOrganizationalUnit().persist();

        fireOuModified(getOrganizationalUnit().getId(), retrieve(getOrganizationalUnit().getId()));

        return getUtility().prepareReturnXmlFromLastModificationDate(getOrganizationalUnit().getLastModificationDate());
    }

    /**
     * Retrieve all content relation in which the current resource is subject or object.
     *
     * @param id organizational-unit id
     * @return list of content relations
     * @throws OrganizationalUnitNotFoundException
     *                         e Thrown if an item with the specified id could not be found.
     * @throws SystemException If an error occurs.
     * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
     * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
     * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
     */
    private String retrieveContentRelations(final String id) throws OrganizationalUnitNotFoundException,
        SystemException, TripleStoreSystemException, IntegritySystemException, WebserverSystemException {
        final Map<String, String[]> filterParams = new HashMap<String, String[]>();

        setOrganizationalUnit(id);
        filterParams.put("query", new String[] { "\"/subject/id\"=" + getOrganizationalUnit().getId() + " or "
            + "\"/object/id\"=" + getOrganizationalUnit().getId() });

        final String result;
        try {
            final String searchResponse = contentRelationHandler.retrieveContentRelations(filterParams);
            result = transformSearchResponse2relations(searchResponse);
        }
        catch (final InvalidSearchQueryException e) {
            throw new SystemException(e);
        }

        return result;

    }

    /**
     * Set the SRURequest object.
     *
     * @param sruRequest SRURequest
     */
    public void setSruRequest(final SRURequest sruRequest) {
        this.sruRequest = sruRequest;
    }

    /**
     * Injects the indexing handler.
     *
     * @param indexingHandler The indexing handler.
     */
    public void setIndexingHandler(final ResourceListener indexingHandler) {
        addOuListener(indexingHandler);
    }

    /**
     * Render list of successors of Organizational Units.
     *
     * @param objid Organizational Unit Id.
     * @return XML representation of successor list.
     * @throws OrganizationalUnitNotFoundException
     *                         Thrown if no Organizational Unit exists with provided objid.
     * @throws SystemException Thrown if render failed.
     */
    @Override
    public String retrieveSuccessors(final String objid) throws OrganizationalUnitNotFoundException, SystemException,
        TripleStoreSystemException, IntegritySystemException, WebserverSystemException {

        setOrganizationalUnit(objid);
        return getRenderer().renderSuccessors(getOrganizationalUnit());
    }

    /**
     * Get Vector/map structure of Predecessors for Velocity.
     *
     * @param predecessors List of predecessors.
     * @param oUobjid      objid of the current OU
     * @return Velocity vector/map structure of predecessor.
     * @throws OrganizationalUnitNotFoundException
     *                                Thrown if under the Predecessor objid was no Organizational Unit found.
     * @throws InvalidStatusException Thrown if predecessor has same id than current OU (predecessor points to itself).
     * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
     * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
     * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
     */
    private List<Map<String, String>> getPredessorsMap(final List<Predecessor> predecessors, final String oUobjid)
        throws OrganizationalUnitNotFoundException, InvalidStatusException, TripleStoreSystemException,
        IntegritySystemException, WebserverSystemException {

        List<Map<String, String>> predecessorsMap = null;

        if (!predecessors.isEmpty()) {
            predecessorsMap = new ArrayList<Map<String, String>>();
            for (final Predecessor predecessor : predecessors) {
                final Map<String, String> predecessorMap = new HashMap<String, String>();

                // check if predecessor exists and is OU (its not required to
                // check if it does not point to itself, because itself does not
                // exists yet.)
                Utility.getInstance().checkIsOrganizationalUnit(predecessor.getObjid());
                if (oUobjid != null && predecessor.getObjid().equals(oUobjid)) {

                    throw new InvalidStatusException("Organizational Unit points to itself as predecessor.");
                }
                predecessorMap.put(XmlTemplateProvider.PREDECESSOR_FORM, predecessor.getForm().getLabel());
                predecessorMap.put(XmlTemplateProvider.OBJID, predecessor.getObjid());

                // add to the predecessors map
                predecessorsMap.add(predecessorMap);
            }
            // check rules
            checkPredecessorRules(predecessors);
        }

        return predecessorsMap;
    }

    /**
     * Checks if definition of predecessors follows the defined rules.
     *
     * @param predecessors List of predecessors.
     * @throws InvalidStatusException Thrown if predecessor form not follows rules.
     */
    private static void checkPredecessorRules(final List<Predecessor> predecessors) throws InvalidStatusException {

        if (predecessors.size() > 1) {
            /*
             * This must be a fusion: check if every predecessor is set as
             * fusion.
             */
            for (final Predecessor predecessor : predecessors) {
                if (predecessor.getForm() != PredecessorForm.FUSION) {
                    throw new InvalidStatusException("Predecessor forms are inconsistent. At least one "
                        + " predecesssor has not form '" + PredecessorForm.FUSION.getLabel() + "'.");
                }
            }
        }
        else {
            /*
             * check replacement: only one predecessor is allowed
             */
            if (predecessors.get(0).getForm() == PredecessorForm.FUSION) {
                throw new InvalidStatusException("Predecessor form to '" + PredecessorForm.FUSION.getLabel()
                    + "' requires more than on predecessor.");
            }
        }
    }
}
