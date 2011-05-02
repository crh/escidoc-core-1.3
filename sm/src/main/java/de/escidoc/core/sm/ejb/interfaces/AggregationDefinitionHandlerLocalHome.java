package de.escidoc.core.sm.ejb.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

/**
 * Local home interface for AggregationDefinitionHandler.
 */
public interface AggregationDefinitionHandlerLocalHome extends EJBLocalHome {

    String COMP_NAME = "java:comp/env/ejb/AggregationDefinitionHandlerLocal";

    String JNDI_NAME = "ejb/AggregationDefinitionHandlerLocal";

    AggregationDefinitionHandlerLocal create() throws CreateException;

}
