package de.escidoc.core.cmm.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

/**
 * Local home interface for ContentModelHandler.
 */
public interface ContentModelHandlerLocalHome extends EJBLocalHome {

    String COMP_NAME = "java:comp/env/ejb/ContentModelHandlerLocal";

    String JNDI_NAME = "ejb/ContentModelHandlerLocal";

    ContentModelHandlerLocal create() throws CreateException;

}
