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
 * Copyright 2006-2009 Fachinformationszentrum Karlsruhe Gesellschaft
 * fuer wissenschaftlich-technische Information mbH and Max-Planck-
 * Gesellschaft zur Foerderung der Wissenschaft e.V.
 * All rights reserved.  Use is subject to license terms.
 */
package de.escidoc.core.adm.business.admin;

import de.escidoc.core.adm.business.renderer.interfaces.AdminRendererInterface;
import de.escidoc.core.common.business.Constants;
import de.escidoc.core.common.business.fedora.TripleStoreUtility;
import de.escidoc.core.common.business.fedora.Utility;
import de.escidoc.core.common.business.fedora.resources.ResourceType;
import de.escidoc.core.common.business.indexing.IndexingHandler;
import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.system.ApplicationServerSystemException;
import de.escidoc.core.common.exceptions.system.EncodingSystemException;
import de.escidoc.core.common.exceptions.system.FedoraSystemException;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.common.util.configuration.EscidocConfiguration;
import de.escidoc.core.common.util.stax.handler.TaskParamHandler;
import de.escidoc.core.common.util.xml.XmlUtility;
import de.escidoc.core.purge.PurgeRequest;
import de.escidoc.core.purge.PurgeRequestBuilder;
import de.escidoc.core.purge.PurgeService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;

/**
 * Administration tool that rebuilds the search index, rebuilds the resource cache and deletes objects physically from
 * the repository.
 *
 * @author André Schenk
 */
public class AdminHandler {

    private Examples examples;

    private FrameworkInfo frameworkInfo;

    private Reindexer reindexer;

    private Utility utility;

    private IndexingHandler indexingHandler;

    private AdminRendererInterface renderer;

    private PurgeService purgeService;

    private TripleStoreUtility tripleStoreUtility;

    /**
     * Delete a list of objects given by their object id's from Fedora. In case of items this method will also delete
     * all depending components of the given items. The deletion runs synchronously and returns some useful information
     * for the user, e.g. the total number of objects deleted.
     *
     * @param taskParam list of object id's to be deleted boolean value to signal if the search index and the resource
     *                  cache have to be kept in sync. If this value is set to false then the re-indexing and re-caching
     *                  should be run manually afterwards.
     * @return total number of objects deleted, ...
     * @throws SystemException     thrown in case of an internal error
     * @throws de.escidoc.core.common.exceptions.system.ApplicationServerSystemException
     * @throws de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException
     * @throws de.escidoc.core.common.exceptions.system.EncodingSystemException
     */
    public String deleteObjects(final String taskParam) throws SystemException, EncodingSystemException,
        ApplicationServerSystemException, XmlCorruptedException {
        final StringBuilder result = new StringBuilder();
        final PurgeStatus purgeStatus = PurgeStatus.getInstance();

        if (purgeStatus.startMethod()) {
            final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(taskParam, false);

            try {
                for (final String id : taskParameter.getIds()) {
                    final PurgeRequest purgeRequest =
                        PurgeRequestBuilder.createPurgeRequest().withResourceId(id).build();
                    this.purgeService.purge(purgeRequest);
                    if (taskParameter.getKeepInSync()) {
                        // synchronize search index
                        reindexer.sendDeleteObjectMessage(id);
                    }
                    purgeStatus.inc();
                }
                result.append("<message>\n");
                result.append("scheduling ").append(taskParameter.getIds().size()).append(
                    " objects(s) for deletion from Fedora\n");
                result.append("</message>\n");
                if (taskParameter.getKeepInSync()) {
                    result.append("<message>\n");
                    result.append("scheduling ").append(taskParameter.getIds().size()).append(
                        " object(s) for deletion from search index\n");
                    result.append("</message>\n");
                }
            }
            finally {
                if (taskParameter.getIds().isEmpty()) {
                    purgeStatus.finishMethod();
                }
                purgeStatus.setFillingComplete();
            }
        }
        else {
            result.append("<message>\n");
            result.append(purgeStatus);
            result.append("</message>\n");
        }
        return getUtility().prepareReturnXml(result.toString());
    }

    /**
     * Get the current status of the running/finished purging process.
     *
     * @return current status (how many objects are still in the queue)
     * @throws SystemException thrown in case of an internal error
     */
    public String getPurgeStatus() throws SystemException {
        return getUtility().prepareReturnXml(PurgeStatus.getInstance().toString());
    }

    /**
     * Get the current status of the running/finished reindexing process.
     *
     * @return current status (how many objects are still in the queue)
     * @throws SystemException thrown in case of an internal error
     */
    public String getReindexStatus() throws SystemException {
        return getUtility().prepareReturnXml(reindexer.getStatus());
    }

    /**
     * decrease the type of the current status of the running reindexing process by 1.
     *
     * @param objectType object-type to decrease
     */
    public void decreaseReindexStatus(final String objectType) {
        if (objectType != null) {
            ReindexStatus.getInstance().dec(ResourceType.getResourceTypeFromUri(objectType));
        }
    }

    /**
     * @return Returns the utility.
     */
    private Utility getUtility() {
        if (this.utility == null) {
            this.utility = Utility.getInstance();
        }
        return this.utility;
    }

    /**
     * Reinitialize the search index. The initialization runs synchronously and returns some useful information for the
     * user, e.g. the total number of objects found.
     *
     * @param clearIndex      clear the index before adding objects to it
     * @param indexNamePrefix name of the index (may be null for "all indexes")
     * @return total number of objects found, ...
     * @throws SystemException             Thrown if a framework internal error occurs.
     * @throws InvalidSearchQueryException thrown if the given search query could not be translated into a SQL query
     * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
     * @throws de.escidoc.core.common.exceptions.system.ApplicationServerSystemException
     * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
     */
    public String reindex(final boolean clearIndex, final String indexNamePrefix) throws SystemException,
        InvalidSearchQueryException, ApplicationServerSystemException, FedoraSystemException, WebserverSystemException {
        return getUtility().prepareReturnXml(reindexer.reindex(clearIndex, indexNamePrefix));
    }

    /**
     * Provides a xml structure containing the index-configuration.
     *
     * @return xml structure with index configuration
     * @throws WebserverSystemException   if anything goes wrong.
     * @throws TripleStoreSystemException if anything goes wrong.
     * @throws EncodingSystemException    if anything goes wrong.
     */
    public String getIndexConfiguration() throws WebserverSystemException, TripleStoreSystemException,
        EncodingSystemException {

        final Map<String, Map<String, Map<String, Object>>> indexConfiguration =
            indexingHandler.getObjectTypeParameters();
        return renderer.renderIndexConfiguration(indexConfiguration);
    }

    /**
     * Provides a xml structure containing public configuration properties of escidoc-core framework and the earliest
     * creation date of Escidoc repository objects.
     *
     * @return xml structure with escidoc configuration properties
     * @throws WebserverSystemException   if anything go wrong.
     * @throws TripleStoreSystemException if anything go wrong.
     * @throws EncodingSystemException    if anything go wrong.
     */
    public String getRepositoryInfo() throws WebserverSystemException, TripleStoreSystemException,
        EncodingSystemException {

        final String earliestCreationDate = tripleStoreUtility.getEarliestCreationDate();
        final EscidocConfiguration config;
        try {
            config = EscidocConfiguration.getInstance();
        }
        catch (final IOException e) {
            throw new WebserverSystemException(e);
        }

        final Properties properties = new Properties();
        final String gsearchUrl = config.get(EscidocConfiguration.GSEARCH_URL);

        final String buildNr = config.get(EscidocConfiguration.BUILD_NUMBER);

        if (buildNr != null) {
            properties.setProperty(EscidocConfiguration.BUILD_NUMBER, buildNr);
        }
        if (gsearchUrl != null) {
            properties.setProperty(EscidocConfiguration.GSEARCH_URL, gsearchUrl);
        }
        final String baseUrl = config.get(EscidocConfiguration.ESCIDOC_CORE_BASEURL);
        if (baseUrl != null) {
            properties.setProperty(EscidocConfiguration.ESCIDOC_CORE_BASEURL, baseUrl);
        }
        final String name = config.get(EscidocConfiguration.ESCIDOC_REPOSITORY_NAME);
        if (name != null) {
            properties.setProperty(EscidocConfiguration.ESCIDOC_REPOSITORY_NAME, name);
        }
        final String email = config.get(EscidocConfiguration.ADMIN_EMAIL);
        if (email != null) {
            properties.setProperty(EscidocConfiguration.ADMIN_EMAIL, email);
        }
        properties.setProperty("escidoc-core.earliest-date", earliestCreationDate);
        properties.setProperty("escidoc-core.database.version", frameworkInfo.getVersion().toString());
        try {
            properties.setProperty("escidoc-core.database.consistent", String.valueOf(frameworkInfo.isConsistent()));
        }
        catch (final Exception e) {
            throw new WebserverSystemException(e);
        }

        // add namespace of important schemas
        properties.putAll(schemaNamespaces());

        final String checksumAlgorithm = config.get(EscidocConfiguration.ESCIDOC_CORE_OM_CONTENT_CHECKSUM_ALGORITHM);
        if (checksumAlgorithm != null) {
            properties.setProperty(EscidocConfiguration.ESCIDOC_CORE_OM_CONTENT_CHECKSUM_ALGORITHM, checksumAlgorithm);
        }

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            properties.storeToXML(os, null);
        }
        catch (final IOException e) {
            throw new WebserverSystemException(e);
        }
        final String propertiesXml;
        try {
            propertiesXml = os.toString(XmlUtility.CHARACTER_ENCODING);
        }
        catch (final UnsupportedEncodingException e) {
            throw new EncodingSystemException(e);
        }
        return propertiesXml;
    }

    /**
     * Namespace of (important) schemas.
     *
     * @return Properties with name and Namespace URI of important eSciDoc schemas
     */
    private static Map schemaNamespaces() {

        final Properties p = new Properties();

        p.setProperty("item", Constants.ITEM_NAMESPACE_URI);
        p.setProperty("container", Constants.CONTAINER_NAMESPACE_URI);
        p.setProperty("organizational-unit", Constants.ORGANIZATIONAL_UNIT_NAMESPACE_URI);
        p.setProperty("context", Constants.CONTEXT_NAMESPACE_URI);
        p.setProperty("user-account", Constants.USER_ACCOUNT_NS_URI);

        return p;
    }

    /**
     * Loads an set of examples objects into the framework.
     *
     * @param type Specifies the type of example set which is to load.
     * @return some useful information
     * @throws SystemException             Thrown if a framework internal error occurs.
     * @throws InvalidSearchQueryException thrown if a given search query could not be translated into a SQL query
     */
    public String loadExamples(final String type) throws InvalidSearchQueryException, SystemException {
        final StringBuilder result = new StringBuilder();

        // select example package
        if (!"common".equals(type)) {
            throw new SystemException("Example set '" + type + "' not supported.");
        }

        try {
            String selfUrl = EscidocConfiguration.getInstance().get(EscidocConfiguration.ESCIDOC_CORE_SELFURL);

            if (!selfUrl.endsWith("/")) {
                selfUrl += "/";
            }
            result.append(examples.load(selfUrl + "examples/escidoc/"));
        }
        catch (final Exception e) {
            throw new SystemException(e);
        }

        return getUtility().prepareReturnXml(result.toString());
    }

    /**
     * Ingest the Examples object.
     *
     * @param examples Examples object to be ingested
     */
    public void setExamples(final Examples examples) {
        this.examples = examples;
    }

    /**
     * Ingest the FrameworkInfo object.
     *
     * @param frameworkInfo FrameworkInfo object to be ingested
     */
    public void setFrameworkInfo(final FrameworkInfo frameworkInfo) {
        this.frameworkInfo = frameworkInfo;
    }

    /**
     * Ingest the reindexer object.
     *
     * @param reindexer reindexer object to be ingested
     */
    public void setReindexer(final Reindexer reindexer) {
        this.reindexer = reindexer;
    }

    /**
     * Setting the indexingHandler.
     *
     * @param indexingHandler The indexingHandler to set.
     */
    public final void setIndexingHandler(final IndexingHandler indexingHandler) {
        this.indexingHandler = indexingHandler;
    }

    /**
     * Injects the renderer.
     *
     * @param renderer The renderer to inject.
     */
    public void setRenderer(final AdminRendererInterface renderer) {
        this.renderer = renderer;
    }

    /**
     * Sets the {@link PurgeService}.
     *
     * @param purgeService the {@link PurgeService}
     */
    public void setPurgeService(final PurgeService purgeService) {
        this.purgeService = purgeService;
    }

    /**
     * Injects the TripleStore utility.
     *
     * @param tripleStoreUtility TripleStoreUtility from Spring
     */
    public void setTripleStoreUtility(final TripleStoreUtility tripleStoreUtility) {
        this.tripleStoreUtility = tripleStoreUtility;
    }
}
