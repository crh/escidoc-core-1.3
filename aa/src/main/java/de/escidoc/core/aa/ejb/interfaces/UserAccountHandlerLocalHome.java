package de.escidoc.core.aa.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

/**
 * Local home interface for UserAccountHandler.
 */
public interface UserAccountHandlerLocalHome extends EJBLocalHome {

    String COMP_NAME = "java:comp/env/ejb/UserAccountHandlerLocal";

    String JNDI_NAME = "ejb/UserAccountHandlerLocal";

    UserAccountHandlerLocal create() throws CreateException;

}
