package de.escidoc.core.oai.ejb;

import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
import de.escidoc.core.common.exceptions.application.violated.UniqueConstraintViolationException;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.common.util.service.UserContext;
import de.escidoc.core.oai.service.interfaces.SetDefinitionHandlerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
import org.springframework.security.context.SecurityContext;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import java.rmi.RemoteException;
import java.util.Map;

public class SetDefinitionHandlerBean implements SessionBean {

    private SetDefinitionHandlerInterface service;

    private SessionContext sessionCtx;

    private static final Logger LOGGER = LoggerFactory.getLogger(SetDefinitionHandlerBean.class);

    public void ejbCreate() throws CreateException {
        try {
            final BeanFactoryLocator beanFactoryLocator = SingletonBeanFactoryLocator.getInstance();
            final BeanFactory factory =
                beanFactoryLocator.useBeanFactory("SetDefinitionHandler.spring.ejb.context").getFactory();
            this.service = (SetDefinitionHandlerInterface) factory.getBean("service.SetDefinitionHandler");
        }
        catch (Exception e) {
            LOGGER.error("ejbCreate(): Exception SetDefinitionHandlerComponent: " + e);
            throw new CreateException(e.getMessage()); // Ignore FindBugs
        }
    }

    @Override
    public final void setSessionContext(final SessionContext arg0) throws RemoteException {
        this.sessionCtx = arg0;
    }

    @Override
    public final void ejbRemove() throws RemoteException {
    }

    @Override
    public final void ejbActivate() throws RemoteException {

    }

    @Override
    public final void ejbPassivate() throws RemoteException {

    }

    public final String create(final String setDefinition, final SecurityContext securityContext)
        throws UniqueConstraintViolationException, InvalidXmlException, MissingMethodParameterException,
        SystemException, AuthenticationException, AuthorizationException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.create(setDefinition);
    }

    public final String create(final String setDefinition, final String authHandle, final Boolean restAccess)
        throws UniqueConstraintViolationException, InvalidXmlException, MissingMethodParameterException,
        SystemException, AuthenticationException, AuthorizationException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.create(setDefinition);
    }

    public final String retrieve(final String setDefinitionId, final SecurityContext securityContext)
        throws ResourceNotFoundException, MissingMethodParameterException, SystemException, AuthenticationException,
        AuthorizationException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.retrieve(setDefinitionId);
    }

    public final String retrieve(final String setDefinitionId, final String authHandle, final Boolean restAccess)
        throws ResourceNotFoundException, MissingMethodParameterException, SystemException, AuthenticationException,
        AuthorizationException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.retrieve(setDefinitionId);
    }

    public final String update(final String setDefinitionId, final String xmlData, final SecurityContext securityContext)
        throws ResourceNotFoundException, OptimisticLockingException, MissingMethodParameterException, SystemException,
        AuthenticationException, AuthorizationException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.update(setDefinitionId, xmlData);
    }

    public final String update(
        final String setDefinitionId, final String xmlData, final String authHandle, final Boolean restAccess)
        throws ResourceNotFoundException, OptimisticLockingException, MissingMethodParameterException, SystemException,
        AuthenticationException, AuthorizationException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.update(setDefinitionId, xmlData);
    }

    public final void delete(final String setDefinitionId, final SecurityContext securityContext)
        throws ResourceNotFoundException, MissingMethodParameterException, SystemException, AuthenticationException,
        AuthorizationException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        service.delete(setDefinitionId);
    }

    public final void delete(final String setDefinitionId, final String authHandle, final Boolean restAccess)
        throws ResourceNotFoundException, MissingMethodParameterException, SystemException, AuthenticationException,
        AuthorizationException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        service.delete(setDefinitionId);
    }

    public final String retrieveSetDefinitions(final Map filter, final SecurityContext securityContext)
        throws AuthenticationException, AuthorizationException, MissingMethodParameterException,
        InvalidSearchQueryException, SystemException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.retrieveSetDefinitions(filter);
    }

    public final String retrieveSetDefinitions(final Map filter, final String authHandle, final Boolean restAccess)
        throws AuthenticationException, AuthorizationException, MissingMethodParameterException,
        InvalidSearchQueryException, SystemException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.retrieveSetDefinitions(filter);
    }
}