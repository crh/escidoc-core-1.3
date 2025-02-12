package de.escidoc.core.cmm.ejb.interfaces;

import de.escidoc.core.common.business.fedora.EscidocBinaryContent;
import de.escidoc.core.common.exceptions.EscidocException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
import de.escidoc.core.common.exceptions.application.missing.MissingElementValueException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.notfound.ContentModelNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.ContentStreamNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.application.violated.LockingException;
import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
import de.escidoc.core.common.exceptions.application.violated.ReadonlyVersionException;
import de.escidoc.core.common.exceptions.application.violated.ResourceInUseException;
import de.escidoc.core.common.exceptions.system.SystemException;
import org.springframework.security.context.SecurityContext;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * Remote interface for ContentModelHandler.
 */
public interface ContentModelHandlerRemote extends EJBObject {

    String create(String xmlData, SecurityContext securityContext) throws InvalidContentException,
        MissingAttributeValueException, SystemException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, XmlCorruptedException, XmlSchemaValidationException, RemoteException;

    String create(String xmlData, String authHandle, Boolean restAccess) throws InvalidContentException,
        MissingAttributeValueException, SystemException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, XmlCorruptedException, XmlSchemaValidationException, RemoteException;

    void delete(String id, SecurityContext securityContext) throws SystemException, ContentModelNotFoundException,
        AuthenticationException, AuthorizationException, MissingMethodParameterException, LockingException,
        InvalidStatusException, ResourceInUseException, RemoteException;

    void delete(String id, String authHandle, Boolean restAccess) throws SystemException,
        ContentModelNotFoundException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, LockingException, InvalidStatusException, ResourceInUseException,
        RemoteException;

    String ingest(String xmlData, SecurityContext securityContext) throws AuthenticationException,
        AuthorizationException, MissingMethodParameterException, SystemException, MissingAttributeValueException,
        MissingElementValueException, ContentModelNotFoundException, InvalidXmlException, InvalidStatusException,
        EscidocException, RemoteException;

    String ingest(String xmlData, String authHandle, Boolean restAccess) throws AuthenticationException,
        AuthorizationException, MissingMethodParameterException, SystemException, MissingAttributeValueException,
        MissingElementValueException, ContentModelNotFoundException, InvalidXmlException, InvalidStatusException,
        EscidocException, RemoteException;

    String retrieve(String id, SecurityContext securityContext) throws ContentModelNotFoundException, SystemException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, RemoteException;

    String retrieve(String id, String authHandle, Boolean restAccess) throws ContentModelNotFoundException,
        SystemException, MissingMethodParameterException, AuthenticationException, AuthorizationException,
        RemoteException;

    String retrieveProperties(String id, SecurityContext securityContext) throws ContentModelNotFoundException,
        SystemException, AuthenticationException, AuthorizationException, MissingMethodParameterException,
        RemoteException;

    String retrieveProperties(String id, String authHandle, Boolean restAccess) throws ContentModelNotFoundException,
        SystemException, AuthenticationException, AuthorizationException, MissingMethodParameterException,
        RemoteException;

    String retrieveContentStreams(String id, SecurityContext securityContext) throws ContentModelNotFoundException,
        SystemException, AuthenticationException, AuthorizationException, MissingMethodParameterException,
        RemoteException;

    String retrieveContentStreams(String id, String authHandle, Boolean restAccess)
        throws ContentModelNotFoundException, SystemException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, RemoteException;

    String retrieveContentStream(String id, String name, SecurityContext securityContext)
        throws ContentModelNotFoundException, SystemException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, RemoteException;

    String retrieveContentStream(String id, String name, String authHandle, Boolean restAccess)
        throws ContentModelNotFoundException, SystemException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, RemoteException;

    EscidocBinaryContent retrieveContentStreamContent(String id, String name, SecurityContext securityContext)
        throws ContentModelNotFoundException, SystemException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, ContentStreamNotFoundException, InvalidStatusException, RemoteException;

    EscidocBinaryContent retrieveContentStreamContent(String id, String name, String authHandle, Boolean restAccess)
        throws ContentModelNotFoundException, SystemException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, ContentStreamNotFoundException, InvalidStatusException, RemoteException;

    String retrieveResources(String id, SecurityContext securityContext) throws ContentModelNotFoundException,
        SystemException, AuthenticationException, AuthorizationException, MissingMethodParameterException,
        RemoteException;

    String retrieveResources(String id, String authHandle, Boolean restAccess) throws ContentModelNotFoundException,
        SystemException, AuthenticationException, AuthorizationException, MissingMethodParameterException,
        RemoteException;

    String retrieveVersionHistory(String id, SecurityContext securityContext) throws ContentModelNotFoundException,
        SystemException, AuthenticationException, AuthorizationException, MissingMethodParameterException,
        RemoteException;

    String retrieveVersionHistory(String id, String authHandle, Boolean restAccess)
        throws ContentModelNotFoundException, SystemException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, RemoteException;

    String retrieveContentModels(Map parameterMap, SecurityContext securityContext) throws InvalidSearchQueryException,
        SystemException, RemoteException;

    String retrieveContentModels(Map parameterMap, String authHandle, Boolean restAccess)
        throws InvalidSearchQueryException, SystemException, RemoteException;

    String update(String id, String xmlData, SecurityContext securityContext) throws InvalidXmlException,
        ContentModelNotFoundException, OptimisticLockingException, SystemException, AuthenticationException,
        AuthorizationException, MissingMethodParameterException, ReadonlyVersionException,
        MissingAttributeValueException, InvalidContentException, RemoteException;

    String update(String id, String xmlData, String authHandle, Boolean restAccess) throws InvalidXmlException,
        ContentModelNotFoundException, OptimisticLockingException, SystemException, AuthenticationException,
        AuthorizationException, MissingMethodParameterException, ReadonlyVersionException,
        MissingAttributeValueException, InvalidContentException, RemoteException;

    EscidocBinaryContent retrieveMdRecordDefinitionSchemaContent(String id, String name, SecurityContext securityContext)
        throws AuthenticationException, AuthorizationException, MissingMethodParameterException,
        ContentModelNotFoundException, SystemException, RemoteException;

    EscidocBinaryContent retrieveMdRecordDefinitionSchemaContent(
        String id, String name, String authHandle, Boolean restAccess) throws AuthenticationException,
        AuthorizationException, MissingMethodParameterException, ContentModelNotFoundException, SystemException,
        RemoteException;

    EscidocBinaryContent retrieveResourceDefinitionXsltContent(String id, String name, SecurityContext securityContext)
        throws AuthenticationException, AuthorizationException, MissingMethodParameterException,
        ContentModelNotFoundException, ResourceNotFoundException, SystemException, RemoteException;

    EscidocBinaryContent retrieveResourceDefinitionXsltContent(
        String id, String name, String authHandle, Boolean restAccess) throws AuthenticationException,
        AuthorizationException, MissingMethodParameterException, ContentModelNotFoundException,
        ResourceNotFoundException, SystemException, RemoteException;

}
