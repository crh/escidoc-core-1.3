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
package de.escidoc.core.om.service;

import de.escidoc.core.common.exceptions.EscidocException;
import de.escidoc.core.om.service.interfaces.IngestHandlerInterface;

/**
 * The resource ingest handler.
 *
 * @author Steffen Wagner
 */
public class IngestHandler implements IngestHandlerInterface {

    private de.escidoc.core.om.business.interfaces.IngestHandlerInterface handler;

    /**
     * Injects the ingest handler.
     *
     * @param ingestHandler The ingest handler bean to inject.
     */
    public void setIngestHandler(final de.escidoc.core.om.business.interfaces.IngestHandlerInterface ingestHandler) {

        this.handler = ingestHandler;
    }

    /**
     * Ingest method.
     *
     * @param xmlData XML representation of resource which is to create via ingest.
     * @return objid of created resource.
     * @throws EscidocException Thrown if XML representation fulfills not all requirements or internal errors occur.
     */
    @Override
    public String ingest(final String xmlData) throws EscidocException {

        return handler.ingest(xmlData);
    }

}
