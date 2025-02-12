package de.escidoc.core.oai.service;

import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
import de.escidoc.core.common.exceptions.application.violated.UniqueConstraintViolationException;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.oai.service.interfaces.SetDefinitionHandlerInterface;

import java.util.Map;

/**
 * A set definition resource handler.
 *
 * @author Rozita Friedman
 */
public class SetDefinitionHandler implements SetDefinitionHandlerInterface {

    /**
     * The logger.
     */

    private de.escidoc.core.oai.business.interfaces.SetDefinitionHandlerInterface handler;

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.escidoc.core.oai.service.interfaces.SetDefinitionHandlerInterface#
     * create(java.lang.String)
     */
    @Override
    public String create(final String setDefinition) throws UniqueConstraintViolationException, InvalidXmlException,
        MissingMethodParameterException, SystemException, AuthenticationException, AuthorizationException {
        return handler.create(setDefinition);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.escidoc.core.oai.service.interfaces.SetDefinitionHandlerInterface#
     * retrieve(java.lang.String)
     */
    @Override
    public String retrieve(final String setDefinitionId) throws ResourceNotFoundException,
        MissingMethodParameterException, SystemException, AuthenticationException, AuthorizationException {
        return handler.retrieve(setDefinitionId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.escidoc.core.oai.service.interfaces.SetDefinitionHandlerInterface#
     * update(java.lang.String, java.lang.String)
     */
    @Override
    public String update(final String setDefinitionId, final String xmlData) throws ResourceNotFoundException,
        OptimisticLockingException, MissingMethodParameterException, SystemException, AuthenticationException,
        AuthorizationException {
        return handler.update(setDefinitionId, xmlData);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.escidoc.core.oai.service.interfaces.SetDefinitionHandlerInterface#
     * delete(java.lang.String)
     */
    @Override
    public void delete(final String setDefinitionId) throws ResourceNotFoundException, MissingMethodParameterException,
        SystemException, AuthenticationException, AuthorizationException {
        handler.delete(setDefinitionId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.escidoc.core.oai.service.interfaces.SetDefinitionHandlerInterface#
     * retrieveSetDefinitions(java.util.Map)
     */
    @Override
    public String retrieveSetDefinitions(final Map<String, String[]> filter) throws AuthenticationException,
        AuthorizationException, MissingMethodParameterException, InvalidSearchQueryException, SystemException {
        return handler.retrieveSetDefinitions(filter);
    }

    /**
     * Injects the set definition handler.
     *
     * @param handler The set definition handler bean to inject.
     */
    public void setSetDefinitionHandler(
        final de.escidoc.core.oai.business.interfaces.SetDefinitionHandlerInterface handler) {

        this.handler = handler;
    }

}
