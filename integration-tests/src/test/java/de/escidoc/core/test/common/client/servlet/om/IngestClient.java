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
package de.escidoc.core.test.common.client.servlet.om;

import de.escidoc.core.om.IngestHandler;
import de.escidoc.core.om.IngestHandlerServiceLocator;
import de.escidoc.core.test.common.client.servlet.ClientBase;
import de.escidoc.core.test.common.client.servlet.Constants;
import de.escidoc.core.test.common.client.servlet.interfaces.ResourceHandlerClientInterface;

import javax.xml.rpc.ServiceException;

/**
 * Offers access methods to the escidoc interfaces of the item resource.
 *
 * @author Michael Schneider
 */
public class IngestClient extends ClientBase implements ResourceHandlerClientInterface {

    private IngestHandler soapClient = null;

    /**
     * @param transport The transport identifier.
     */
    public IngestClient(final int transport) {
        super(transport);
    }

    /**
     * Ingest a resource.
     *
     * @param xmlData the string containing the resource in XML representation.
     * @return The HttpMethod after the service call (REST) or the result object (SOAP).
     * @throws Exception If the service call fails.
     */
    public Object ingest(final String xmlData) throws Exception {
        return callEsciDoc("Ingest.ingest", "ingest", Constants.HTTP_METHOD_PUT, Constants.INGEST_BASE_URI,
            new String[] {}, changeToString(xmlData));

    }

    /**
     * @return Returns the soapClient.
     * @throws ServiceException If service instantiation fails.
     */
    @Override
    public IngestHandler getSoapClient() throws ServiceException {

        if (soapClient == null) {
            IngestHandlerServiceLocator serviceLocator = new IngestHandlerServiceLocator(getEngineConfig());
            String ep = checkSoapAddress(serviceLocator.getIngestHandlerServiceAddress());
            serviceLocator.setIngestHandlerServiceEndpointAddress(ep);
            soapClient = serviceLocator.getIngestHandlerService();
        }
        return soapClient;
    }

    /**
     * @param id The id of the item.
     * @return The HttpMethod after the service call (REST) or the result object (SOAP).
     * @throws Exception If the service call fails.
     */
    public Object retrieveResources(final String id) throws Exception {

        throw new Exception("unsupported");
    }

}
