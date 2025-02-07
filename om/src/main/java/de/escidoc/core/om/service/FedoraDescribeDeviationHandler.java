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

import de.escidoc.core.om.service.interfaces.FedoraDescribeDeviationHandlerInterface;

import java.util.Map;

/**
 * An describe resource handler.
 * <p/>
 * Security note: This handler should not be intercepted for authorization, as it does not provide security relevant
 * information and can be public accessible.
 *
 * @author Michael Hoppe
 */
public class FedoraDescribeDeviationHandler implements FedoraDescribeDeviationHandlerInterface {

    private de.escidoc.core.om.business.interfaces.FedoraDescribeDeviationHandlerInterface handler;

    /**
     * Injects the FedoraDescribeDeviation handler.
     *
     * @param fedoraDescribeDeviationHandler The FedoraDescribeDeviation handler bean to inject.
     */
    public void setFedoraDescribeDeviationHandler(
        final de.escidoc.core.om.business.interfaces.FedoraDescribeDeviationHandlerInterface fedoraDescribeDeviationHandler) {

        this.handler = fedoraDescribeDeviationHandler;
    }

    /**
     * @param parameters request parameters.
     * @return String response
     * @throws Exception ex
     * @see de.escidoc.core.om.service.interfaces .FedoraDescribeDeviationHandlerInterface #getFedoraDescription( Map)
     */
    @Override
    public String getFedoraDescription(final Map<String, String[]> parameters) throws Exception {
        return handler.getFedoraDescription(parameters);
    }

}
