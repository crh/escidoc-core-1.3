/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE
 * or http://www.escidoc.de/license.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at license/ESCIDOC.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
 * fuer wissenschaftlich-technische Information mbH and Max-Planck-
 * Gesellschaft zur Foerderung der Wissenschaft e.V.  
 * All rights reserved.  Use is subject to license terms.
 */
package de.escidoc.core.oai.business.renderer;

import de.escidoc.core.aa.business.renderer.AbstractRenderer;
import de.escidoc.core.common.business.Constants;
import de.escidoc.core.common.business.filter.RecordPacking;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.common.util.xml.XmlUtility;
import de.escidoc.core.common.util.xml.factory.SetDefinitionXmlProvider;
import de.escidoc.core.common.util.xml.factory.XmlTemplateProvider;
import de.escidoc.core.oai.business.persistence.SetDefinition;
import de.escidoc.core.oai.business.renderer.interfaces.SetDefinitionRendererInterface;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Set definition renderer implementation using the velocity template engine.
 *
 * @author Rozita Friedman
 */
public final class VelocityXmlSetDefinitionRenderer extends AbstractRenderer implements SetDefinitionRendererInterface {

    /**
     * See Interface for functional description.
     *
     * @see SetDefinitionRendererInterface#render(SetDefinition)
     */
    @Override
    public String render(final SetDefinition setDefinition) throws SystemException, WebserverSystemException {
        final Map<String, Object> values = new HashMap<String, Object>();
        values.put("isRootSetDefinition", XmlTemplateProvider.TRUE);
        addCommonValues(values);
        addSetDefinitionValues(setDefinition, values);
        return getSetDefinitionXmlProvider().getSetDefinitionXml(values);
    }

    /**
     * Adds the values of the {@link SetDefinition} to the provided {@link Map}.
     *
     * @param setDefinition The {@link SetDefinition}.
     * @param values        The {@link Map} to add the values to.
     * @throws SystemException Thrown in case of an internal error.
     */
    private static void addSetDefinitionValues(final SetDefinition setDefinition, final Map<String, Object> values)
        throws SystemException {
        DateTime lmdDateTime = new DateTime(setDefinition.getLastModificationDate());
        lmdDateTime = lmdDateTime.withZone(DateTimeZone.UTC);
        final String lmd = lmdDateTime.toString(Constants.TIMESTAMP_FORMAT);
        values.put("setDefinitionLastModificationDate", lmd);
        values.put("setDefinitionHref", setDefinition.getHref());
        DateTime creationDateTime = new DateTime(setDefinition.getCreationDate());
        creationDateTime = creationDateTime.withZone(DateTimeZone.UTC);
        final String creationDate = creationDateTime.toString(Constants.TIMESTAMP_FORMAT);
        values.put("setDefinitionCreationDate", creationDate);
        values.put("setDefinitionName", setDefinition.getName());
        values.put("setDefinitionSpecification", setDefinition.getSpecification());
        values.put("setDefinitionQuery", setDefinition.getQuery());
        values.put("setDefinitionDescription", setDefinition.getDescription());
        values.put("setDefinitionId", setDefinition.getId());
        values.put("setDefinitionCreatedByTitle", setDefinition.getCreatorTitle());
        final String createdById = setDefinition.getCreatorId();
        final String cratedByHref = XmlUtility.getUserAccountHref(createdById);
        values.put("setDefinitionCreatedByHref", cratedByHref);
        values.put("setDefinitionCreatedById", createdById);
        values.put("setDefinitionModifiedByTitle", setDefinition.getModifiedByTitle());
        values.put("setDefinitionModifiedByHref", XmlUtility.getUserAccountHref(setDefinition.getModifiedById()));
        values.put("setDefinitionModifiedById", setDefinition.getModifiedById());

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.escidoc.core.oai.business.renderer.interfaces.
     * SetDefinitionRendererInterface#renderSetDefinitions(java.util.List)
     */
    @Override
    public String renderSetDefinitions(final List<SetDefinition> setDefinitions, final RecordPacking recordPacking)
        throws SystemException, WebserverSystemException {
        final Map<String, Object> values = new HashMap<String, Object>();

        values.put("isRootSetDefinition", "false");
        values.put("recordPacking", recordPacking);
        addCommonValues(values);
        addSetDefinitionListValues(values);

        final Collection<Map<String, Object>> setDefinitionsValues =
            new ArrayList<Map<String, Object>>(setDefinitions.size());
        for (final SetDefinition setDefinition : setDefinitions) {
            final Map<String, Object> setDefinitionValues = new HashMap<String, Object>();
            addSetDefinitionValues(setDefinition, setDefinitionValues);
            setDefinitionsValues.add(setDefinitionValues);
        }
        values.put("setDefinitions", setDefinitionsValues);
        return getSetDefinitionXmlProvider().getSetDefinitionsSrwXml(values);
    }

    /**
     * Adds the common values to the provided map.
     *
     * @param values The map to add values to.
     * @throws WebserverSystemException Thrown in case of an internal error.
     */
    private void addCommonValues(final Map<String, Object> values) throws WebserverSystemException {

        addSetDefinitionNamespaceValues(values);
        addPropertiesNamespaceValues(values);
        addStructuralRelationNamespaceValues(values);
        addEscidocBaseUrl(values);
    }

    /**
     * Adds the structural relations name space values to the provided map.
     *
     * @param values The map to add values to.
     */
    @Override
    protected void addStructuralRelationNamespaceValues(final Map<String, Object> values) {

        values.put(XmlTemplateProvider.ESCIDOC_SREL_NS_PREFIX, Constants.STRUCTURAL_RELATIONS_NS_PREFIX);
        values.put(XmlTemplateProvider.ESCIDOC_SREL_NS, Constants.STRUCTURAL_RELATIONS_NS_URI);
    }

    /**
     * Adds the set definition name space values.
     *
     * @param values The {@link Map} to that the values shall be added.
     */
    private static void addSetDefinitionNamespaceValues(final Map<String, Object> values) {
        values.put("setDefinitionNamespacePrefix", Constants.SET_DEFINITION_NS_PREFIX);
        values.put("setDefinitionNamespace", Constants.SET_DEFINITION_NS_URI);
    }

    /**
     * Adds the set definition list values to the provided map.
     *
     * @param values The map to add values to.
     */
    private static void addSetDefinitionListValues(final Map<String, Object> values) {

        addSetDefinitionsNamespaceValues(values);
        values.put("searchResultNamespace", Constants.SEARCH_RESULT_NS_URI);
        values.put("setDefinitionListTitle", "Set Definition List");
    }

    /**
     * Adds the values related to the set definitions name space to the provided {@link Map}.
     *
     * @param values The MAP to add the values to.
     */
    private static void addSetDefinitionsNamespaceValues(final Map<String, Object> values) {

        values.put("setDefinitionListNamespacePrefix", Constants.SET_DEFINITION_LIST_NS_PREFIX);
        values.put("setDefinitionListNamespace", Constants.SET_DEFINITION_LIST_NS_URI);
    }

    /**
     * Adds the escidoc base URL to the provided map.
     *
     * @param values The map to add values to.
     * @throws WebserverSystemException Thrown in case of an internal error.
     */
    private static void addEscidocBaseUrl(final Map<String, Object> values) throws WebserverSystemException {

        values.put(XmlTemplateProvider.VAR_ESCIDOC_BASE_URL, XmlUtility.getEscidocBaseUrl());
    }

    /**
     * Gets the <code>SetDefinitionXmlProvider</code> object.
     *
     * @return Returns the <code>SetDefinitionXmlProvider</code> object.
     * @throws WebserverSystemException Thrown in case of an internal error.
     */
    private static SetDefinitionXmlProvider getSetDefinitionXmlProvider() throws WebserverSystemException {

        return SetDefinitionXmlProvider.getInstance();
    }
}
