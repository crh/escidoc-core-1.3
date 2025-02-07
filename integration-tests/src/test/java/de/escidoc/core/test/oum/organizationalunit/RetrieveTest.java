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
package de.escidoc.core.test.oum.organizationalunit;

import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.remote.application.notfound.OrganizationalUnitNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertNotNull;

/**
 * Test the mock implementation of the item resource.
 *
 * @author Michael Schneider
 */
@RunWith(value = Parameterized.class)
public class RetrieveTest extends OrganizationalUnitTestBase {

    /**
     * @param transport The transport identifier.
     */
    public RetrieveTest(final int transport) {
        super(transport);
    }

    /**
     * Tests successful retrieving of an existing OrganizationalUnit.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOumRou1() throws Exception {

        final String createdXml = createSuccessfully("escidoc_ou_create.xml");
        final String objid = getObjidValue(getDocument(createdXml));

        String retrievedXml = null;
        try {
            retrievedXml = retrieve(objid);
        }
        catch (final Exception e) {
            failException("Retrieving existing OU failed.", e);
        }
        assertOrganizationalUnit(retrievedXml, createdXml, startTimestamp, startTimestamp);
    }

    /**
     * Tests declining retrieving an OrganizationalUnit with providing an unknown id.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOumRou2() throws Exception {

        Class ec = OrganizationalUnitNotFoundException.class;
        try {
            retrieve(UNKNOWN_ID);
            failMissingException("Retrieving an OU with unknown id has not been declined.", ec);
        }
        catch (final Exception e) {
            assertExceptionType("Retrieving an OU with unknown id has not been declined," + " correctly.", ec, e);
        }
    }

    /**
     * Tests declining retrieving an OrganizationalUnit with providing the id of a resource of another type.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOumRou2_2() throws Exception {

        Class ec = OrganizationalUnitNotFoundException.class;

        try {
            retrieve(CONTEXT_ID);
            failMissingException("Retrieving an OU with id of resource of another resource type"
                + " has not been declined.", ec);
        }
        catch (final Exception e) {
            assertExceptionType("Retrieving an OU with id of resource of another resource type"
                + " has not been declined, correctly", ec, e);
        }
    }

    /**
     * Test retrieving an OrganizationalUnit without id.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOumROu3() throws Exception {

        Class ec = MissingMethodParameterException.class;
        try {
            retrieve(null);
            failMissingException("Retrieving an OU without id" + " has not been declined.", ec);
        }
        catch (final Exception e) {
            assertExceptionType("Retrieving an OU without id" + " has not been declined, correctly.", ec, e);
        }
    }

    /**
     * Test retrieving the persistent organizational unit object with id "escidoc:persistent1".
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOumROu6() throws Exception {

        String retrievedXml = null;
        try {
            retrievedXml = retrieve("escidoc:persistent1");
        }
        catch (final Exception e) {
            failException(e);
        }
        assertNotNull("No org. unit. data retrieved. ", retrievedXml);
        assertXmlValidOrganizationalUnit(retrievedXml);

    }

    /**
     * Test retrieving the persistent organizational unit object with id "escidoc:persistent11".
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOumROu7() throws Exception {

        String retrievedXml = null;
        try {
            retrievedXml = retrieve("escidoc:persistent11");
        }
        catch (final Exception e) {
            failException(e);
        }
        assertNotNull("No org. unit. data retrieved. ", retrievedXml);
        assertXmlValidOrganizationalUnit(retrievedXml);

    }

    /**
     * Test retrieving the persistent organizational unit object with id "escidoc:persistent13".
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOumROu8() throws Exception {

        String retrievedXml = null;
        try {
            retrievedXml = retrieve("escidoc:persistent13");
        }
        catch (final Exception e) {
            failException(e);
        }
        assertNotNull("No org. unit. data retrieved. ", retrievedXml);
        assertXmlValidOrganizationalUnit(retrievedXml);

    }

    /**
     * Test retrieving the persistent organizational unit object with id "escidoc:persistent1".
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOumROu9() throws Exception {

        String retrievedXml = null;
        try {
            retrievedXml = retrieve("escidoc:persistent1");
        }
        catch (final Exception e) {
            failException(e);
        }
        assertNotNull("No org. unit. data retrieved. ", retrievedXml);
        assertXmlValidOrganizationalUnit(retrievedXml);

    }

    /**
     * Test retrieving the persistent organizational unit object with id "escidoc:persistent22".
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOumROu10() throws Exception {

        String retrievedXml = null;
        try {
            retrievedXml = retrieve("escidoc:persistent22");
        }
        catch (final Exception e) {
            failException(e);
        }
        assertNotNull("No org. unit. data retrieved. ", retrievedXml);
        assertXmlValidOrganizationalUnit(retrievedXml);

    }

}
