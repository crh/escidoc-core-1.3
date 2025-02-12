package de.escidoc.core.test.oai.setdefinition;

import de.escidoc.core.test.EscidocRestSoapTestBase;
import de.escidoc.core.test.common.client.servlet.Constants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(value = Parameterized.class)
public class SetDefinitionFilterTest extends SetDefinitionTestBase {

    /**
     * @param transport The transport identifier.
     */
    public SetDefinitionFilterTest(final int transport) {
        super(transport);
    }

    /**
     * Test successful retrieving one existing set definition resource using filter set specification.
     *
     * @throws Exception e
     */
    @Test
    public void testRetrieveBySetSpecificationCQL() throws Exception {
        Document createdSetDefinitionDocument = createSuccessfully("escidoc_setdefinition_for_create.xml");
        String objid = getObjidValue(createdSetDefinitionDocument);
        String setSpecValue =
            selectSingleNode(createdSetDefinitionDocument, "/set-definition/specification").getTextContent();
        final Map<String, String[]> filterParams = new HashMap<String, String[]>();
        filterParams.put(FILTER_PARAMETER_QUERY, new String[] { "\"" + FILTER_SET_SPECIFICATION + "\"=\""
            + setSpecValue });
        String retrievedSetDefinitionsXml = null;

        try {
            retrievedSetDefinitionsXml = retrieveSetDefinitions(filterParams);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.failException("Retrieving of list of set definitions failed. ", e);
        }
        assertXmlValidSrwResponse(retrievedSetDefinitionsXml);
        final Document retrievedDocument = EscidocRestSoapTestBase.getDocument(retrievedSetDefinitionsXml);
        final NodeList setDefinitionNodes =
            selectNodeList(retrievedDocument, XPATH_SRW_SET_DEFINITION_LIST_SET_DEFINITION);
        assertEquals("Unexpected number of set definitions.", 1, setDefinitionNodes.getLength());
        String objidInTheList = null;
        if (getTransport() == Constants.TRANSPORT_REST) {
            String href =
                selectSingleNode(retrievedDocument, XPATH_SRW_SET_DEFINITION_LIST_SET_DEFINITION + "/@href")
                    .getNodeValue();
            objidInTheList = getIdFromHrefValue(href);
        }
        else {
            objidInTheList =
                selectSingleNode(retrievedDocument, XPATH_SRW_SET_DEFINITION_LIST_SET_DEFINITION + "/@objid")
                    .getNodeValue();
        }
        assertEquals("objid of the set definition is wrong", objid, objidInTheList);
    }

    /**
     * Test successful retrieving existing set definitions resource using filter set specification pattern.
     *
     * @throws Exception e
     */
    @Test
    public void testRetrieveBySetSpecificationLikeCQL() throws Exception {
        createSuccessfully("escidoc_setdefinition_for_create.xml");
        final Map<String, String[]> filterParams = new HashMap<String, String[]>();
        filterParams.put(FILTER_PARAMETER_QUERY, new String[] { "\"" + FILTER_SET_SPECIFICATION + "\"=\"%Set1%" });
        String retrievedSetDefinitionsXml = null;

        try {
            retrievedSetDefinitionsXml = retrieveSetDefinitions(filterParams);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.failException("Retrieving of list of set definitions failed. ", e);
        }
        assertXmlValidSrwResponse(retrievedSetDefinitionsXml);
        final Document retrievedDocument = EscidocRestSoapTestBase.getDocument(retrievedSetDefinitionsXml);
        final NodeList setDefinitionNodes =
            selectNodeList(retrievedDocument, XPATH_SRW_SET_DEFINITION_LIST_SET_DEFINITION);
        int length = setDefinitionNodes.getLength();
        boolean moreThenOne = length > 1;
        assertEquals("Wrong number in the list", true, moreThenOne);
    }

    /**
     * Test successful retrieving existing set definitions without filter.
     *
     * @throws Exception e
     */
    @Test
    public void testRetrieveSetDefinitionsCQL() throws Exception {
        final Map<String, String[]> filterParams = new HashMap<String, String[]>();
        filterParams.put(FILTER_PARAMETER_MAXIMUMRECORDS, new String[] { "6" });
        String retrievedSetDefinitionsXml = null;

        try {
            retrievedSetDefinitionsXml = retrieveSetDefinitions(filterParams);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.failException("Retrieving of list of set definitions failed. ", e);
        }
        assertXmlValidSrwResponse(retrievedSetDefinitionsXml);
        final Document retrievedDocument = EscidocRestSoapTestBase.getDocument(retrievedSetDefinitionsXml);
        final NodeList setDefinitionNodes =
            selectNodeList(retrievedDocument, XPATH_SRW_SET_DEFINITION_LIST_SET_DEFINITION);
        int length = setDefinitionNodes.getLength();
        boolean moreThenOne = length >= 1;
        assertEquals("Wrong number in the list", true, moreThenOne);
    }

    /**
     * Test successfully retrieving an explain response.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void explainTest() throws Exception {
        final Map<String, String[]> filterParams = new HashMap<String, String[]>();

        filterParams.put(EscidocRestSoapTestBase.FILTER_PARAMETER_EXPLAIN, new String[] { "" });

        String result = null;

        try {
            result = retrieveSetDefinitions(filterParams);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.failException(e);
        }
        assertXmlValidSrwResponse(result);
    }
}
