package de.escidoc.core.tme.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import java.rmi.RemoteException;

/**
 * Home interface for JhoveHandler.
 */
public interface JhoveHandlerRemoteHome extends EJBHome {

    String COMP_NAME = "java:comp/env/ejb/JhoveHandler";

    String JNDI_NAME = "ejb/JhoveHandler";

    JhoveHandlerRemote create() throws CreateException, RemoteException;

}
