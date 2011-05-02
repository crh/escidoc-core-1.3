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

package de.escidoc.core.om.business.fedora.ingest;

import de.escidoc.core.common.business.Constants;
import de.escidoc.core.common.exceptions.EscidocException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidResourceException;
import de.escidoc.core.om.business.interfaces.IngestFacade;
import de.escidoc.core.om.business.interfaces.ValueFormatter;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin facade on top of the FedoraIngestHandler. This class separates the Handler from the individual classes used for
 * the ingest and the determination process of the resource type.
 *
 * @author Kai Strnad
 */
public class FedoraIngestFacade implements IngestFacade {

    private ResourceMapperDao resourceMapperDao;

    private ValueFormatter formatter;

    /**
     * @param formatter the formatter to set
     */
    public void setFormatter(final ValueFormatter formatter) {
        this.formatter = formatter;
    }

    /**
     * @return the formatter
     */
    public ValueFormatter getFormatter() {
        return this.formatter;
    }

    /**
     * Getter for the ResourceMapperDao
     *
     * @return the resourceMapperDao
     */
    public ResourceMapperDao getResourceMapperDao() {
        return this.resourceMapperDao;
    }

    /**
     * Setter for the ResourceMapperDao
     * @param resourceMapperDao
     */
    public void setResourceMapperDao(final ResourceMapperDao resourceMapperDao) {
        this.resourceMapperDao = resourceMapperDao;
    }

    /**
     * Ingest a given resource and return the id which has been assigned by the framework. Format the return value
     * according to the rule implemented in the injected Formatter instance.
     */
    @Override
    public String ingest(final String xmlData) throws EscidocException, InvalidResourceException {
        final ResourceMapperBean bean = getResourceMapperDao().getIngestableForResource(xmlData);
        final String objectId = bean.getResource().ingest(xmlData);

        final Map<String, String> values = new HashMap<String, String>();
        values.put(Constants.INGEST_OBJ_ID, objectId);
        values.put(Constants.INGEST_RESOURCE_TYPE, bean.getResourceType().toString());

        return getFormatter().format(values);
    }
}
