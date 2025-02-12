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

package de.escidoc.core.common.exceptions.application.security;

import de.escidoc.core.common.exceptions.system.WebserverSystemException;

/**
 * The AuthorizationException is used to indicate that the action is not allowed because the authorization fails, e.g.
 * due to missing privileges of the authenticated user. Status code (302) indicating that a redirect to eSciDoc login
 * could be needed.
 *
 * @author Torsten Tetteroo
 */
public class AuthorizationException extends SecurityException {

    /**
     * The serial version uid.
     */
    private static final long serialVersionUID = -1134432729970703482L;

    public static final int HTTP_STATUS_CODE = ESCIDOC_HTTP_SC_SECURITY;

    public static final String HTTP_STATUS_MESSAGE = "Authorization failed. ";

    /**
     * Default constructor.
     *
     * @throws WebserverSystemException Thrown in case of an internal error.
     */
    public AuthorizationException() throws WebserverSystemException {
        super(HTTP_STATUS_CODE, HTTP_STATUS_MESSAGE);
    }

    /**
     * Constructor used to map an initial exception.
     *
     * @param error Throwable
     * @throws WebserverSystemException Thrown in case of an internal error.
     */
    public AuthorizationException(final Throwable error) throws WebserverSystemException {
        super(error, HTTP_STATUS_CODE, HTTP_STATUS_MESSAGE);
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message - the detail message.
     * @throws WebserverSystemException Thrown in case of an internal error.
     */
    public AuthorizationException(final String message) throws WebserverSystemException {
        super(message, HTTP_STATUS_CODE, HTTP_STATUS_MESSAGE);
    }

    /**
     * Constructor used to create a new Exception with the specified detail message and a mapping to an initial
     * exception.
     *
     * @param message - the detail message.
     * @param error   Throwable
     * @throws WebserverSystemException Thrown in case of an internal error.
     */
    public AuthorizationException(final String message, final Throwable error) throws WebserverSystemException {
        super(message, error, HTTP_STATUS_CODE, HTTP_STATUS_MESSAGE);
    }
}
