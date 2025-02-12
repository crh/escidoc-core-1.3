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
package de.escidoc.core.test.st;

import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.remote.application.notfound.StagingFileNotFoundException;
import de.escidoc.core.test.EscidocRestSoapTestBase;
import de.escidoc.core.test.common.client.servlet.Constants;
import de.escidoc.core.test.common.client.servlet.HttpHelper;
import de.escidoc.core.test.security.client.PWCallback;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test suite for the StagingFile.
 *
 * @author Torsten Tetteroo
 */
public abstract class StagingFileTest extends StagingFileTestBase {

    private final String testUploadFile = "UploadTest.zip";

    private final String testUploadFileMimeType = "application/zip";

    /**
     * Constructor.
     */
    public StagingFileTest() throws Exception {
        super(Constants.TRANSPORT_REST);
    }

    /**
     * @param transport The transport identifier.
     * @throws Exception If anything fails.
     */
    protected StagingFileTest(final int transport) throws Exception {

        super(transport);
        if (transport != Constants.TRANSPORT_REST) {
            throw new Exception("Provided Transport not supported [" + transport + "]");
        }
    }

    /**
     * Test successfully creating a StagingFile.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testSTCsf1() throws Exception {

        InputStream fileInputStream = StagingFileTestBase.getFileInputStream(testUploadFile);

        HttpResponse httpRes = null;
        try {
            httpRes = create(fileInputStream, testUploadFileMimeType, testUploadFile);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.failException(e);
        }
        assertNotNull("No HTTPMethod. ", httpRes);
        assertHttpStatusOfMethod("Create failed", httpRes);
        final String stagingFileXml = EntityUtils.toString(httpRes.getEntity(), HTTP.UTF_8);

        EscidocRestSoapTestBase.assertXmlValidStagingFile(stagingFileXml);
        Document document = EscidocRestSoapTestBase.getDocument(stagingFileXml);
        assertXmlExists("No xlink type", document, "/staging-file/@type");
        assertXmlExists("No xlink href", document, "/staging-file/@href");
        assertXmlExists("No last modification date", document, "/staging-file/@last-modification-date");
    }

    /**
     * Test declining the creation of a StagingFile without binary content.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testSTCsf2() throws Exception {

        try {
            create(null, testUploadFileMimeType, testUploadFile);
            EscidocRestSoapTestBase.failMissingException(MissingMethodParameterException.class);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.assertExceptionType("", MissingMethodParameterException.class, e);
        }
    }

    /**
     * Test successfully retrieving staging file.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testSTRsf1() throws Exception {

        InputStream fileInputStream = StagingFileTestBase.getFileInputStream(testUploadFile);
        HttpResponse httpRes = null;
        try {
            httpRes = create(fileInputStream, testUploadFileMimeType, testUploadFile);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.failException(e);
        }
        assertNotNull("No HTTPMethod. ", httpRes);
        assertHttpStatusOfMethod("Create failed", httpRes);
        Document document = EscidocRestSoapTestBase.getDocument(EntityUtils.toString(httpRes.getEntity(), HTTP.UTF_8));

        String objidValue = getIdFromRootElementHref(document);

        try {
            PWCallback.setHandle("");
            httpRes = retrieveStagingFile(objidValue);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.failException(e);
        }
        finally {
            PWCallback.resetHandle();
        }
        assertNotNull("Got no HTTP method object", httpRes);
        assertHttpStatusOfMethod("Retrieve failed", httpRes);
        final Header contentTypeHeader = httpRes.getFirstHeader(HttpHelper.HTTP_HEADER_CONTENT_TYPE);
        assertNotNull("Retrieve failed! No returned mime type found", contentTypeHeader);
        assertEquals("Retrieve failed! The returned mime type is wrong,", testUploadFileMimeType, contentTypeHeader
            .getValue());
        StagingFileTestBase.assertResponseContentMatchesSourceFile(httpRes, testUploadFile);

    }

    /**
     * Test declining the retrieval of a StagingFile with missing parameter token.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testSTRsf2() throws Exception {

        try {
            retrieveStagingFile(null);
            EscidocRestSoapTestBase.failMissingException(MissingMethodParameterException.class);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.assertExceptionType("Unexpected exception, ",
                MissingMethodParameterException.class, e);
        }
    }

    /**
     * Test declining the retrieval of a StagingFile with unknown token.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testSTRsf4() throws Exception {

        try {
            retrieveStagingFile(UNKNOWN_ID);
            EscidocRestSoapTestBase.failMissingException(StagingFileNotFoundException.class);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase
                .assertExceptionType("Unexpected exception, ", StagingFileNotFoundException.class, e);
        }
    }

    /**
     * Test declining the retrieval of a StagingFile with providing the id of an existing resource of another type.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testSTRsf4_2() throws Exception {

        try {
            retrieveStagingFile(CONTEXT_ID);
            EscidocRestSoapTestBase.failMissingException(StagingFileNotFoundException.class);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase
                .assertExceptionType("Unexpected exception, ", StagingFileNotFoundException.class, e);
        }
    }

    /**
     * Test declining the retrieval of a staging file that has been previously retrieved.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testSTRsf8() throws Exception {

        InputStream fileInputStream = StagingFileTestBase.getFileInputStream(testUploadFile);
        HttpResponse httpRes = null;
        try {
            httpRes = create(fileInputStream, testUploadFileMimeType, testUploadFile);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.failException(e);
        }
        assertNotNull("No HTTPMethod. ", httpRes);
        assertHttpStatusOfMethod("Create failed", httpRes);
        Document document = EscidocRestSoapTestBase.getDocument(EntityUtils.toString(httpRes.getEntity(), HTTP.UTF_8));

        String objidValue = getIdFromRootElementHref(document);

        try {
            httpRes = retrieveStagingFile(objidValue);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.failException(e);
        }

        try {
            httpRes = retrieveStagingFile(objidValue);
            EscidocRestSoapTestBase.failMissingException("Upload Servlet's get method did not decline"
                + " repeated retrieval of a staging file, ", StagingFileNotFoundException.class);
        }
        catch (final Exception e) {
            EscidocRestSoapTestBase.assertExceptionType("Upload Servlet's get method did not decline"
                + " repeated retrieval of a staging file, correctly, ", StagingFileNotFoundException.class, e);
        }

    }

}
