package de.escidoc.core.oai.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

/**
 * Local home interface for SetDefinitionHandler.
 */
public interface SetDefinitionHandlerLocalHome extends EJBLocalHome {

    String COMP_NAME = "java:comp/env/ejb/SetDefinitionHandlerLocal";

    String JNDI_NAME = "ejb/SetDefinitionHandlerLocal";

    SetDefinitionHandlerLocal create() throws CreateException;

}
