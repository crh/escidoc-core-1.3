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
package de.escidoc.core.om.business.indexer;

import de.escidoc.core.common.business.fedora.EscidocBinaryContent;
import de.escidoc.core.common.business.fedora.MIMETypedStream;
import de.escidoc.core.common.business.fedora.TripleStoreUtility;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
import de.escidoc.core.common.servlet.invocation.BeanMethod;
import de.escidoc.core.common.servlet.invocation.MethodMapper;
import de.escidoc.core.common.servlet.invocation.exceptions.MethodNotFoundException;
import de.escidoc.core.common.util.IOUtils;
import de.escidoc.core.common.util.configuration.EscidocConfiguration;
import de.escidoc.core.common.util.service.BeanLocator;
import de.escidoc.core.common.util.service.ConnectionUtility;
import de.escidoc.core.common.util.service.UserContext;
import de.escidoc.core.common.util.xml.XmlUtility;
import de.escidoc.core.common.util.xml.factory.FoXmlProvider;
import de.escidoc.core.om.business.fedora.deviation.Constants;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Michael Hoppe
 *         <p/>
 *         Singleton for caching Resources (items, container, fulltexts) For indexing by fedoragsearch
 */
public final class IndexerResourceCache {

    /**
     * Fall back value if reading property {@link <code>EscidocConfiguration.INDEXER_CACHE_SIZE</code>} fails.
     */
    private static final int INDEXER_CACHE_SIZE_FALL_BACK = 30;

    private int indexerCacheSize;

    private static final int BUFFER_SIZE = 0xFFFF;

    /**
     * Holds identifier and object.
     */
    private final Cache resources;

    private MethodMapper methodMapper;

    private TripleStoreUtility tripleStoreUtility;

    private ConnectionUtility connectionUtility;

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexerResourceCache.class);

    private static final IndexerResourceCache instance = new IndexerResourceCache();

    /**
     * private Constructor for Singleton.
     */
    private IndexerResourceCache() {
        try {
            this.methodMapper =
                (MethodMapper) BeanLocator.getBean("Common.spring.ejb.context", "common.CommonMethodMapper");
            this.connectionUtility =
                (ConnectionUtility) BeanLocator.getBean("Common.spring.ejb.context",
                    "escidoc.core.common.util.service.ConnectionUtility");
            this.tripleStoreUtility = TripleStoreUtility.getInstance();

            //initialize map that holds cached Objects for indexing
            try {
                this.indexerCacheSize =
                    Integer.parseInt(EscidocConfiguration.getInstance().get(
                        EscidocConfiguration.ESCIDOC_CORE_INDEXER_CACHE_SIZE));
            }
            catch (final Exception e) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Error on parsing indexer resource cache size.");
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Error on parsing indexer resource cache size.", e);
                }
                this.indexerCacheSize = INDEXER_CACHE_SIZE_FALL_BACK;
            }
        }
        catch (final Exception e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Error on initializing indexer resource cache.");
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error on initializing indexer resource cache.", e);
            }
        }
        final CacheManager cacheManager = CacheManager.create();
        this.resources = new Cache(new CacheConfiguration("resourcesCache", this.indexerCacheSize));
        cacheManager.addCache(this.resources);
    }

    /**
     * Only initialize Object once. Check for old objects in cache.
     *
     * @return IndexerResourceCache IndexerResourceCache
     */
    public static IndexerResourceCache getInstance() {
        return instance;
    }

    /**
     * Get resource with given identifier.
     *
     * @param identifier identifier
     * @return Object resource-object
     * @throws SystemException e
     * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
     */
    public Object getResource(final String identifier) throws SystemException, TripleStoreSystemException {
        final String href = getHref(identifier);
        if (getResourceWithInternalKey(href) == null) {
            if (identifier.startsWith("http")) {
                cacheExternalResource(href);
            }
            else {
                cacheInternalResource(href);
            }
        }
        return getResourceWithInternalKey(href);
    }

    /**
     * Set resource with given identifier.
     *
     * @param identifier identifier
     * @param resource   resource-object
     * @throws SystemException e
     * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
     */
    public void setResource(final String identifier, final Object resource) throws SystemException,
        TripleStoreSystemException {
        final String href = getHref(identifier);
        final Element element = new Element(href, resource);
        resources.put(element);
    }

    /**
     * Get resource with given identifier.
     *
     * @param identifier identifier
     * @return Resource with required identifier.
     * @throws SystemException e
     */
    private Object getResourceWithInternalKey(final String identifier) throws SystemException {
        final Element element = resources.get(identifier);
        return element != null ? element.getObjectValue() : null;
    }

    /**
     * delete resource with given identifier.
     *
     * @param identifier identifier
     * @throws SystemException e
     * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
     */
    public void deleteResource(final String identifier) throws SystemException, TripleStoreSystemException {
        final String href = getHref(identifier);
        final Collection<String> keys = new ArrayList<String>();
        for (final Object key : resources.getKeys()) {
            final String keyAsString = (String) key;
            if (keyAsString.startsWith(href)) {
                keys.add(keyAsString);
            }
        }
        for (final String key : keys) {
            resources.remove(key);
        }
    }

    /**
     * delete resource with given identifier.
     *
     * @param identifier identifier
     * @param resource
     * @throws SystemException e
     * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
     */
    public void replaceResource(final String identifier, final Object resource) throws SystemException,
        TripleStoreSystemException {
        final String href = getHref(identifier);
        final Collection<String> keys = new ArrayList<String>();
        for (final Object key : resources.getKeys()) {
            final String keyAsString = (String) key;
            if (keyAsString.startsWith(href)) {
                keys.add(keyAsString);
            }
        }
        for (final String key : keys) {
            resources.remove(key);
        }
        final Element element = new Element(href, resource);
        resources.put(element);
    }

    /**
     * get resource with given identifier from framework and write it into cache.
     *
     * @param identifier identifier
     * @throws SystemException e
     */
    private void cacheInternalResource(final String identifier) throws SystemException {
        try {
            if (UserContext.getHandle() != null) {
                UserContext.setRestAccess(Constants.USE_REST_REQUEST_PROTOCOL);
            }
            final BeanMethod method = methodMapper.getMethod(identifier, null, null, "GET", "");
            final Object content = method.invokeWithProtocol(null, Constants.USE_REST_REQUEST_PROTOCOL);
            if (content != null && "EscidocBinaryContent".equals(content.getClass().getSimpleName())) {
                final EscidocBinaryContent escidocBinaryContent = (EscidocBinaryContent) content;
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                final InputStream in = escidocBinaryContent.getContent();
                try {
                    final byte[] bytes = new byte[BUFFER_SIZE];
                    int i;
                    while ((i = in.read(bytes)) > -1) {
                        out.write(bytes, 0, i);
                    }
                    out.flush();
                    final MIMETypedStream stream =
                        new MIMETypedStream(escidocBinaryContent.getMimeType(), out.toByteArray(), null);
                    setResource(identifier, stream);
                }
                catch (final Exception e) {
                    throw new SystemException(e);
                }
                finally {
                    IOUtils.closeStream(in);
                    IOUtils.closeStream(out);
                }
            }
            else if (content != null) {
                final String xml = (String) content;
                setResource(identifier, xml);
            }
        }
        catch (final InvocationTargetException e) {
            if (!"AuthorizationException".equals(e.getTargetException().getClass().getSimpleName())
                && !"InvalidStatusException".equals(e.getTargetException().getClass().getSimpleName())) {
                throw new SystemException(e);
            }
        }
        catch (final MethodNotFoundException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Error on caching internal resource.");
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error on caching internal resource.", e);
            }
        }
        catch (final Exception e) {
            throw new SystemException(e);
        }
    }

    /**
     * get resource with given URL and write it into cache.
     *
     * @param identifier identifier
     * @throws SystemException e
     */
    private void cacheExternalResource(final String identifier) throws SystemException {
        ByteArrayOutputStream out = null;
        InputStream in = null;
        try {
            final HttpResponse httpResponse = connectionUtility.getRequestURL(new URL(identifier));

            if (httpResponse != null) {

                // TODO testen ob header mitgeschickt wird
                final Header ctype = httpResponse.getFirstHeader("Content-Type");
                final String mimeType =
                    ctype != null ? ctype.getValue() : FoXmlProvider.MIME_TYPE_APPLICATION_OCTET_STREAM;

                out = new ByteArrayOutputStream();
                in = httpResponse.getEntity().getContent();
                int byteval;
                while ((byteval = in.read()) > -1) {
                    out.write(byteval);
                }
                final MIMETypedStream stream = new MIMETypedStream(mimeType, out.toByteArray(), null);
                setResource(identifier, stream);
            }
        }
        catch (final Exception e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Error on caching external resource.");
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error on caching external resource.", e);
            }
        }
        finally {
            IOUtils.closeStream(in);
            IOUtils.closeStream(out);
        }
    }

    /**
     * generate href out of pid.
     *
     * @param identifier identifier
     * @return String href
     * @throws SystemException e
     * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
     */
    private String getHref(final String identifier) throws SystemException, TripleStoreSystemException {
        String href = identifier;
        if (!href.contains("/")) {
            // objectId provided, generate href
            // get object-type
            href = XmlUtility.getObjidWithoutVersion(href);
            final String objectType = tripleStoreUtility.getObjectType(href);
            if (objectType == null) {
                throw new SystemException("couldnt get objectType for object " + href);
            }

            href = XmlUtility.getHref(objectType, identifier);
        }
        return href;
    }

}
