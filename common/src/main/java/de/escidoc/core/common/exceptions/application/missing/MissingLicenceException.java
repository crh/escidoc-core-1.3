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

package de.escidoc.core.common.exceptions.application.missing;

/**
 * The MissingLicenceException is used to indicate that one of the objects that must have a licence does not have one.
 * returned httpStatusCode is 417. Status code (417) indicating that the server could not meet the expectation given in
 * the Expect request header.
 *
 * @author Michael Hoppe (FIZ Karlsruhe)
 */
public class MissingLicenceException extends MissingParameterException {

    /**
     * The serial version uid.
     */
    private static final long serialVersionUID = -7642290196365829286L;

    public static final int HTTP_STATUS_CODE = ESCIDOC_HTTP_SC_MISSING;

    public static final String HTTP_STATUS_MESSAGE = "License is missing";

    /**
     * Default constructor.
     */
    public MissingLicenceException() {
        super(HTTP_STATUS_CODE, HTTP_STATUS_MESSAGE);
    }

    /**
     * Constructor used to map an initial exception.
     *
     * @param error Throwable
     */
    public MissingLicenceException(final Throwable error) {
        super(error, HTTP_STATUS_CODE, HTTP_STATUS_MESSAGE);
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message - the detail message.
     */
    public MissingLicenceException(final String message) {
        super(message, HTTP_STATUS_CODE, HTTP_STATUS_MESSAGE);
    }

    /**
     * Constructor used to create a new Exception with the specified detail message and a mapping to an initial
     * exception.
     *
     * @param message - the detail message.
     * @param error   Throwable
     */
    public MissingLicenceException(final String message, final Throwable error) {
        super(message, error, HTTP_STATUS_CODE, HTTP_STATUS_MESSAGE);
    }
}
