package de.escidoc.core.sm.ejb.interfaces;

import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.notfound.ScopeNotFoundException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.system.SystemException;
import org.springframework.security.context.SecurityContext;

import javax.ejb.EJBLocalObject;
import java.util.Map;

/**
 * Local interface for ScopeHandler.
 */
public interface ScopeHandlerLocal extends EJBLocalObject {

    String create(String xmlData, SecurityContext securityContext) throws AuthenticationException,
        AuthorizationException, XmlSchemaValidationException, XmlCorruptedException, MissingMethodParameterException,
        SystemException;

    String create(String xmlData, String authHandle, Boolean restAccess) throws AuthenticationException,
        AuthorizationException, XmlSchemaValidationException, XmlCorruptedException, MissingMethodParameterException,
        SystemException;

    void delete(String id, SecurityContext securityContext) throws AuthenticationException, AuthorizationException,
        ScopeNotFoundException, MissingMethodParameterException, SystemException;

    void delete(String id, String authHandle, Boolean restAccess) throws AuthenticationException,
        AuthorizationException, ScopeNotFoundException, MissingMethodParameterException, SystemException;

    String retrieve(String id, SecurityContext securityContext) throws AuthenticationException, AuthorizationException,
        ScopeNotFoundException, MissingMethodParameterException, SystemException;

    String retrieve(String id, String authHandle, Boolean restAccess) throws AuthenticationException,
        AuthorizationException, ScopeNotFoundException, MissingMethodParameterException, SystemException;

    String retrieveScopes(Map parameters, SecurityContext securityContext) throws InvalidSearchQueryException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException;

    String retrieveScopes(Map parameters, String authHandle, Boolean restAccess) throws InvalidSearchQueryException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException;

    String update(String id, String xmlData, SecurityContext securityContext) throws AuthenticationException,
        AuthorizationException, ScopeNotFoundException, MissingMethodParameterException, XmlSchemaValidationException,
        XmlCorruptedException, SystemException;

    String update(String id, String xmlData, String authHandle, Boolean restAccess) throws AuthenticationException,
        AuthorizationException, ScopeNotFoundException, MissingMethodParameterException, XmlSchemaValidationException,
        XmlCorruptedException, SystemException;

}
