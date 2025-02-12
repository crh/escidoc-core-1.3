/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
 * only (the "License"). You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
 * the specific language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
 * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
 * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
 * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
 * terms.
 */

package de.escidoc.core.common.util.security.helper;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.StringAttribute;
import de.escidoc.core.common.business.aa.authorisation.AttributeIds;
import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
import de.escidoc.core.common.exceptions.application.missing.MissingElementValueException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.common.util.security.persistence.InvocationMapping;
import de.escidoc.core.common.util.security.persistence.MethodMapping;
import de.escidoc.core.common.util.service.UserContext;
import de.escidoc.core.common.util.string.StringUtility;
import de.escidoc.core.common.util.xml.XmlUtility;
import org.apache.commons.collections.map.LRUMap;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Roland Werner (Accenture)
 */
public class InvocationParser {

    /**
     * Pattern used to detect the place holder for indices in the defined xpath.
     */
    private static final Pattern PATTERN_INDEXED = Pattern.compile(InvocationMapping.INDEXED_PATTERN);

    /**
     * Pattern used to detect sub-resource-attributes in invocation-mappings.
     */
    private static final Pattern PATTERN_SUBRESOURCE = Pattern.compile(InvocationMapping.SUBRESOURCE_PATTERN);

    private static final Matcher matcherSubresource = PATTERN_SUBRESOURCE.matcher("");

    private static final int CACHE_SIZE = 20;

    private final DocumentCache documentCache = new DocumentCache(CACHE_SIZE);

    /**
     * Cache for xml <code>Document</code> objects.<br> This cache is used to avoid multiple parsing of the same
     * document. It provides an method to retrieve a document with creation of not found documents. It uses a
     * <code>LRUMap</code> that is synchronized (via {@link Collections}.synchronizedMap({@link Map})).
     *
     * @author Torsten Tetteroo
     */
    private static final class DocumentCache {

        private final Map<Object, Document> map;

        /**
         * Creates cache of specified size.
         *
         * @param size The number of elements in the cache.
         */
        @SuppressWarnings("unchecked")
        private DocumentCache(final int size) {

            this.map = Collections.synchronizedMap(new LRUMap(size));
        }

        /**
         * Retrieves the document for the provided document data.<br> If it does not exist in the cache, it will be
         * created from the document data and saved.
         *
         * @param documentData The object to get the xml document for, or <code>null</code> in case of an error.
         * @return Returns the xml <code>Document</code> object representing the provided xml data.
         * @throws IOException                  Thrown in case of an i/o error.
         * @throws ParserConfigurationException Thrown in case of an error in parser configuration
         * @throws SAXException                 Thrown in case of a parse error
         * @throws java.io.UnsupportedEncodingException
         */
        public Document retrieveDocument(final Object documentData) throws IOException, ParserConfigurationException,
            SAXException, UnsupportedEncodingException {

            Document document = map.get(documentData);
            if (document == null) {
                final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                document =
                    builder.parse(new ByteArrayInputStream(((String) documentData)
                        .getBytes(XmlUtility.CHARACTER_ENCODING)));
                map.put(documentData, document);
            }

            return document;
        }
    }

    /**
     * Builds a {@link List} of {@link Map} objects that are holding the attributes for a authorization request to the
     * PDP from the provided methodName and arguments, and from the stored user context.<br> This method delegates to
     * <code>buildRequestsList(Object MethodMapping, boolean)</code>
     *
     * @param arguments     The arguments
     * @param methodMapping The method mappings to use
     * @return The generated {@link List} of {@link Map} objects.
     * @throws MissingElementValueException   Thrown if an argument does not contain a mandatory element.
     * @throws MissingAttributeValueException Thrown if an argument does not contain a mandatory attribute .
     * @throws MissingMethodParameterException
     *                                        Thrown if an argument has not been provided but is needed for
     *                                        authorization.
     * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
     * @throws de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException
     */
    public List<Map<String, String>> buildRequestsList(final Object[] arguments, final MethodMapping methodMapping)
        throws MissingMethodParameterException, MissingAttributeValueException, MissingElementValueException,
        WebserverSystemException, XmlCorruptedException {

        return buildRequestsList(arguments, methodMapping, true);
    }

    /**
     * Builds a {@link List} of {@link Map} objects that are holding the attributes for a authorization request to the
     * PDP from the provided methodName and arguments, and from the stored user context.<br> This method delegates to
     * <code>buildRequestsList(Object MethodMapping, boolean)</code>
     *
     * @param argument      The arguments
     * @param methodMapping The method mappings to use
     * @return The generated {@link List} of {@link Map} objects.
     * @throws MissingElementValueException   Thrown if an argument does not contain a mandatory element.
     * @throws MissingAttributeValueException Thrown if an argument does not contain a mandatory attribute .
     * @throws MissingMethodParameterException
     *                                        Thrown if an argument has not been provided but is needed for
     *                                        authorization.
     * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
     * @throws de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException
     */
    public List<Map<String, String>> buildRequestsList(final Object argument, final MethodMapping methodMapping)
        throws MissingMethodParameterException, MissingAttributeValueException, MissingElementValueException,
        WebserverSystemException, XmlCorruptedException {

        return buildRequestsList(argument, methodMapping, false);
    }

    /**
     * Builds a {@link List} of {@link Map} objects that are holding the attributes for a authorization request to the
     * PDP from the provided methodName and arguments, and from the stored user context.
     *
     * @param arguments     The arguments
     * @param methodMapping The method mappings to use
     * @param isArray       Flag that indicates that the given arguments parameter is an array (<code>true</code>) or
     *                      not (<code>false</code>).
     * @return The generated {@link List} of {@link Map} objects.
     * @throws MissingElementValueException   Thrown if an argument does not contain a mandatory element.
     * @throws MissingAttributeValueException Thrown if an argument does not contain a mandatory attribute .
     * @throws MissingMethodParameterException
     *                                        Thrown if an argument has not been provided but is needed for
     *                                        authorization.
     * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
     * @throws de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException
     */
    private List<Map<String, String>> buildRequestsList(
        final Object arguments, final MethodMapping methodMapping, final boolean isArray)
        throws MissingMethodParameterException, MissingAttributeValueException, MissingElementValueException,
        WebserverSystemException, XmlCorruptedException {

        final Set<InvocationMapping> invocationMappings = methodMapping.getInvocationMappings();

        final List<Map<String, String>> requests = new ArrayList<Map<String, String>>();

        // for each resource, loop will be left in case of IndexOutOfBounds
        // exception
        int index = 0;
        do {
            final Map<String, String> request = new HashMap<String, String>();
            try {
                // setup subject
                String id = UserContext.getId();
                if (id == null) {
                    id = UserContext.ANONYMOUS_IDENTIFIER;
                }
                request.put(AttributeIds.URN_SUBJECT_ID, id);

                // setup resource
                request.putAll(setupResourceAttributes(arguments, invocationMappings, isArray, index));

                // setup action
                request.put(AttributeIds.URN_ACTION_ID, methodMapping.getActionName());
            }
            catch (final IndexOutOfBoundsException e) {
                break;
            }

            requests.add(request);
            index++;
        }
        while (!methodMapping.isSingleResource());

        return requests;
    }

    /**
     * Creates XACML resource attributes for the provided data.<br>
     * <p/>
     * For this the arguments' data referenced by the provided invocation mappings are relevant. Each of these
     * referenced objects will be transformed into an XACML resource attribute.
     *
     * @param arguments          The arguments of the method call.
     * @param invocationMappings The invocation mappings for that the request shall be generated.
     * @param isArray            Flag that indicates that the given arguments parameter is an array (<code>true</code>)
     *                           or not (<code>false</code>).
     * @param index              The index of the current object.
     * @return Returns a <code>Map</code> from attribute urn to attribute value defining the resource attributes.
     * @throws MissingMethodParameterException
     *                                        Thrown if an invocation mapping references an argument that is set to
     *                                        <code>null</code>.
     * @throws MissingAttributeValueException Thrown if an invocation mapping references an attribute in an argument
     *                                        that holds XML data but the attribute cannot be found.
     * @throws MissingElementValueException   Thrown if an invocation mapping references an element in an argument that
     *                                        holds XML data but the element cannot be found.
     * @throws WebserverSystemException       Thrown if there is a problem with an invocation mapping.
     * @throws de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException
     */
    private Map<String, String> setupResourceAttributes(
        final Object arguments, final Iterable<InvocationMapping> invocationMappings, final boolean isArray,
        final int index) throws MissingMethodParameterException, MissingAttributeValueException,
        MissingElementValueException, WebserverSystemException, XmlCorruptedException {

        if (arguments == null || invocationMappings == null) {
            return new HashMap<String, String>();
        }

        final Map<String, String> resourceAttributes = new HashMap<String, String>();
        boolean resourceIdProvided = false;
        boolean subresourceIdProvided = false;

        // for each invocation mapping...
        for (final InvocationMapping invocationMapping : invocationMappings) {
            // FIXME: resolve this
            final StringAttribute value = getValueForInvocationMapping(arguments, isArray, index, invocationMapping);

            // and put the resource attribute in the Vector
            if (value != null) {
                final String attributeId = invocationMapping.getAttributeId();
                if (attributeId.equals(EvaluationCtx.RESOURCE_ID)) {
                    // found the resource ID
                    resourceIdProvided = true;
                }
                else if (matcherSubresource.reset(attributeId).matches()) {
                    resourceAttributes.put(AttributeIds.URN_SUBRESOURCE_ATTR, value.getValue());
                    subresourceIdProvided = true;
                }
                resourceAttributes.put(attributeId, value.getValue());
            }
        }

        // we need the resource-id. If not provided, create empty element
        if (!resourceIdProvided) {
            resourceAttributes.put(EvaluationCtx.RESOURCE_ID, "");
        }

        // we need the subresource-id. If not provided, create empty element
        if (!subresourceIdProvided) {
            resourceAttributes.put(AttributeIds.URN_SUBRESOURCE_ATTR, "");
        }

        return resourceAttributes;
    }

    /**
     * Gets the {@link StringAttribute} for the provided values.
     *
     * @param arguments         The arguments of the method call.
     * @param isArray           Flag that indicates that the given arguments parameter is an array (<code>true</code>)
     *                          or not (<code>false</code>).
     * @param index             The index of the current object.
     * @param invocationMapping The {@link InvocationMapping} to get the value for.
     * @return Returns a {@link StringAttribute} with the value for the invocation mapping.
     * @throws MissingMethodParameterException
     *                                  Thrown if a mandatory method parameter is not provided.
     * @throws WebserverSystemException Thrown in case of an internal error.
     * @throws de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException
     */
    private StringAttribute getValueForInvocationMapping(
        final Object arguments, final boolean isArray, final int index, final InvocationMapping invocationMapping)
        throws WebserverSystemException, MissingMethodParameterException, XmlCorruptedException {

        // set the value
        final StringAttribute value;
        if (invocationMapping.getMappingType() == InvocationMapping.VALUE_MAPPING) {
            // fetch the value from inside the invocation mapping
            value = new StringAttribute(invocationMapping.getValue());
        }
        else {
            // Get the current object addressed by the invocation mapping
            final Object currentObject;
            if (isArray) {
                currentObject = ((Object[]) arguments)[invocationMapping.getPosition()];
            }
            else {
                if (invocationMapping.getPosition() != 0) {
                    throw new WebserverSystemException("Invocation mapping error. Position "
                        + invocationMapping.getPosition() + " invalid for single argument, must be 0. [id="
                        + invocationMapping.getId() + ']');
                }
                currentObject = arguments;
            }

            // assert the addressed object has been provided
            if (currentObject == null) {
                throw new MissingMethodParameterException("The parameter at specified " + "position must be provided"
                    + (invocationMapping.getPosition() + 1));
            }
            if (invocationMapping.getMappingType() == InvocationMapping.SIMPLE_ATTRIBUTE_MAPPING) {
                value = new StringAttribute(currentObject.toString());
            }
            else if (invocationMapping.getMappingType() == InvocationMapping.XML_ATTRIBUTE_MAPPING
                || invocationMapping.getMappingType() == InvocationMapping.OPTIONAL_XML_ATTRIBUTE_MAPPING) {
                // fetch the value from XML document
                final Document document;
                try {
                    document = documentCache.retrieveDocument(currentObject);
                }
                catch (final SAXException e) {
                    throw new XmlCorruptedException(StringUtility.format("Parsing of provided XML data failed. ", e
                        .getMessage()), e);
                }
                catch (final Exception e) {
                    throw new WebserverSystemException("Internal error. Parsing of XML failed.", e);
                }

                String path = invocationMapping.getPath();
                boolean extractObjidNeeded = false;
                if (path.startsWith("extractObjid:")) {
                    path = path.substring("extractObjid:".length());
                    extractObjidNeeded = true;
                }
                final String xpath = PATTERN_INDEXED.matcher(path).replaceAll("[" + (index + 1) + ']');
                final NodeList nodeList;
                try {
                    nodeList = XPathAPI.selectNodeList(document, xpath);
                }
                catch (final TransformerException e) {
                    throw new WebserverSystemException(StringUtility.format("Invocation mapping error. Xpath invalid?",
                        xpath, index, invocationMapping.getId()), e);
                }

                if (nodeList == null || nodeList.getLength() == 0) {
                    if (index > 0) {
                        throw new IndexOutOfBoundsException();
                    }
                    else if (invocationMapping.getMappingType() == InvocationMapping.XML_ATTRIBUTE_MAPPING) {
                        throw new XmlCorruptedException(StringUtility.format("Expected value not found "
                            + "in provided XML data ", xpath));
                    }
                    else {
                        // skip undefined optional attribute by setting
                        // the value to null.
                        value = null;
                    }
                }
                else {
                    int length = 1;
                    if (invocationMapping.isMultiValue()) {
                        length = nodeList.getLength();
                    }
                    final Collection<String> values = new HashSet<String>();
                    for (int i = 0; i < length; i++) {
                        final Node node = nodeList.item(i);
                        String tmpValue = null;
                        if (node.getFirstChild() != null) {
                            tmpValue = node.getFirstChild().getNodeValue();
                        }
                        else {
                            tmpValue = "";
                        }
                        if (tmpValue != null) {
                            if (extractObjidNeeded) {
                                tmpValue = XmlUtility.getIdFromURI(tmpValue);
                            }
                            values.add(tmpValue.trim());
                        }
                    }
                    if (values.isEmpty()) {
                        value = null;
                    }
                    else {
                        final StringBuilder valueBuf = new StringBuilder("");
                        for (final String val : values) {
                            if (!val.isEmpty()) {
                                if (valueBuf.length() > 0) {
                                    valueBuf.append(' ');
                                }
                                valueBuf.append(val);
                            }
                        }
                        value = new StringAttribute(valueBuf.toString());
                    }
                }
            }
            else {
                throw new WebserverSystemException(StringUtility.format("Unsupported invocation mapping type",
                    invocationMapping.getMappingType(), invocationMapping.getId()));
            }
        }
        return value;
    }
}
