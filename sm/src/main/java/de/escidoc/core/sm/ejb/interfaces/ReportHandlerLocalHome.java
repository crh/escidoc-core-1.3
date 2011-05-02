package de.escidoc.core.sm.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

/**
 * Local home interface for ReportHandler.
 */
public interface ReportHandlerLocalHome extends EJBLocalHome {

    String COMP_NAME = "java:comp/env/ejb/ReportHandlerLocal";

    String JNDI_NAME = "ejb/ReportHandlerLocal";

    ReportHandlerLocal create() throws CreateException;

}
