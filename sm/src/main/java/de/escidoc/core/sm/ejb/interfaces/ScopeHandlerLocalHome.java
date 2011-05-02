package de.escidoc.core.sm.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

/**
 * Local home interface for ScopeHandler.
 */
public interface ScopeHandlerLocalHome extends EJBLocalHome {

    String COMP_NAME = "java:comp/env/ejb/ScopeHandlerLocal";

    String JNDI_NAME = "ejb/ScopeHandlerLocal";

    ScopeHandlerLocal create() throws CreateException;

}
