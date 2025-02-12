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
package de.escidoc.core.aa.service;

import de.escidoc.core.aa.service.interfaces.EscidocUserDetailsServiceInterface;
import de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface;
import de.escidoc.core.common.exceptions.application.notfound.UserAccountNotFoundException;
import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.common.util.service.UserContext;
import de.escidoc.core.common.util.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UsernameNotFoundException;

/**
 * Implementation of an Acegi UserDetailsService.
 *
 * @author Torsten Tetteroo
 * @see UserDetailsService
 */
public class EscidocUserDetailsService implements EscidocUserDetailsServiceInterface {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EscidocUserDetailsService.class);

    private static final String FAILED_TO_AUTHENTICATE_USER_BY_HANDLE =
        "Failed to authenticate user with provided information";

    private UserAccountHandlerInterface userAccountHandler;

    /**
     * See Interface for functional description.
     *
     * @see org.springframework.security.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
     */
    @Override
    public UserDetails loadUserByUsername(final String identifier) {
        boolean wasExternalBefore = false;
        try {
            // Calls from the authorization component to other components run
            // with privileges of the internal user (superuser).
            // They will not be further intercepted.
            wasExternalBefore = UserContext.runAsInternalUser();

            final UserDetails retrievedUserDetails = getUserAccountHandler().retrieveUserDetails(identifier);

            LOGGER.debug(retrievedUserDetails.toString());

            return retrievedUserDetails;
        }
        catch (final UserAccountNotFoundException e) {
            throw new UsernameNotFoundException(
                StringUtility.format(FAILED_TO_AUTHENTICATE_USER_BY_HANDLE, identifier), e);
        }
        catch (final WebserverSystemException e) {
            throw new ObjectRetrievalFailureException(e.getMessage(), e);
        }
        catch (final Exception e) {
            throw new ObjectRetrievalFailureException(e.getMessage(), new WebserverSystemException(e));
        }
        finally {
            if (wasExternalBefore) {
                try {
                    UserContext.runAsExternalUser();
                }
                catch (final WebserverSystemException e) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Error on changing user context.");
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Error on changing user context.", e);
                    }
                }
            }
        }
    }

    /**
     * Gets the user account handler.
     *
     * @return Returns the {@link UserAccountHandlerInterface}.
     */
    private UserAccountHandlerInterface getUserAccountHandler() {

        return this.userAccountHandler;
    }

    /**
     * Injects the user account handler EJB.
     *
     * @param userAccountHandler The {@link UserAccountHandlerInterface} implementation to be injected.
     */
    public void setUserAccountHandler(final UserAccountHandlerInterface userAccountHandler) {

        this.userAccountHandler = userAccountHandler;
    }

}
