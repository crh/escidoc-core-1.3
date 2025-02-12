package de.escidoc.core.aa.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

/**
 * Local home interface for PolicyDecisionPoint.
 */
public interface PolicyDecisionPointLocalHome extends EJBLocalHome {

    String COMP_NAME = "java:comp/env/ejb/PolicyDecisionPointLocal";

    String JNDI_NAME = "ejb/PolicyDecisionPointLocal";

    PolicyDecisionPointLocal create() throws CreateException;

}
