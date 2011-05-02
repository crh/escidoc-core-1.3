package de.escidoc.core.sm.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

/**
 * Local home interface for ReportDefinitionHandler.
 */
public interface ReportDefinitionHandlerLocalHome extends EJBLocalHome {

    String COMP_NAME = "java:comp/env/ejb/ReportDefinitionHandlerLocal";

    String JNDI_NAME = "ejb/ReportDefinitionHandlerLocal";

    ReportDefinitionHandlerLocal create() throws CreateException;

}
