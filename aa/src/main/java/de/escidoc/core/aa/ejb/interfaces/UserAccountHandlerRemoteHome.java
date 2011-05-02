package de.escidoc.core.aa.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import java.rmi.RemoteException;

/**
 * Home interface for UserAccountHandler.
 */
public interface UserAccountHandlerRemoteHome extends EJBHome {

    String COMP_NAME = "java:comp/env/ejb/UserAccountHandler";

    String JNDI_NAME = "ejb/UserAccountHandler";

    UserAccountHandlerRemote create() throws CreateException, RemoteException;

}
