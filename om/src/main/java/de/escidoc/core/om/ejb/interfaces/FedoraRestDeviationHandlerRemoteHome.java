package de.escidoc.core.om.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import java.rmi.RemoteException;

/**
 * Home interface for FedoraRestDeviationHandler.
 */
public interface FedoraRestDeviationHandlerRemoteHome extends EJBHome {

    String COMP_NAME = "java:comp/env/ejb/FedoraRestDeviationHandler";

    String JNDI_NAME = "ejb/FedoraRestDeviationHandler";

    FedoraRestDeviationHandlerRemote create() throws CreateException, RemoteException;

}
