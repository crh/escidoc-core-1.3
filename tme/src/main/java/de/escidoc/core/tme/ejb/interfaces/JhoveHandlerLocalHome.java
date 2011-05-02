package de.escidoc.core.tme.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

/**
 * Local home interface for JhoveHandler.
 */
public interface JhoveHandlerLocalHome extends EJBLocalHome {

    String COMP_NAME = "java:comp/env/ejb/JhoveHandlerLocal";

    String JNDI_NAME = "ejb/JhoveHandlerLocal";

    JhoveHandlerLocal create() throws CreateException;

}
