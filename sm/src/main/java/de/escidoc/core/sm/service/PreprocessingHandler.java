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
package de.escidoc.core.sm.service;

import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.sm.service.interfaces.PreprocessingHandlerInterface;

/**
 * A statistic data resource handler.
 *
 * @author Michael Hoppe
 */
public class PreprocessingHandler implements PreprocessingHandlerInterface {

    private de.escidoc.core.sm.business.interfaces.PreprocessingHandlerInterface handler;

    /**
     * Injects the Preprocessing handler.
     *
     * @param preprocessingHandler The Preprocessing handler bean to inject.
     */
    public void setPreprocessingHandler(
        final de.escidoc.core.sm.business.interfaces.PreprocessingHandlerInterface preprocessingHandler) {

        this.handler = preprocessingHandler;
    }

    /**
     * See Interface for functional description.
     *
     * @param aggregationDefinitionId id of the aggregation-definition to preprocess.
     * @param xmlData                 Preprocessing-information as xml in preprocessing-information schema.
     * @throws AuthenticationException      Thrown in case of failed authentication.
     * @throws AuthorizationException       Thrown in case of failed authorization.
     * @throws XmlSchemaValidationException ex
     * @throws XmlCorruptedException        ex
     * @throws MissingMethodParameterException
     *                                      ex
     * @throws SystemException              ex
     * @see de.escidoc.core.sm.service.interfaces.PreprocessingHandlerInterface #create(java.lang.String)
     */
    @Override
    public void preprocess(final String aggregationDefinitionId, final String xmlData) throws AuthenticationException,
        AuthorizationException, XmlSchemaValidationException, XmlCorruptedException, MissingMethodParameterException,
        SystemException {
        handler.preprocess(aggregationDefinitionId, xmlData);
    }

}
