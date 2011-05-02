/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
 * only (the "License"). You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
 * the specific language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
 * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
 * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
 * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
 * terms.
 */

package de.escidoc.core.common.business.fedora.resources;

import de.escidoc.core.common.business.Constants;
import de.escidoc.core.common.business.PropertyMapKeys;
import de.escidoc.core.common.business.fedora.TripleStoreUtility;
import de.escidoc.core.common.business.fedora.Utility;
import de.escidoc.core.common.business.fedora.datastream.Datastream;
import de.escidoc.core.common.business.fedora.resources.interfaces.ContainerInterface;
import de.escidoc.core.common.exceptions.application.notfound.ContainerNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException;
import de.escidoc.core.common.exceptions.system.EncodingSystemException;
import de.escidoc.core.common.exceptions.system.FedoraSystemException;
import de.escidoc.core.common.exceptions.system.IntegritySystemException;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
import de.escidoc.core.common.util.stax.StaxParser;
import de.escidoc.core.common.util.stax.handler.DcReadHandler;
import de.escidoc.core.common.util.stax.handler.RelsExtContentRelationsReadHandler;
import de.escidoc.core.common.util.xml.Elements;
import de.escidoc.core.common.util.xml.XmlUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Implementation of a Fedora Container Object which consist of datastreams managed in Fedora Digital Repository
 * System.
 *
 * @author Frank Schwichtenberg
 */
public class Container extends GenericVersionableResourcePid implements ContainerInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(Container.class);

    private Datastream dc;

    private Map<String, Datastream> mdRecords;

    private Datastream cts;

    private String contextId;

    private Datastream escidocRelsExt;

    public static final String DATASTREAM_ESCIDOC_RELS_EXT = "ESCIDOC_RELS_EXT";

    /**
     * Constructor of Container with specified id. The datastreams are instantiated and retrieved if the related getter
     * is called.
     *
     * @param id The id of an container managed in Fedora.
     * @throws StreamNotFoundException   Thrown if a datastream could not be found.
     * @throws SystemException           Thrown in case of an internal error.
     * @throws ResourceNotFoundException Thrown if no container could be found under the provided id.
     * @throws de.escidoc.core.common.exceptions.application.notfound.ContainerNotFoundException
     * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
     * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
     * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
     * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
     */
    public Container(final String id) throws StreamNotFoundException, SystemException, ResourceNotFoundException,
        ContainerNotFoundException, IntegritySystemException, TripleStoreSystemException, XmlParserSystemException,
        WebserverSystemException {

        super(id);
        setPropertiesNames(expandPropertiesNames(getPropertiesNames()),
            expandPropertiesNamesMapping(getPropertiesNamesMapping()));

        Utility.getInstance().checkIsContainer(getId());

        setHref(Constants.CONTAINER_URL_BASE + getId());
        getVersionData();
        if (getVersionNumber() != null) {

            setDcData();
        }
        this.mdRecords = new HashMap<String, Datastream>();

    }

    /**
     * Get creation date of a versionated resource.
     * <p/>
     * Attention: The creation date of a resource differs from the creation date in the Fedora resource.
     *
     * @return creation date
     * @throws TripleStoreSystemException Thrown if request to TripleStore failed.
     * @throws WebserverSystemException   Thrown in case of internal error.
     */
    @Override
    public String getCreationDate() throws TripleStoreSystemException, WebserverSystemException {

        if (this.creationDate == null) {
            /*
             * The creation version date is the date of the first version. This
             * is not the creation date of the Fedora object! With Fedora
             * 3.2/3.3 is the date indirectly obtained from the date of the
             * first RELS-EXT version or from the version/date entry of the
             * second version of RELS-EXT or the 'created date' of the RELS-EXT
             * datastream.
             * 
             * Another way would be to obtain the creation date from the WOV.
             * 
             * The current implementation derives the creation date from the
             * 'created Date' of the RELS-EXT datastream.
             */

            try {
                final org.fcrepo.server.types.gen.Datastream[] datastreams =
                    getFedoraUtility().getDatastreamHistory(getId(), DATASTREAM_ESCIDOC_RELS_EXT);
                this.creationDate = datastreams[datastreams.length - 1].getCreateDate();
            }
            catch (final FedoraSystemException e) {
                throw new WebserverSystemException(e);
            }

        }
        return this.creationDate;
    }

    /**
     * Obtain title and description from DC an write them to properties map.
     *
     * @throws XmlParserSystemException Thrown if parsing of DC failed.
     */
    private void setDcData() throws XmlParserSystemException {
        /*
         * TODO maybe parsing could be removed, because values from DC are
         * written to TrippleSTore by Fedora.
         */
        // parse DC data stream
        final StaxParser sp = new StaxParser();
        final DcReadHandler dch = new DcReadHandler(sp);
        sp.addHandler(dch);
        try {
            sp.parse(this.getDc().getStream());
        }
        catch (final Exception e) {
            throw new XmlParserSystemException("Unexpected exception.", e);
        }
        setTitle(dch.getPropertiesMap().get(Elements.ELEMENT_DC_TITLE));
        setDescription(dch.getPropertiesMap().get(Elements.ELEMENT_DESCRIPTION));
    }

    /**
     * Get Content Model Specific.
     *
     * @return content model specific datastream (if exist)
     * @throws StreamNotFoundException If datastream with content-model-specific id does not exist
     * @throws FedoraSystemException   If access to Fedora fail
     */
    public Datastream getCts() throws StreamNotFoundException, FedoraSystemException {
        if (this.cts == null) {
            try {
                this.cts = new Datastream(Elements.ELEMENT_CONTENT_MODEL_SPECIFIC, getId(), getVersionDate());
            }
            catch (final WebserverSystemException e) {
                throw new FedoraSystemException(e);
            }
        }
        return this.cts;
    }

    /**
     *
     * @param ds
     * @throws StreamNotFoundException
     * @throws FedoraSystemException
     * @throws TripleStoreSystemException
     * @throws WebserverSystemException
     */
    public void setCts(final Datastream ds) throws StreamNotFoundException, FedoraSystemException,
        TripleStoreSystemException, WebserverSystemException {
        try {
            final Datastream curDs = getCts();
            if (!ds.equals(curDs)) {
                ds.merge();
                this.cts = ds;
            }
        }
        catch (final StreamNotFoundException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Error on setting datastream.");
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error on setting datastream.", e);
            }
            // this is not an update; its a create
            ds.persist(false);
            this.cts = ds;
        }
        // FedoraException when datastreams are preinitialized (see Item) and
        // getCts does not throw an exception on non-existing datastream.
        catch (final FedoraSystemException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Error on setting datastream.");
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error on setting datastream.", e);
            }
            // this is not an update; its a create
            ds.persist(false);
            this.cts = ds;
        }
    }

    /**
     * See Interface for functional description.
     *
     * @return MdRecords HashMap
     */
    @Override
    public Map<String, Datastream> getMdRecords() throws IntegritySystemException, FedoraSystemException,
        WebserverSystemException {

        final Map<String, Datastream> result = new HashMap<String, Datastream>();
        final org.fcrepo.server.types.gen.Datastream[] datastreams = getDatastreamInfos();

        final Collection<String> names = new ArrayList<String>();
        for (final org.fcrepo.server.types.gen.Datastream datastream : datastreams) {
            final List<String> altIDs = Arrays.asList(datastream.getAltIDs());
            if (altIDs != null && altIDs.contains(Datastream.METADATA_ALTERNATE_ID)) {
                names.add(datastream.getID());
            }
        }
        for (final String name : names) {
            try {
                final Datastream newDs = new Datastream(name, getId(), getVersionDate());
                result.put(name, newDs);
            }
            catch (final StreamNotFoundException e) {
                final String message = "Metadata record \"" + name + "\" not found for container " + getId() + '.';
                LOGGER.error(message, e);
                throw new IntegritySystemException(message, e);
            }
            catch (final WebserverSystemException e) {
                // FIXME getVersionDate throws an WebserverSystemException in
                // case of IntegritySystemException
                throw new FedoraSystemException(e);
            }
        }
        this.mdRecords = result;
        return result;
    }

    /**
     * @throws WebserverSystemException Thrown in case of an internal error.
     */
    @Override
    public void setMdRecords(final Map<String, Datastream> mdRecords) throws FedoraSystemException,
        WebserverSystemException, TripleStoreSystemException, IntegritySystemException, EncodingSystemException {
        // check if mdRecords is set, contains all metadata
        // Datastreams, is equal to given mdRecords and save every
        // changed data stream to fedora

        // get list of names of data streams with alternateId = "metadata"
        final Set<String> namesInFedora = getMdRecords().keySet();

        // delete data streams which are in fedora but not in mdRecords
        for (final String nameInFedora : namesInFedora) {
            if (!mdRecords.containsKey(nameInFedora)) {
                try {
                    final Datastream fedoraDs = getMdRecord(nameInFedora);
                    if (fedoraDs != null) {
                        // FIXME remove the entire datastream
                        fedoraDs.delete();
                    }
                }
                catch (final StreamNotFoundException e) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Failed to set MdRecords.");
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Failed to set MdRecords.", e);
                    }
                }
            }
        }

        // create or activate data streams which are in mdRecords but not in
        // fedora
        final Iterator<Entry<String, Datastream>> nameIt = mdRecords.entrySet().iterator();
        while (nameIt.hasNext()) {
            final Entry<String, Datastream> mapEntry = nameIt.next();
            final String name = mapEntry.getKey();
            if (namesInFedora.contains(name)) {
                // update Datastreams which already exist
                setMdRecord(name, mdRecords.get(name));
            }
            else {

                final Datastream currentMdRecord = mapEntry.getValue();
                final byte[] stream = currentMdRecord.getStream();
                final List<String> altIds = currentMdRecord.getAlternateIDs();
                final String[] altIDs = new String[altIds.size()];
                for (int i = 0; i < altIds.size(); i++) {
                    altIDs[i] = altIds.get(i);
                }
                getFedoraUtility().addDatastream(getId(), name, altIDs, "md-record", true, stream, false);
                this.mdRecords.put(name, currentMdRecord);
                nameIt.remove();
            }
        }
    }

    @Override
    public Datastream getMdRecord(final String name) throws StreamNotFoundException, FedoraSystemException {
        // check if the ds is set
        if (!this.mdRecords.containsKey(name)) {
            // retrieve from fedora and add to map
            final Datastream ds;
            try {
                ds = new Datastream(name, getId(), getVersionDate());
            }
            catch (final WebserverSystemException e) {
                throw new FedoraSystemException(e);
            }
            this.mdRecords.put(name, ds);
        }
        return this.mdRecords.get(name);
    }

    /**
     * @throws WebserverSystemException Thrown in case of an internal error.
     */
    @Override
    public void setMdRecord(final String name, final Datastream ds) throws WebserverSystemException,
        FedoraSystemException, TripleStoreSystemException, EncodingSystemException, IntegritySystemException {
        // check if the metadata datastream is set, is equal to ds and save to
        // fedora

        // don't trust the handler
        // ds.addAlternateId("metadata");
        final String type = ds.getAlternateIDs().get(1);
        final String schema = ds.getAlternateIDs().get(2);
        final String mimeType = ds.getMimeType();
        try {
            final Datastream curDs = getMdRecord(name);
            final String curMimeType = curDs.getMimeType();
            String curType = "";
            String curSchema = "";
            final List<String> altIds = curDs.getAlternateIDs();
            if (altIds.size() > 1) {
                curType = altIds.get(1);
                if (altIds.size() > 2) {
                    curSchema = altIds.get(2);
                }
            }
            boolean contentChanged = false;
            if (!ds.equals(curDs)) {
                contentChanged = true;
            }
            if (contentChanged || !type.equals(curType) || !schema.equals(curSchema) || !mimeType.equals(curMimeType)) {
                if (contentChanged && "escidoc".equals(name)) {

                    final Map<String, String> mdProperties = ds.getProperties();
                    if (mdProperties != null) {
                        if (mdProperties.containsKey("nsUri")) {
                            final String nsUri = mdProperties.get("nsUri");
                            final String dcNewContent =
                                XmlUtility.createDC(nsUri, ds.toStringUTF8(), getId(),
                                    getResourcePropertiesValue(PropertyMapKeys.CURRENT_VERSION_CONTENT_MODEL_ID));
                            if (dcNewContent != null && dcNewContent.trim().length() > 0) {
                                final Datastream dcNew;
                                try {
                                    dcNew =
                                        new Datastream("DC", getId(), dcNewContent
                                            .getBytes(XmlUtility.CHARACTER_ENCODING), Datastream.MIME_TYPE_TEXT_XML);
                                }
                                catch (final UnsupportedEncodingException e) {
                                    throw new EncodingSystemException(e);
                                }
                                setDc(dcNew);
                            }
                        }
                        else {
                            throw new IntegritySystemException("namespace uri of 'escidoc' metadata"
                                + " does not set in datastream.");
                        }
                    }
                    else {
                        throw new IntegritySystemException("Properties of 'md-record' datastream"
                            + " with then name 'escidoc' does not exist");
                    }
                }

                this.mdRecords.put(name, ds);
                ds.merge();

            }
        }
        catch (final StreamNotFoundException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Failed to set MdRecords.");
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Failed to set MdRecords.", e);
            }
            // this is not an update; its a create
            ds.addAlternateId(type);
            ds.addAlternateId(schema);
            this.mdRecords.put(name, ds);
            ds.persist(false);
        }
    }

    @Override
    public Datastream getMembers() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Get the ESCIDOC_RELS_EXT for the corresponding version of the Resource.
     *
     * @return ESCIDOC_RELS_EXT corresponding to the Resource version.
     * @throws StreamNotFoundException Thrown if the ESCIDOC_RELS_EXT data stream (with specified version) was not
     *                                 found.
     * @throws FedoraSystemException   Thrown in case of internal error.
     */

    public Datastream getEscidocRelsExt() throws StreamNotFoundException, FedoraSystemException {

        if (this.escidocRelsExt == null) {
            try {
                // TODO: USe a different constructor to set a correct control
                // group
                if (isLatestVersion()) {
                    setEscidocRelsExt(new Datastream(DATASTREAM_ESCIDOC_RELS_EXT, getId(), null));
                }
                else {
                    setEscidocRelsExt(new Datastream(DATASTREAM_ESCIDOC_RELS_EXT, getId(), getVersionDate()));
                }
            }
            catch (final WebserverSystemException e) {
                throw new FedoraSystemException(e);
            }
        }

        return this.escidocRelsExt;
    }

    /**
     * See Interface for functional description.
     *
     * @param ds The ESCIDOC_RELS_EXT datasream.
     * @throws StreamNotFoundException  Thrown if the datastream was not found.
     * @throws FedoraSystemException    Thrown if Fedora request failed.
     * @throws WebserverSystemException Thrown in case of internal failure.
     * @see FedoraResource#setRelsExt(Datastream)
     */
    public void setEscidocRelsExt(final Datastream ds) throws StreamNotFoundException, FedoraSystemException,
        WebserverSystemException {

        if (this.escidocRelsExt == null || !this.escidocRelsExt.equals(ds)) {
            this.escidocRelsExt = ds;

        }
    }

    @Override
    public void setMembers(final Datastream ds) {
        // TODO Auto-generated method stub

    }

    /**
     * Persists the whole object to Fedora.
     *
     * @return lastModificationDate of the resource (Attention this timestamp differs from the last-modification
     *         timestamp of the repository. See Versioning Concept.)
     * @throws FedoraSystemException    Thrown if connection to Fedora failed.
     * @throws WebserverSystemException Thrown in case of internal error.
     */
    @Override
    public String persist() throws FedoraSystemException, WebserverSystemException {
        /*
         * Persist persists the data streams of the object and updates all
         * version depending values. These values are RELS-EXT (version/date)
         * and WOV timestamp.
         * 
         * It is assumed that all data (except timestamp information) are
         * up-to-date in the datastreams! Afterwards should no operations be
         * necessary.
         * 
         * Procedure to persist an resource with versions:
         * 
         * 1.write ESCIDOC_RELS_EXT with a content of the RELS-EXT data stream.
         * 
         * 2. get last-modifcation-date from fedora object (We need the
         * timestamp from ESCIDOC_RELS_EXT precisely. But if no other method
         * writes (hopefully) to this object so we can use the object
         * timestamp.)
         * 
         * 3. write the timestamp to the WOV
         * 
         * 4. Update RELS-EXT (/version/date) with the timestamp (which is
         * written to WOV)
         * 
         * Note: These are to many data stream updates to write one single
         * information (timestamp)
         */
        String timestamp = null;
        if (this.isNeedSync()) {
            // ----------------------------------------------
            // writing RELS-EXT once (problem: /version/date is to old)
            // timestamp = getLastFedoraModificationDate();
            //
            // updateRelsExtVersionTimestamp(timestamp);
            // persistRelsExt();
            // updateWovTimestamp(getVersionNumber(), timestamp);
            // persistWov();

            // ----------------------------------------------
            // writing RELS-EXT twice.
            timestamp = persistEscidocRelsExt();
            if (timestamp == null) {
                timestamp = getLastFedoraModificationDate();
            }
            updateWovTimestamp(getVersionNumber(), timestamp);
            persistWov();
            updateRelsExtVersionTimestamp(timestamp);
            persistRelsExt();
            setResourceProperties(PropertyMapKeys.LAST_MODIFICATION_DATE, timestamp);
        }

        getFedoraUtility().sync();

        return timestamp;
    }

    /**
     * @return Vector with HashMaps of relations.
     */
    @Override
    public List<Map<String, String>> getRelations() throws FedoraSystemException, IntegritySystemException,
        XmlParserSystemException, WebserverSystemException {

        final Datastream datastreamWithRelations;
        try {
            datastreamWithRelations = getVersionNumber() == null ? getRelsExt() : getEscidocRelsExt();
        }
        catch (final StreamNotFoundException e1) {
            throw new IntegritySystemException("Datastream not found.", e1);
        }
        final byte[] datastreamWithRelationsContent = datastreamWithRelations.getStream();

        final StaxParser sp = new StaxParser();
        final ByteArrayInputStream relsExtInputStream = new ByteArrayInputStream(datastreamWithRelationsContent);

        final RelsExtContentRelationsReadHandler reHandler = new RelsExtContentRelationsReadHandler(sp);
        sp.addHandler(reHandler);
        try {
            sp.parse(relsExtInputStream);
        }
        catch (final WebserverSystemException e) {
            throw e;
        }
        catch (final Exception e) {
            XmlUtility.handleUnexpectedStaxParserException("", e);
        }
        return reHandler.getRelations();
    }

    /**
     * Write ESCIDOC_RELS_EXT to Fedora.
     *
     * @return new timestamp of data stream (or null if not updated)
     * @throws FedoraSystemException    Thrown if connection to Fedora failed.
     * @throws WebserverSystemException Thrown in case of internal error.
     */
    protected String persistEscidocRelsExt() throws FedoraSystemException, WebserverSystemException {

        // old timestamp instead of null.
        try {
            if (this.escidocRelsExt != null) {
                this.escidocRelsExt.setStream(getRelsExt().getStream());
            }
            else {
                this.escidocRelsExt =
                    new Datastream(DATASTREAM_ESCIDOC_RELS_EXT, getId(), getRelsExt().getStream(),
                        Datastream.MIME_TYPE_TEXT_XML);
                escidocRelsExt.setControlGroup("M");
                setEscidocRelsExt(this.escidocRelsExt);

            }
        }
        catch (final StreamNotFoundException e) {
            throw new WebserverSystemException("RELS-EXT datastream not found in" + " container with id " + getId(), e);
        }
        return this.escidocRelsExt.merge();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.escidoc.core.om.business.fedora.resources.interfaces.FedoraResource
     * #setDc(de.escidoc.core.common.business.fedora.datastream.Datastream)
     */
    public void setDc(final Datastream ds) throws StreamNotFoundException, FedoraSystemException,
        WebserverSystemException, TripleStoreSystemException {
        // TODO should lock only be checked in handler?
        // if (this.isLocked) {
        // throw new LockingException("Item " + getId() + " is locked.");
        // }
        // check if relsExt is set, is equal to ds and save to fedora
        try {

            final Datastream curDs = getDc();

            if (!ds.equals(curDs)) {

                this.dc = ds;
                ds.merge();
            }
        }
        catch (final StreamNotFoundException e) {
            // An item have to have a RELS-EXT datastream
            throw new StreamNotFoundException("No DC for item " + getId() + '.', e);
        }
        getSomeValuesFromFedora();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.escidoc.core.om.business.fedora.resources.interfaces.FedoraResource
     * #getDc()
     */
    public Datastream getDc() throws StreamNotFoundException, FedoraSystemException {
        if (this.dc == null) {
            try {
                this.dc = new Datastream(Datastream.DC_DATASTREAM, getId(), getVersionDate());
            }
            catch (final WebserverSystemException e) {
                throw new FedoraSystemException(e);
            }
        }
        return this.dc;
    }

    /**
     * Get the Id of the context.
     *
     * @return context id
     * @throws WebserverSystemException Thrown if the id could not retrieved from the TripleStore.
     */
    public String getContextId() throws WebserverSystemException {

        if (this.contextId == null) {
            try {
                this.contextId =
                    TripleStoreUtility.getInstance().getPropertiesElements(getId(), TripleStoreUtility.PROP_CONTEXT_ID);
            }
            catch (final TripleStoreSystemException e) {
                throw new WebserverSystemException(e);
            }
        }

        return this.contextId;
    }

    /**
     * Get the href of the Context.
     *
     * @return href of context.
     * @throws WebserverSystemException Thrown if determining of contextId failed.
     */
    public String getContextHref() throws WebserverSystemException {

        return Constants.CONTEXT_URL_BASE + getContextId();
    }

    /**
     * The Title of the Container Context.
     *
     * @return title of Context
     * @throws WebserverSystemException Thrown in case of internal failure.
     */
    public String getContextTitle() throws WebserverSystemException {

        final String contextTitle;

        try {
            contextTitle =
                TripleStoreUtility.getInstance().getPropertiesElements(getId(), TripleStoreUtility.PROP_CONTEXT_TITLE);
        }
        catch (final TripleStoreSystemException e) {
            throw new WebserverSystemException(e);
        }

        return contextTitle;
    }

    /**
     * Expand a list with names of properties values with the propertiesNames for a versionated resource. These list
     * could be used to request the TripleStore.
     *
     * @param propertiesNames Collection of propertiesNames. The collection contains only the version resource specific
     *                        propertiesNames.
     * @return Parameter name collection
     */
    private static Collection<String> expandPropertiesNames(final Collection<String> propertiesNames) {

        final Collection<String> newPropertiesNames =
            propertiesNames != null ? propertiesNames : new ArrayList<String>();

        newPropertiesNames.add(TripleStoreUtility.PROP_CONTENT_MODEL_TITLE);
        newPropertiesNames.add(TripleStoreUtility.PROP_CONTENT_CATEGORY);

        return newPropertiesNames;
    }

    /**
     * Expanding the properties naming map.
     *
     * @param propertiesMapping The properties name mapping from external as key and the internal name as value. E.g.
     *                          with the key "version-status" and "LATEST_VERSION_STATUS" as value is the value of
     *                          "versin-status" after the mapping accessible with the internal key
     *                          "LATEST_VERSION_STATUS".
     * @return The key mapping.
     */
    private static Map<String, String> expandPropertiesNamesMapping(final Map<String, String> propertiesMapping) {

        final Map<String, String> newPropertiesNames =
            propertiesMapping != null ? propertiesMapping : new HashMap<String, String>();

        newPropertiesNames.put(TripleStoreUtility.PROP_LATEST_VERSION_PID, PropertyMapKeys.LATEST_VERSION_PID);
        newPropertiesNames.put(TripleStoreUtility.PROP_CONTENT_CATEGORY,
            PropertyMapKeys.LATEST_VERSION_CONTENT_CATEGORY);
        // FIXME release is a method of Item/Container so this is to move higher
        // within the hirarchie
        newPropertiesNames.put(TripleStoreUtility.PROP_LATEST_RELEASE_PID, PropertyMapKeys.LATEST_RELEASE_PID);

        return newPropertiesNames;
    }

    /**
     * Get Whole Object Version datastream (WOV).
     *
     * @return WOV
     * @throws StreamNotFoundException Thrown if wov datastream was not found.
     * @throws FedoraSystemException   Thrown in case of Fedora error.
     */
    @Override
    public Datastream getWov() throws StreamNotFoundException, FedoraSystemException {

        if (this.wov == null) {
            this.wov = new Datastream(DATASTREAM_WOV, getId(), null);
            this.wov.setControlGroup("M");
        }
        return this.wov;
    }

    /**
     * Write the WOV data stream to Fedora repository.
     *
     * @param ds The WOV data stream.
     * @throws StreamNotFoundException    Thrown if the WOV data stream was not found.
     * @throws FedoraSystemException      Thrown in case of Fedora error.
     */
    @Override
    public void setWov(final Datastream ds) throws FedoraSystemException, StreamNotFoundException {
        if (!getWov().equals(ds)) {
            ds.setControlGroup("M");
            this.wov = ds;
            this.setNeedSync(true);
        }
    }
}
