package de.escidoc.core.cmm.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import java.rmi.RemoteException;

/**
 * Home interface for ContentModelHandler.
 */
public interface ContentModelHandlerRemoteHome extends EJBHome {

    String COMP_NAME = "java:comp/env/ejb/ContentModelHandler";

    String JNDI_NAME = "ejb/ContentModelHandler";

    ContentModelHandlerRemote create() throws CreateException, RemoteException;

}
