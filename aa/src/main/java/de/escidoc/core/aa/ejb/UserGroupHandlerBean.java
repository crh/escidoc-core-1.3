package de.escidoc.core.aa.ejb;

import de.escidoc.core.aa.service.interfaces.UserGroupHandlerInterface;
import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidScopeException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.notfound.GrantNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.OrganizationalUnitNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.RoleNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.UserAccountNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.UserGroupNotFoundException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.application.violated.AlreadyActiveException;
import de.escidoc.core.common.exceptions.application.violated.AlreadyDeactiveException;
import de.escidoc.core.common.exceptions.application.violated.AlreadyExistsException;
import de.escidoc.core.common.exceptions.application.violated.AlreadyRevokedException;
import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
import de.escidoc.core.common.exceptions.application.violated.UniqueConstraintViolationException;
import de.escidoc.core.common.exceptions.application.violated.UserGroupHierarchyViolationException;
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
import java.util.Map;

public class UserGroupHandlerBean implements SessionBean {

    private UserGroupHandlerInterface service;

    private SessionContext sessionCtx;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserGroupHandlerBean.class);

    public void ejbCreate() throws CreateException {
        try {
            final BeanFactoryLocator beanFactoryLocator = SingletonBeanFactoryLocator.getInstance();
            final BeanFactory factory =
                beanFactoryLocator.useBeanFactory("UserGroupHandler.spring.ejb.context").getFactory();
            this.service = (UserGroupHandlerInterface) factory.getBean("service.UserGroupHandler");
        }
        catch (Exception e) {
            LOGGER.error("ejbCreate(): Exception UserGroupHandlerComponent: " + e);
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

    public final String create(final String xmlData, final SecurityContext securityContext)
        throws UniqueConstraintViolationException, XmlCorruptedException, XmlSchemaValidationException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.create(xmlData);
    }

    public final String create(final String xmlData, final String authHandle, final Boolean restAccess)
        throws UniqueConstraintViolationException, XmlCorruptedException, XmlSchemaValidationException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.create(xmlData);
    }

    public final void delete(final String groupId, final SecurityContext securityContext)
        throws UserGroupNotFoundException, MissingMethodParameterException, AuthenticationException,
        AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        service.delete(groupId);
    }

    public final void delete(final String groupId, final String authHandle, final Boolean restAccess)
        throws UserGroupNotFoundException, MissingMethodParameterException, AuthenticationException,
        AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        service.delete(groupId);
    }

    public final String retrieve(final String groupId, final SecurityContext securityContext)
        throws UserGroupNotFoundException, MissingMethodParameterException, AuthenticationException,
        AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.retrieve(groupId);
    }

    public final String retrieve(final String groupId, final String authHandle, final Boolean restAccess)
        throws UserGroupNotFoundException, MissingMethodParameterException, AuthenticationException,
        AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.retrieve(groupId);
    }

    public final String update(final String groupId, final String xmlData, final SecurityContext securityContext)
        throws UserGroupNotFoundException, UniqueConstraintViolationException, XmlCorruptedException,
        XmlSchemaValidationException, MissingMethodParameterException, MissingAttributeValueException,
        OptimisticLockingException, AuthenticationException, AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.update(groupId, xmlData);
    }

    public final String update(
        final String groupId, final String xmlData, final String authHandle, final Boolean restAccess)
        throws UserGroupNotFoundException, UniqueConstraintViolationException, XmlCorruptedException,
        XmlSchemaValidationException, MissingMethodParameterException, MissingAttributeValueException,
        OptimisticLockingException, AuthenticationException, AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.update(groupId, xmlData);
    }

    public final void activate(final String groupId, final String taskParam, final SecurityContext securityContext)
        throws AlreadyActiveException, UserGroupNotFoundException, XmlCorruptedException,
        MissingMethodParameterException, MissingAttributeValueException, OptimisticLockingException,
        AuthenticationException, AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        service.activate(groupId, taskParam);
    }

    public final void activate(
        final String groupId, final String taskParam, final String authHandle, final Boolean restAccess)
        throws AlreadyActiveException, UserGroupNotFoundException, XmlCorruptedException,
        MissingMethodParameterException, MissingAttributeValueException, OptimisticLockingException,
        AuthenticationException, AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        service.activate(groupId, taskParam);
    }

    public final void deactivate(final String groupId, final String taskParam, final SecurityContext securityContext)
        throws AlreadyDeactiveException, UserGroupNotFoundException, XmlCorruptedException,
        MissingMethodParameterException, MissingAttributeValueException, OptimisticLockingException,
        AuthenticationException, AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        service.deactivate(groupId, taskParam);
    }

    public final void deactivate(
        final String groupId, final String taskParam, final String authHandle, final Boolean restAccess)
        throws AlreadyDeactiveException, UserGroupNotFoundException, XmlCorruptedException,
        MissingMethodParameterException, MissingAttributeValueException, OptimisticLockingException,
        AuthenticationException, AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        service.deactivate(groupId, taskParam);
    }

    public final String retrieveCurrentGrants(final String userGroupId, final SecurityContext securityContext)
        throws UserGroupNotFoundException, MissingMethodParameterException, AuthenticationException,
        AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.retrieveCurrentGrants(userGroupId);
    }

    public final String retrieveCurrentGrants(
        final String userGroupId, final String authHandle, final Boolean restAccess) throws UserGroupNotFoundException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.retrieveCurrentGrants(userGroupId);
    }

    public final String createGrant(final String groupId, final String grantXML, final SecurityContext securityContext)
        throws AlreadyExistsException, UserGroupNotFoundException, InvalidScopeException, RoleNotFoundException,
        XmlCorruptedException, XmlSchemaValidationException, MissingMethodParameterException, AuthenticationException,
        AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.createGrant(groupId, grantXML);
    }

    public final String createGrant(
        final String groupId, final String grantXML, final String authHandle, final Boolean restAccess)
        throws AlreadyExistsException, UserGroupNotFoundException, InvalidScopeException, RoleNotFoundException,
        XmlCorruptedException, XmlSchemaValidationException, MissingMethodParameterException, AuthenticationException,
        AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.createGrant(groupId, grantXML);
    }

    public final void revokeGrant(
        final String groupId, final String grantId, final String taskParam, final SecurityContext securityContext)
        throws UserGroupNotFoundException, GrantNotFoundException, AlreadyRevokedException, XmlCorruptedException,
        MissingAttributeValueException, MissingMethodParameterException, AuthenticationException,
        AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        service.revokeGrant(groupId, grantId, taskParam);
    }

    public final void revokeGrant(
        final String groupId, final String grantId, final String taskParam, final String authHandle,
        final Boolean restAccess) throws UserGroupNotFoundException, GrantNotFoundException, AlreadyRevokedException,
        XmlCorruptedException, MissingAttributeValueException, MissingMethodParameterException,
        AuthenticationException, AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        service.revokeGrant(groupId, grantId, taskParam);
    }

    public final String retrieveGrant(final String groupId, final String grantId, final SecurityContext securityContext)
        throws UserGroupNotFoundException, GrantNotFoundException, MissingMethodParameterException,
        AuthenticationException, AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.retrieveGrant(groupId, grantId);
    }

    public final String retrieveGrant(
        final String groupId, final String grantId, final String authHandle, final Boolean restAccess)
        throws UserGroupNotFoundException, GrantNotFoundException, MissingMethodParameterException,
        AuthenticationException, AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.retrieveGrant(groupId, grantId);
    }

    public final void revokeGrants(final String groupId, final String taskParam, final SecurityContext securityContext)
        throws UserGroupNotFoundException, GrantNotFoundException, AlreadyRevokedException, XmlCorruptedException,
        MissingAttributeValueException, MissingMethodParameterException, AuthenticationException,
        AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        service.revokeGrants(groupId, taskParam);
    }

    public final void revokeGrants(
        final String groupId, final String taskParam, final String authHandle, final Boolean restAccess)
        throws UserGroupNotFoundException, GrantNotFoundException, AlreadyRevokedException, XmlCorruptedException,
        MissingAttributeValueException, MissingMethodParameterException, AuthenticationException,
        AuthorizationException, SystemException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        service.revokeGrants(groupId, taskParam);
    }

    public final String retrieveResources(final String groupId, final SecurityContext securityContext)
        throws UserGroupNotFoundException, SystemException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.retrieveResources(groupId);
    }

    public final String retrieveResources(final String groupId, final String authHandle, final Boolean restAccess)
        throws UserGroupNotFoundException, SystemException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.retrieveResources(groupId);
    }

    public final String retrieveUserGroups(final Map filter, final SecurityContext securityContext)
        throws MissingMethodParameterException, AuthenticationException, AuthorizationException,
        InvalidSearchQueryException, SystemException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.retrieveUserGroups(filter);
    }

    public final String retrieveUserGroups(final Map filter, final String authHandle, final Boolean restAccess)
        throws MissingMethodParameterException, AuthenticationException, AuthorizationException,
        InvalidSearchQueryException, SystemException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.retrieveUserGroups(filter);
    }

    public final String addSelectors(final String groupId, final String taskParam, final SecurityContext securityContext)
        throws OrganizationalUnitNotFoundException, UserAccountNotFoundException, UserGroupNotFoundException,
        InvalidContentException, MissingMethodParameterException, SystemException, AuthenticationException,
        AuthorizationException, OptimisticLockingException, XmlCorruptedException, XmlSchemaValidationException,
        UserGroupHierarchyViolationException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.addSelectors(groupId, taskParam);
    }

    public final String addSelectors(
        final String groupId, final String taskParam, final String authHandle, final Boolean restAccess)
        throws OrganizationalUnitNotFoundException, UserAccountNotFoundException, UserGroupNotFoundException,
        InvalidContentException, MissingMethodParameterException, SystemException, AuthenticationException,
        AuthorizationException, OptimisticLockingException, XmlCorruptedException, XmlSchemaValidationException,
        UserGroupHierarchyViolationException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.addSelectors(groupId, taskParam);
    }

    public final String removeSelectors(
        final String groupId, final String taskParam, final SecurityContext securityContext)
        throws XmlCorruptedException, XmlSchemaValidationException, AuthenticationException, AuthorizationException,
        SystemException, UserGroupNotFoundException, OptimisticLockingException, MissingMethodParameterException,
        UserAccountNotFoundException, OrganizationalUnitNotFoundException {
        try {
            UserContext.setUserContext(securityContext);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.removeSelectors(groupId, taskParam);
    }

    public final String removeSelectors(
        final String groupId, final String taskParam, final String authHandle, final Boolean restAccess)
        throws XmlCorruptedException, XmlSchemaValidationException, AuthenticationException, AuthorizationException,
        SystemException, UserGroupNotFoundException, OptimisticLockingException, MissingMethodParameterException,
        UserAccountNotFoundException, OrganizationalUnitNotFoundException {
        try {
            UserContext.setUserContext(authHandle);
            UserContext.setRestAccess(restAccess);
        }
        catch (Exception e) {
            throw new SystemException("Initialization of security context failed.", e);
        }
        return service.removeSelectors(groupId, taskParam);
    }
}