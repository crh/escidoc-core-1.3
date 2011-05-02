package de.escidoc.core.aa.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import java.rmi.RemoteException;

/**
 * Home interface for UserGroupHandler.
 */
public interface UserGroupHandlerRemoteHome extends EJBHome {

    String COMP_NAME = "java:comp/env/ejb/UserGroupHandler";

    String JNDI_NAME = "ejb/UserGroupHandler";

    UserGroupHandlerRemote create() throws CreateException, RemoteException;

}
