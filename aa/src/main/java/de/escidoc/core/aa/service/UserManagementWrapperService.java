package de.escidoc.core.aa.service;

import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.system.SystemException;
import org.springframework.security.context.SecurityContext;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Service endpoint interface for UserManagementWrapper.
 */
public interface UserManagementWrapperService extends Remote {

    void logout(SecurityContext securityContext) throws AuthenticationException, SystemException, RemoteException;

    void logout(String authHandle, Boolean restAccess) throws AuthenticationException, SystemException, RemoteException;

    void initHandleExpiryTimestamp(String handle, SecurityContext securityContext) throws AuthenticationException,
        SystemException, RemoteException;

    void initHandleExpiryTimestamp(String handle, String authHandle, Boolean restAccess)
        throws AuthenticationException, SystemException, RemoteException;

}
