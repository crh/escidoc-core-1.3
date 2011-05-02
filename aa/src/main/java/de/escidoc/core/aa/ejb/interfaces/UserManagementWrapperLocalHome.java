package de.escidoc.core.aa.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

/**
 * Local home interface for UserManagementWrapper.
 */
public interface UserManagementWrapperLocalHome extends EJBLocalHome {

    String COMP_NAME = "java:comp/env/ejb/UserManagementWrapperLocal";

    String JNDI_NAME = "ejb/UserManagementWrapperLocal";

    UserManagementWrapperLocal create() throws CreateException;

}
