package de.escidoc.core.om.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import java.rmi.RemoteException;

/**
 * Home interface for ContentRelationHandler.
 */
public interface ContentRelationHandlerRemoteHome extends EJBHome {

    String COMP_NAME = "java:comp/env/ejb/ContentRelationHandler";

    String JNDI_NAME = "ejb/ContentRelationHandler";

    ContentRelationHandlerRemote create() throws CreateException, RemoteException;

}
