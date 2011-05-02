package de.escidoc.core.om.ejb.interfaces;

import de.escidoc.core.common.business.fedora.EscidocBinaryContent;
import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidContextException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
import de.escidoc.core.common.exceptions.application.missing.MissingContentException;
import de.escidoc.core.common.exceptions.application.missing.MissingElementValueException;
import de.escidoc.core.common.exceptions.application.missing.MissingLicenceException;
import de.escidoc.core.common.exceptions.application.missing.MissingMdRecordException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.notfound.ComponentNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.ContentModelNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.ContentRelationNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.ContentStreamNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.ContextNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.FileNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.ItemNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.MdRecordNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.OperationNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.ReferencedResourceNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.RelationPredicateNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.XmlSchemaNotFoundException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.application.violated.AlreadyDeletedException;
import de.escidoc.core.common.exceptions.application.violated.AlreadyExistsException;
import de.escidoc.core.common.exceptions.application.violated.AlreadyPublishedException;
import de.escidoc.core.common.exceptions.application.violated.AlreadyWithdrawnException;
import de.escidoc.core.common.exceptions.application.violated.LockingException;
import de.escidoc.core.common.exceptions.application.violated.NotPublishedException;
import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
import de.escidoc.core.common.exceptions.application.violated.ReadonlyAttributeViolationException;
import de.escidoc.core.common.exceptions.application.violated.ReadonlyElementViolationException;
import de.escidoc.core.common.exceptions.application.violated.ReadonlyVersionException;
import de.escidoc.core.common.exceptions.application.violated.ReadonlyViolationException;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.om.service.interfaces.EscidocServiceRedirectInterface;
import org.springframework.security.context.SecurityContext;

import javax.ejb.EJBLocalObject;
import java.util.Map;

/**
 * Local interface for ItemHandler.
 */
public interface ItemHandlerLocal extends EJBLocalObject {

    String create(String xmlData, SecurityContext securityContext) throws MissingContentException,
        ContextNotFoundException, ContentModelNotFoundException, ReadonlyElementViolationException,
        MissingAttributeValueException, MissingElementValueException, ReadonlyAttributeViolationException,
        AuthenticationException, AuthorizationException, XmlCorruptedException, XmlSchemaValidationException,
        MissingMethodParameterException, FileNotFoundException, SystemException, InvalidContentException,
        ReferencedResourceNotFoundException, RelationPredicateNotFoundException, MissingMdRecordException,
        InvalidStatusException;

    String create(String xmlData, String authHandle, Boolean restAccess) throws MissingContentException,
        ContextNotFoundException, ContentModelNotFoundException, ReadonlyElementViolationException,
        MissingAttributeValueException, MissingElementValueException, ReadonlyAttributeViolationException,
        AuthenticationException, AuthorizationException, XmlCorruptedException, XmlSchemaValidationException,
        MissingMethodParameterException, FileNotFoundException, SystemException, InvalidContentException,
        ReferencedResourceNotFoundException, RelationPredicateNotFoundException, MissingMdRecordException,
        InvalidStatusException;

    void delete(String id, SecurityContext securityContext) throws ItemNotFoundException, AlreadyPublishedException,
        LockingException, AuthenticationException, AuthorizationException, InvalidStatusException,
        MissingMethodParameterException, SystemException;

    void delete(String id, String authHandle, Boolean restAccess) throws ItemNotFoundException,
        AlreadyPublishedException, LockingException, AuthenticationException, AuthorizationException,
        InvalidStatusException, MissingMethodParameterException, SystemException;

    String retrieve(String id, SecurityContext securityContext) throws ItemNotFoundException,
        ComponentNotFoundException, AuthenticationException, AuthorizationException, MissingMethodParameterException,
        SystemException;

    String retrieve(String id, String authHandle, Boolean restAccess) throws ItemNotFoundException,
        ComponentNotFoundException, AuthenticationException, AuthorizationException, MissingMethodParameterException,
        SystemException;

    String update(String id, String xmlData, SecurityContext securityContext) throws ItemNotFoundException,
        FileNotFoundException, InvalidContextException, InvalidStatusException, LockingException,
        NotPublishedException, MissingLicenceException, ComponentNotFoundException, MissingContentException,
        AuthenticationException, AuthorizationException, InvalidXmlException, MissingMethodParameterException,
        InvalidContentException, SystemException, OptimisticLockingException, AlreadyExistsException,
        ReadonlyViolationException, ReferencedResourceNotFoundException, RelationPredicateNotFoundException,
        ReadonlyVersionException, MissingAttributeValueException, MissingMdRecordException;

    String update(String id, String xmlData, String authHandle, Boolean restAccess) throws ItemNotFoundException,
        FileNotFoundException, InvalidContextException, InvalidStatusException, LockingException,
        NotPublishedException, MissingLicenceException, ComponentNotFoundException, MissingContentException,
        AuthenticationException, AuthorizationException, InvalidXmlException, MissingMethodParameterException,
        InvalidContentException, SystemException, OptimisticLockingException, AlreadyExistsException,
        ReadonlyViolationException, ReferencedResourceNotFoundException, RelationPredicateNotFoundException,
        ReadonlyVersionException, MissingAttributeValueException, MissingMdRecordException;

    String createComponent(String id, String xmlData, SecurityContext securityContext) throws MissingContentException,
        ItemNotFoundException, ComponentNotFoundException, LockingException, MissingElementValueException,
        AuthenticationException, AuthorizationException, InvalidStatusException, MissingMethodParameterException,
        FileNotFoundException, InvalidXmlException, InvalidContentException, SystemException,
        ReadonlyViolationException, OptimisticLockingException, MissingAttributeValueException;

    String createComponent(String id, String xmlData, String authHandle, Boolean restAccess)
        throws MissingContentException, ItemNotFoundException, ComponentNotFoundException, LockingException,
        MissingElementValueException, AuthenticationException, AuthorizationException, InvalidStatusException,
        MissingMethodParameterException, FileNotFoundException, InvalidXmlException, InvalidContentException,
        SystemException, ReadonlyViolationException, OptimisticLockingException, MissingAttributeValueException;

    String retrieveComponent(String id, String componentId, SecurityContext securityContext)
        throws ItemNotFoundException, ComponentNotFoundException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, SystemException;

    String retrieveComponent(String id, String componentId, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, ComponentNotFoundException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, SystemException;

    String retrieveComponentMdRecords(String id, String componentId, SecurityContext securityContext)
        throws ItemNotFoundException, ComponentNotFoundException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, SystemException;

    String retrieveComponentMdRecords(String id, String componentId, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, ComponentNotFoundException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, SystemException;

    String retrieveComponentMdRecord(String id, String componentId, String mdRecordId, SecurityContext securityContext)
        throws ItemNotFoundException, AuthenticationException, AuthorizationException, ComponentNotFoundException,
        MdRecordNotFoundException, MissingMethodParameterException, SystemException;

    String retrieveComponentMdRecord(
        String id, String componentId, String mdRecordId, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, AuthenticationException, AuthorizationException, ComponentNotFoundException,
        MdRecordNotFoundException, MissingMethodParameterException, SystemException;

    String updateComponent(String id, String componentId, String xmlData, SecurityContext securityContext)
        throws ItemNotFoundException, ComponentNotFoundException, LockingException, FileNotFoundException,
        MissingAttributeValueException, AuthenticationException, AuthorizationException, InvalidStatusException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidXmlException,
        ReadonlyViolationException, MissingContentException, InvalidContentException, ReadonlyVersionException;

    String updateComponent(String id, String componentId, String xmlData, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, ComponentNotFoundException, LockingException, FileNotFoundException,
        MissingAttributeValueException, AuthenticationException, AuthorizationException, InvalidStatusException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidXmlException,
        ReadonlyViolationException, MissingContentException, InvalidContentException, ReadonlyVersionException;

    String retrieveComponents(String id, SecurityContext securityContext) throws ItemNotFoundException,
        AuthenticationException, AuthorizationException, ComponentNotFoundException, MissingMethodParameterException,
        SystemException;

    String retrieveComponents(String id, String authHandle, Boolean restAccess) throws ItemNotFoundException,
        AuthenticationException, AuthorizationException, ComponentNotFoundException, MissingMethodParameterException,
        SystemException;

    String retrieveComponentProperties(String id, String componentId, SecurityContext securityContext)
        throws ItemNotFoundException, ComponentNotFoundException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, SystemException;

    String retrieveComponentProperties(String id, String componentId, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, ComponentNotFoundException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, SystemException;

    String createMetadataRecord(String id, String xmlData, SecurityContext securityContext)
        throws ItemNotFoundException, ComponentNotFoundException, XmlSchemaNotFoundException, LockingException,
        MissingAttributeValueException, AuthenticationException, AuthorizationException, InvalidStatusException,
        MissingMethodParameterException, SystemException, InvalidXmlException;

    String createMetadataRecord(String id, String xmlData, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, ComponentNotFoundException, XmlSchemaNotFoundException, LockingException,
        MissingAttributeValueException, AuthenticationException, AuthorizationException, InvalidStatusException,
        MissingMethodParameterException, SystemException, InvalidXmlException;

    String createMdRecord(String id, String xmlData, SecurityContext securityContext) throws ItemNotFoundException,
        SystemException, InvalidXmlException, LockingException, MissingAttributeValueException, InvalidStatusException,
        ComponentNotFoundException, MissingMethodParameterException, AuthorizationException, AuthenticationException;

    String createMdRecord(String id, String xmlData, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, SystemException, InvalidXmlException, LockingException,
        MissingAttributeValueException, InvalidStatusException, ComponentNotFoundException,
        MissingMethodParameterException, AuthorizationException, AuthenticationException;

    EscidocBinaryContent retrieveContent(String id, String contentId, SecurityContext securityContext)
        throws ItemNotFoundException, AuthenticationException, AuthorizationException, MissingMethodParameterException,
        SystemException, InvalidStatusException, ResourceNotFoundException;

    EscidocBinaryContent retrieveContent(String id, String contentId, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, AuthenticationException, AuthorizationException, MissingMethodParameterException,
        SystemException, InvalidStatusException, ResourceNotFoundException;

    EscidocBinaryContent retrieveContentStreamContent(String itemId, String name, SecurityContext securityContext)
        throws AuthenticationException, AuthorizationException, MissingMethodParameterException, ItemNotFoundException,
        SystemException, ContentStreamNotFoundException;

    EscidocBinaryContent retrieveContentStreamContent(String itemId, String name, String authHandle, Boolean restAccess)
        throws AuthenticationException, AuthorizationException, MissingMethodParameterException, ItemNotFoundException,
        SystemException, ContentStreamNotFoundException;

    EscidocBinaryContent retrieveContent(
        String id, String contentId, String transformer, String param, SecurityContext securityContext)
        throws ItemNotFoundException, ComponentNotFoundException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, SystemException, InvalidStatusException;

    EscidocBinaryContent retrieveContent(
        String id, String contentId, String transformer, String param, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, ComponentNotFoundException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, SystemException, InvalidStatusException;

    EscidocServiceRedirectInterface redirectContentService(
        String id, String contentId, String transformer, String clientService, SecurityContext securityContext)
        throws ItemNotFoundException, ComponentNotFoundException, AuthorizationException,
        MissingMethodParameterException, SystemException, InvalidStatusException;

    EscidocServiceRedirectInterface redirectContentService(
        String id, String contentId, String transformer, String clientService, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, ComponentNotFoundException, AuthorizationException,
        MissingMethodParameterException, SystemException, InvalidStatusException;

    String retrieveMdRecord(String id, String mdRecordId, SecurityContext securityContext)
        throws ItemNotFoundException, MdRecordNotFoundException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, SystemException;

    String retrieveMdRecord(String id, String mdRecordId, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, MdRecordNotFoundException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, SystemException;

    String retrieveMdRecordContent(String id, String mdRecordId, SecurityContext securityContext)
        throws ItemNotFoundException, MdRecordNotFoundException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, SystemException;

    String retrieveMdRecordContent(String id, String mdRecordId, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, MdRecordNotFoundException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, SystemException;

    String retrieveDcRecordContent(String id, SecurityContext securityContext) throws ItemNotFoundException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, MdRecordNotFoundException,
        SystemException;

    String retrieveDcRecordContent(String id, String authHandle, Boolean restAccess) throws ItemNotFoundException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, MdRecordNotFoundException,
        SystemException;

    String updateMdRecord(String id, String mdRecordId, String xmlData, SecurityContext securityContext)
        throws ItemNotFoundException, XmlSchemaNotFoundException, LockingException, InvalidContentException,
        MdRecordNotFoundException, AuthenticationException, AuthorizationException, InvalidStatusException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidXmlException,
        ReadonlyViolationException, ReadonlyVersionException;

    String updateMdRecord(String id, String mdRecordId, String xmlData, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, XmlSchemaNotFoundException, LockingException, InvalidContentException,
        MdRecordNotFoundException, AuthenticationException, AuthorizationException, InvalidStatusException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidXmlException,
        ReadonlyViolationException, ReadonlyVersionException;

    String retrieveMdRecords(String id, SecurityContext securityContext) throws ItemNotFoundException,
        AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException;

    String retrieveMdRecords(String id, String authHandle, Boolean restAccess) throws ItemNotFoundException,
        AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException;

    String retrieveContentStreams(String id, SecurityContext securityContext) throws ItemNotFoundException,
        AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException;

    String retrieveContentStreams(String id, String authHandle, Boolean restAccess) throws ItemNotFoundException,
        AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException;

    String retrieveContentStream(String id, String name, SecurityContext securityContext) throws ItemNotFoundException,
        AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException,
        ContentStreamNotFoundException;

    String retrieveContentStream(String id, String name, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, AuthenticationException, AuthorizationException, MissingMethodParameterException,
        SystemException, ContentStreamNotFoundException;

    String retrieveProperties(String id, SecurityContext securityContext) throws ItemNotFoundException,
        AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException;

    String retrieveProperties(String id, String authHandle, Boolean restAccess) throws ItemNotFoundException,
        AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException;

    String retrieveResources(String id, SecurityContext securityContext) throws ItemNotFoundException,
        AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException;

    String retrieveResources(String id, String authHandle, Boolean restAccess) throws ItemNotFoundException,
        AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException;

    EscidocBinaryContent retrieveResource(
        String id, String resourceName, Map parameters, SecurityContext securityContext) throws ItemNotFoundException,
        AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException,
        OperationNotFoundException;

    EscidocBinaryContent retrieveResource(
        String id, String resourceName, Map parameters, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, AuthenticationException, AuthorizationException, MissingMethodParameterException,
        SystemException, OperationNotFoundException;

    String retrieveVersionHistory(String id, SecurityContext securityContext) throws ItemNotFoundException,
        AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException;

    String retrieveVersionHistory(String id, String authHandle, Boolean restAccess) throws ItemNotFoundException,
        AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException;

    String retrieveParents(String id, SecurityContext securityContext) throws ItemNotFoundException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException;

    String retrieveParents(String id, String authHandle, Boolean restAccess) throws ItemNotFoundException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException;

    String retrieveRelations(String id, SecurityContext securityContext) throws ItemNotFoundException,
        AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException;

    String retrieveRelations(String id, String authHandle, Boolean restAccess) throws ItemNotFoundException,
        AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException;

    String release(String id, String lastModified, SecurityContext securityContext) throws ItemNotFoundException,
        ComponentNotFoundException, LockingException, InvalidStatusException, AuthenticationException,
        AuthorizationException, MissingMethodParameterException, SystemException, OptimisticLockingException,
        ReadonlyViolationException, ReadonlyVersionException, InvalidXmlException;

    String release(String id, String lastModified, String authHandle, Boolean restAccess) throws ItemNotFoundException,
        ComponentNotFoundException, LockingException, InvalidStatusException, AuthenticationException,
        AuthorizationException, MissingMethodParameterException, SystemException, OptimisticLockingException,
        ReadonlyViolationException, ReadonlyVersionException, InvalidXmlException;

    String submit(String id, String lastModified, SecurityContext securityContext) throws ItemNotFoundException,
        ComponentNotFoundException, LockingException, InvalidStatusException, AuthenticationException,
        AuthorizationException, MissingMethodParameterException, SystemException, OptimisticLockingException,
        ReadonlyViolationException, ReadonlyVersionException, InvalidXmlException;

    String submit(String id, String lastModified, String authHandle, Boolean restAccess) throws ItemNotFoundException,
        ComponentNotFoundException, LockingException, InvalidStatusException, AuthenticationException,
        AuthorizationException, MissingMethodParameterException, SystemException, OptimisticLockingException,
        ReadonlyViolationException, ReadonlyVersionException, InvalidXmlException;

    String revise(String id, String lastModified, SecurityContext securityContext) throws AuthenticationException,
        AuthorizationException, ItemNotFoundException, ComponentNotFoundException, LockingException,
        InvalidStatusException, MissingMethodParameterException, SystemException, OptimisticLockingException,
        ReadonlyViolationException, ReadonlyVersionException, InvalidContentException, XmlCorruptedException;

    String revise(String id, String lastModified, String authHandle, Boolean restAccess)
        throws AuthenticationException, AuthorizationException, ItemNotFoundException, ComponentNotFoundException,
        LockingException, InvalidStatusException, MissingMethodParameterException, SystemException,
        OptimisticLockingException, ReadonlyViolationException, ReadonlyVersionException, InvalidContentException,
        XmlCorruptedException;

    String withdraw(String id, String lastModified, SecurityContext securityContext) throws ItemNotFoundException,
        ComponentNotFoundException, NotPublishedException, LockingException, AlreadyWithdrawnException,
        AuthenticationException, AuthorizationException, InvalidStatusException, MissingMethodParameterException,
        SystemException, OptimisticLockingException, ReadonlyViolationException, ReadonlyVersionException,
        InvalidXmlException;

    String withdraw(String id, String lastModified, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, ComponentNotFoundException, NotPublishedException, LockingException,
        AlreadyWithdrawnException, AuthenticationException, AuthorizationException, InvalidStatusException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, ReadonlyViolationException,
        ReadonlyVersionException, InvalidXmlException;

    String lock(String id, String lastModified, SecurityContext securityContext) throws ItemNotFoundException,
        ComponentNotFoundException, LockingException, InvalidContentException, AuthenticationException,
        AuthorizationException, MissingMethodParameterException, SystemException, OptimisticLockingException,
        InvalidXmlException, InvalidStatusException;

    String lock(String id, String lastModified, String authHandle, Boolean restAccess) throws ItemNotFoundException,
        ComponentNotFoundException, LockingException, InvalidContentException, AuthenticationException,
        AuthorizationException, MissingMethodParameterException, SystemException, OptimisticLockingException,
        InvalidXmlException, InvalidStatusException;

    String unlock(String id, String lastModified, SecurityContext securityContext) throws ItemNotFoundException,
        ComponentNotFoundException, LockingException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidXmlException;

    String unlock(String id, String lastModified, String authHandle, Boolean restAccess) throws ItemNotFoundException,
        ComponentNotFoundException, LockingException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidXmlException;

    void deleteComponent(String itemId, String componentId, SecurityContext securityContext)
        throws ItemNotFoundException, ComponentNotFoundException, LockingException, AuthenticationException,
        AuthorizationException, MissingMethodParameterException, SystemException, InvalidStatusException;

    void deleteComponent(String itemId, String componentId, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, ComponentNotFoundException, LockingException, AuthenticationException,
        AuthorizationException, MissingMethodParameterException, SystemException, InvalidStatusException;

    String moveToContext(String id, String taskParam, SecurityContext securityContext) throws ContextNotFoundException,
        InvalidContentException, ItemNotFoundException, LockingException, InvalidStatusException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException;

    String moveToContext(String id, String taskParam, String authHandle, Boolean restAccess)
        throws ContextNotFoundException, InvalidContentException, ItemNotFoundException, LockingException,
        InvalidStatusException, MissingMethodParameterException, AuthenticationException, AuthorizationException,
        SystemException;

    String retrieveItems(Map filter, SecurityContext securityContext) throws SystemException;

    String retrieveItems(Map filter, String authHandle, Boolean restAccess) throws SystemException;

    String assignVersionPid(String id, String taskParam, SecurityContext securityContext) throws ItemNotFoundException,
        ComponentNotFoundException, LockingException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidStatusException,
        XmlCorruptedException, ReadonlyVersionException;

    String assignVersionPid(String id, String taskParam, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, ComponentNotFoundException, LockingException, AuthenticationException,
        AuthorizationException, MissingMethodParameterException, SystemException, OptimisticLockingException,
        InvalidStatusException, XmlCorruptedException, ReadonlyVersionException;

    String assignObjectPid(String id, String taskParam, SecurityContext securityContext) throws ItemNotFoundException,
        ComponentNotFoundException, LockingException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidStatusException,
        XmlCorruptedException;

    String assignObjectPid(String id, String taskParam, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, ComponentNotFoundException, LockingException, AuthenticationException,
        AuthorizationException, MissingMethodParameterException, SystemException, OptimisticLockingException,
        InvalidStatusException, XmlCorruptedException;

    String assignContentPid(String id, String componentId, String taskParam, SecurityContext securityContext)
        throws ItemNotFoundException, LockingException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidStatusException,
        ComponentNotFoundException, XmlCorruptedException, ReadonlyVersionException;

    String assignContentPid(String id, String componentId, String taskParam, String authHandle, Boolean restAccess)
        throws ItemNotFoundException, LockingException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidStatusException,
        ComponentNotFoundException, XmlCorruptedException, ReadonlyVersionException;

    String addContentRelations(String id, String param, SecurityContext securityContext) throws SystemException,
        ItemNotFoundException, ComponentNotFoundException, OptimisticLockingException,
        ReferencedResourceNotFoundException, RelationPredicateNotFoundException, AlreadyExistsException,
        InvalidStatusException, InvalidXmlException, MissingElementValueException, LockingException,
        ReadonlyViolationException, InvalidContentException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, ReadonlyVersionException;

    String addContentRelations(String id, String param, String authHandle, Boolean restAccess) throws SystemException,
        ItemNotFoundException, ComponentNotFoundException, OptimisticLockingException,
        ReferencedResourceNotFoundException, RelationPredicateNotFoundException, AlreadyExistsException,
        InvalidStatusException, InvalidXmlException, MissingElementValueException, LockingException,
        ReadonlyViolationException, InvalidContentException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, ReadonlyVersionException;

    String removeContentRelations(String id, String param, SecurityContext securityContext) throws SystemException,
        ItemNotFoundException, ComponentNotFoundException, OptimisticLockingException, InvalidStatusException,
        MissingElementValueException, InvalidContentException, InvalidXmlException, ContentRelationNotFoundException,
        AlreadyDeletedException, LockingException, ReadonlyViolationException, AuthenticationException,
        AuthorizationException, MissingMethodParameterException, ReadonlyVersionException;

    String removeContentRelations(String id, String param, String authHandle, Boolean restAccess)
        throws SystemException, ItemNotFoundException, ComponentNotFoundException, OptimisticLockingException,
        InvalidStatusException, MissingElementValueException, InvalidContentException, InvalidXmlException,
        ContentRelationNotFoundException, AlreadyDeletedException, LockingException, ReadonlyViolationException,
        AuthenticationException, AuthorizationException, MissingMethodParameterException, ReadonlyVersionException;

}
