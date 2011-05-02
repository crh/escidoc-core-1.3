package de.escidoc.core.aa.ejb;

import de.escidoc.core.aa.service.interfaces.ActionHandlerInterface;
import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
import de.escidoc.core.common.exceptions.application.notfound.ContextNotFoundException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.common.util.service.UserContext;
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

public class ActionHandlerBean implements SessionBean {

    private ActionHandlerInterface service;

    private SessionContext sessionCtx;

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionHandlerBean.class);

    public void ejbCreate() throws CreateException {
        try {
            final BeanFactoryLocator beanFactoryLocator = SingletonBeanFactoryLocator.getInstance();
            final BeanFactory factory =
                beanFactoryLocator.useBeanFactory("ActionHandler.spring.ejb.context").getFactory();
            this.service = (ActionHandlerInterface) factory.getBean("service.ActionHandler");
        }
        catch (Exception e) {
            LOGGER.error("ejbCreate(): Exception ActionHandlerComponent: " + e);
            throw new CreateException(e.getMessage());
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

    public final String createUnsecuredActions(
        final String contextId, final String actions, final SecurityContext securityContext)
        throws ContextNotFoundException, XmlCorruptedException, XmlSchemaValidationException, AuthenticationException,
        AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.createUnsecuredActions(contextId, actions);
    }

    public final String createUnsecuredActions(
        final String contextId, final String actions, final String authHandle, final Boolean restAccess)
        throws ContextNotFoundException, XmlCorruptedException, XmlSchemaValidationException, AuthenticationException,
        AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.createUnsecuredActions(contextId, actions);
    }

    public final void deleteUnsecuredActions(final String contextId, final SecurityContext securityContext)
        throws ContextNotFoundException, AuthenticationException, AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        service.deleteUnsecuredActions(contextId);
    }

    public final void deleteUnsecuredActions(final String contextId, final String authHandle, final Boolean restAccess)
        throws ContextNotFoundException, AuthenticationException, AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        service.deleteUnsecuredActions(contextId);
    }

    public final String retrieveUnsecuredActions(final String contextId, final SecurityContext securityContext)
        throws ContextNotFoundException, AuthenticationException, AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.retrieveUnsecuredActions(contextId);
    }

    public final String retrieveUnsecuredActions(
        final String contextId, final String authHandle, final Boolean restAccess) throws ContextNotFoundException,
        AuthenticationException, AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.retrieveUnsecuredActions(contextId);
    }
}