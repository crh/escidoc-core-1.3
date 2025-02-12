package de.escidoc.core.om.ejb.interfaces;

import de.escidoc.core.common.exceptions.application.invalid.InvalidTripleStoreOutputFormatException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidTripleStoreQueryException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
import de.escidoc.core.common.exceptions.application.missing.MissingElementValueException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.system.SystemException;
import org.springframework.security.context.SecurityContext;

import javax.ejb.EJBLocalObject;

/**
 * Local interface for SemanticStoreHandler.
 */
public interface SemanticStoreHandlerLocal extends EJBLocalObject {

    String spo(String taskParam, SecurityContext securityContext) throws SystemException,
        InvalidTripleStoreQueryException, InvalidTripleStoreOutputFormatException, InvalidXmlException,
        MissingElementValueException, AuthenticationException, AuthorizationException;

    String spo(String taskParam, String authHandle, Boolean restAccess) throws SystemException,
        InvalidTripleStoreQueryException, InvalidTripleStoreOutputFormatException, InvalidXmlException,
        MissingElementValueException, AuthenticationException, AuthorizationException;

}
