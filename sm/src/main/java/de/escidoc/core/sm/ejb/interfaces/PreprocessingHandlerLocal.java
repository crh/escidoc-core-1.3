package de.escidoc.core.sm.ejb.interfaces;

import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.system.SystemException;
import org.springframework.security.context.SecurityContext;

import javax.ejb.EJBLocalObject;

/**
 * Local interface for PreprocessingHandler.
 */
public interface PreprocessingHandlerLocal extends EJBLocalObject {

    void preprocess(String aggregationDefinitionId, String xmlData, SecurityContext securityContext)
        throws AuthenticationException, AuthorizationException, XmlSchemaValidationException, XmlCorruptedException,
        MissingMethodParameterException, SystemException;

    void preprocess(String aggregationDefinitionId, String xmlData, String authHandle, Boolean restAccess)
        throws AuthenticationException, AuthorizationException, XmlSchemaValidationException, XmlCorruptedException,
        MissingMethodParameterException, SystemException;

}
