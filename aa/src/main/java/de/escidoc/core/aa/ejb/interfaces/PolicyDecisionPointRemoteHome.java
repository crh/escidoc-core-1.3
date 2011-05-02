package de.escidoc.core.aa.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import java.rmi.RemoteException;

/**
 * Home interface for PolicyDecisionPoint.
 */
public interface PolicyDecisionPointRemoteHome extends EJBHome {

    String COMP_NAME = "java:comp/env/ejb/PolicyDecisionPoint";

    String JNDI_NAME = "ejb/PolicyDecisionPoint";

    PolicyDecisionPointRemote create() throws CreateException, RemoteException;

}
