package de.escidoc.core.om.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import java.rmi.RemoteException;

/**
 * Home interface for FedoraDescribeDeviationHandler.
 */
public interface FedoraDescribeDeviationHandlerRemoteHome extends EJBHome {

    String COMP_NAME = "java:comp/env/ejb/FedoraDescribeDeviationHandler";

    String JNDI_NAME = "ejb/FedoraDescribeDeviationHandler";

    FedoraDescribeDeviationHandlerRemote create() throws CreateException, RemoteException;

}
