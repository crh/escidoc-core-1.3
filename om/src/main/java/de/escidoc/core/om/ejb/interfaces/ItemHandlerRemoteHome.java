package de.escidoc.core.om.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import java.rmi.RemoteException;

/**
 * Home interface for ItemHandler.
 */
public interface ItemHandlerRemoteHome extends EJBHome {

    String COMP_NAME = "java:comp/env/ejb/ItemHandler";

    String JNDI_NAME = "ejb/ItemHandler";

    ItemHandlerRemote create() throws CreateException, RemoteException;

}
