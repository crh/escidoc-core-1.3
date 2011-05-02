package de.escidoc.core.aa.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

/**
 * Local home interface for UserGroupHandler.
 */
public interface UserGroupHandlerLocalHome extends EJBLocalHome {

    String COMP_NAME = "java:comp/env/ejb/UserGroupHandlerLocal";

    String JNDI_NAME = "ejb/UserGroupHandlerLocal";

    UserGroupHandlerLocal create() throws CreateException;

}
