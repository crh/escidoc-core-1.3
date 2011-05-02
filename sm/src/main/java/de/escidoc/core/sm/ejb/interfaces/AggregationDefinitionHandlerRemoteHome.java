package de.escidoc.core.sm.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import java.rmi.RemoteException;

/**
 * Home interface for AggregationDefinitionHandler.
 */
public interface AggregationDefinitionHandlerRemoteHome extends EJBHome {

    String COMP_NAME = "java:comp/env/ejb/AggregationDefinitionHandler";

    String JNDI_NAME = "ejb/AggregationDefinitionHandler";

    AggregationDefinitionHandlerRemote create() throws CreateException, RemoteException;

}
