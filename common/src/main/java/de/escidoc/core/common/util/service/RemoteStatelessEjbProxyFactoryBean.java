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

package de.escidoc.core.common.util.service;

import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.common.util.string.StringUtility;
import org.aopalliance.intercept.MethodInvocation;
import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityAssociation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ejb.access.AbstractRemoteSlsbInvokerInterceptor;
import org.springframework.ejb.access.SimpleRemoteStatelessSessionProxyFactoryBean;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.util.MethodInvocationUtils;

import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * Customized proxy factory Bean for the Remote and Stateless EJB lookup. Provides the current {@link SecurityContext}
 * as an additional parameter in methods calls.<br> These extended methods must be defined in an interface that has a
 * qualified name that can be constructed from the qualified name of the business interface by replacing "Interface"
 * with "Remote" and "service.interfaces" with "ejb.interfaces", e.g. "de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface"
 * -> "de.escidoc.core.aa.ejb.interfaces.UserAccountHandlerRemote"
 *
 * @author Torsten Tetteroo
 */
public class RemoteStatelessEjbProxyFactoryBean extends SimpleRemoteStatelessSessionProxyFactoryBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteStatelessEjbProxyFactoryBean.class);

    /**
     * Pattern used to replace Interface with Remote in class name to get the name of the extended interface.
     */
    private static final Pattern PATTERN_INTERFACE = Pattern.compile("Interface");

    /**
     * Pattern used to replace "service.interface" with "ejb.interface" in class name to get the name of the extended
     * interface.
     */
    private static final Pattern PATTERN_SERVIVE_INTERFACE = Pattern.compile("service.interfaces");

    private String packageName;

    /**
     * The extended interface that provides the same methods as the business interface but each with an additional
     * parameter to forward the {@link SecurityContext}.
     */
    private Class extendedInterface;

    /**
     * See Interface for functional description.
     */
    @Override
    public void afterPropertiesSet() throws NamingException {

        try {
            this.setJndiEnvironment(EjbFactoryBeanHelper.getInitialContextJndiProperties(this.packageName));
        }
        catch (final WebserverSystemException e) {
            final NamingException ex = new NamingException(); // Ignore FindBugs
            ex.setRootCause(e);
            throw ex;
        }
        final String className =
            PATTERN_SERVIVE_INTERFACE.matcher(
                PATTERN_INTERFACE.matcher(getBusinessInterface().getName()).replaceAll("Remote")).replaceAll(
                "ejb.interfaces");
        try {
            this.extendedInterface = Class.forName(className);
        }
        catch (final ClassNotFoundException e) {
            final NamingException ex = new NamingException(); // Ignore FindBugs
            ex.setRootCause(e);
            throw ex;
        }

        super.afterPropertiesSet();
    }

    /**
     * See Interface for functional description.
     *
     * @see AbstractRemoteSlsbInvokerInterceptor #invoke(org.aopalliance.intercept.MethodInvocation) // *
     */
    @Override
    public Object invoke(final MethodInvocation arg0) throws Throwable, InvocationTargetException {
        final Method method = arg0.getMethod();
        final Object[] args = arg0.getArguments();
        final Class[] argTypes = method.getParameterTypes();

        final int argsLength = args.length;
        final Object[] extendedArgs;
        final Class[] extendedArgsTypes;

        try {
            final SecurityContext securityContext = UserContext.getSecurityContext();
            if (securityContext == null) {
                throw new SystemException("Security context not set.");
            }
            final Authentication authentication = securityContext.getAuthentication();
            if (authentication == null) {
                throw new SystemException("Security context does not hold an authentication object.");
            }
            if (authentication.isAuthenticated()) {
                extendedArgs = new Object[argsLength + 1];
                extendedArgsTypes = new Class[extendedArgs.length];

                extendedArgs[argsLength] = securityContext;
                extendedArgsTypes[argsLength] = SecurityContext.class;
                SecurityAssociation.pushRunAsIdentity(new RunAsIdentity("Administrator", ""));
            }
            else {
                // user currently not authenticated. Just user name and
                // handle/password are forwarded
                extendedArgs = new Object[argsLength + 2];
                extendedArgsTypes = new Class[extendedArgs.length];

                int i = argsLength;
                extendedArgs[i] = authentication.getCredentials();
                extendedArgsTypes[i] = String.class;
                i++;
                extendedArgs[i] = UserContext.isRestAccess();
                extendedArgsTypes[i] = Boolean.class;
            }
        }
        catch (final SystemException e1) {
            throw new InvocationTargetException(e1);
        }

        for (int i = 0; i < argsLength; i++) {
            extendedArgs[i] = args[i];
            extendedArgsTypes[i] = argTypes[i];
        }

        final String methodName = method.getName();
        final MethodInvocation extendedMethodInvocation =
            MethodInvocationUtils.createFromClass(null, this.extendedInterface, methodName, extendedArgsTypes,
                extendedArgs);
        if (extendedMethodInvocation == null) {
            throw new InvocationTargetException(new SystemException(StringUtility.format(
                "Remote Invocation failed, could not find extended target method.", this.extendedInterface, methodName,
                extendedArgs)));
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(StringUtility.format("Calling super.invoke", methodName, extendedArgs));
        }
        return super.invoke(extendedMethodInvocation);
    }

    /**
     * Injects the package name.
     *
     * @param packageName the packageName to set
     */
    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

}
