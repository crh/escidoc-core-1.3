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
package de.escidoc.core.om.business.fedora.deviation;

import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.common.util.configuration.EscidocConfiguration;
import de.escidoc.core.common.util.service.ConnectionUtility;
import de.escidoc.core.om.business.interfaces.FedoraDescribeDeviationHandlerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Michael Hoppe
 */
public class FedoraDescribeDeviationHandler implements FedoraDescribeDeviationHandlerInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(FedoraDescribeDeviationHandler.class);

    private ConnectionUtility connectionUtility;

    /**
     * @param parameters http request parameters.
     * @return String response
     * @see de.escidoc.core.om.business.interfaces .FedoraDescribeDeviationHandlerInterface#getDatastreamDissemination(Map)
     */
    @Override
    public String getFedoraDescription(final Map<String, String[]> parameters) throws IOException,
        MalformedURLException, WebserverSystemException {

        final String urlParams = buildUrlParameters(parameters);
        String baseURL = EscidocConfiguration.getInstance().get(EscidocConfiguration.FEDORA_URL);
        final String user = EscidocConfiguration.getInstance().get(EscidocConfiguration.FEDORA_USER);
        final String pass = EscidocConfiguration.getInstance().get(EscidocConfiguration.FEDORA_PASSWORD);
        if (!baseURL.endsWith("/")) {
            baseURL += "/";
        }

        String describeUrl = null;
        try {
            describeUrl = baseURL + "describe" + urlParams;
            return connectionUtility.getRequestURLAsString(new URL(describeUrl), user, pass);
        }
        finally {
            if (describeUrl != null) {
                try {
                    connectionUtility.resetAuthentication(new URL(describeUrl));
                }
                catch (final Exception e) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Error on reseting authentication.");
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Error on reseting authentication.", e);
                    }
                }
            }
        }
    }

    /**
     * make http-requestparameter string out of given Map.
     *
     * @param parameters http request parameters.
     * @return String http requestparameters as String
     */
    private static String buildUrlParameters(final Map<String, String[]> parameters) {
        final StringBuilder urlParams = new StringBuilder("");
        if (parameters != null && !parameters.isEmpty()) {
            urlParams.append('?');
            for (final Entry<String, String[]> e : parameters.entrySet()) {
                if (urlParams.length() > 1) {
                    urlParams.append('&');
                }
                final String[] values = e.getValue();
                if (values != null && values.length > 0) {
                    urlParams.append(e.getKey()).append('=').append(values[0]);
                }
            }
        }
        return urlParams.toString();

    }

    /**
     * See Interface for functional description.
     *
     * @param connectionUtility The HTTP connection utility.
     */
    public void setConnectionUtility(final ConnectionUtility connectionUtility) {

        this.connectionUtility = connectionUtility;
    }

}
