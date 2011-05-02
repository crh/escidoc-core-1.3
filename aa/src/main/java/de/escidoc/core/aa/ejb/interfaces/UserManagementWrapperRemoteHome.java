package de.escidoc.core.aa.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import java.rmi.RemoteException;

/**
 * Home interface for UserManagementWrapper.
 */
public interface UserManagementWrapperRemoteHome extends EJBHome {

    String COMP_NAME = "java:comp/env/ejb/UserManagementWrapper";

    String JNDI_NAME = "ejb/UserManagementWrapper";

    UserManagementWrapperRemote create() throws CreateException, RemoteException;

}
