package de.escidoc.core.om.ejb.interfaces;

import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.notfound.ContentRelationNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.MdRecordNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.ReferencedResourceNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.RelationPredicateNotFoundException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.application.violated.LockingException;
import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
import de.escidoc.core.common.exceptions.application.violated.PidAlreadyAssignedException;
import de.escidoc.core.common.exceptions.system.SystemException;
import org.springframework.security.context.SecurityContext;

import javax.ejb.EJBLocalObject;
import java.util.Map;

/**
 * Local interface for ContentRelationHandler.
 */
public interface ContentRelationHandlerLocal extends EJBLocalObject {

    String create(String xmlData, SecurityContext securityContext) throws AuthenticationException,
        AuthorizationException, MissingAttributeValueException, MissingMethodParameterException, InvalidXmlException,
        InvalidContentException, ReferencedResourceNotFoundException, RelationPredicateNotFoundException,
        SystemException;

    String create(String xmlData, String authHandle, Boolean restAccess) throws AuthenticationException,
        AuthorizationException, MissingAttributeValueException, MissingMethodParameterException, InvalidXmlException,
        InvalidContentException, ReferencedResourceNotFoundException, RelationPredicateNotFoundException,
        SystemException;

    void delete(String id, SecurityContext securityContext) throws AuthenticationException, AuthorizationException,
        ContentRelationNotFoundException, SystemException, LockingException;

    void delete(String id, String authHandle, Boolean restAccess) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, SystemException, LockingException;

    String lock(String id, String param, SecurityContext securityContext) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, LockingException, InvalidContentException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidXmlException,
        InvalidStatusException;

    String lock(String id, String param, String authHandle, Boolean restAccess) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, LockingException, InvalidContentException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidXmlException,
        InvalidStatusException;

    String unlock(String id, String param, SecurityContext securityContext) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, LockingException, MissingMethodParameterException,
        SystemException, OptimisticLockingException, InvalidXmlException, InvalidContentException,
        InvalidStatusException;

    String unlock(String id, String param, String authHandle, Boolean restAccess) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, LockingException, MissingMethodParameterException,
        SystemException, OptimisticLockingException, InvalidXmlException, InvalidContentException,
        InvalidStatusException;

    String submit(String id, String param, SecurityContext securityContext) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, LockingException, InvalidStatusException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidXmlException,
        InvalidContentException;

    String submit(String id, String param, String authHandle, Boolean restAccess) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, LockingException, InvalidStatusException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidXmlException,
        InvalidContentException;

    String release(String id, String param, SecurityContext securityContext) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, LockingException, InvalidStatusException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidXmlException,
        InvalidContentException;

    String release(String id, String param, String authHandle, Boolean restAccess) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, LockingException, InvalidStatusException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidXmlException,
        InvalidContentException;

    String revise(String id, String param, SecurityContext securityContext) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, LockingException, InvalidStatusException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, XmlCorruptedException,
        InvalidContentException;

    String revise(String id, String param, String authHandle, Boolean restAccess) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, LockingException, InvalidStatusException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, XmlCorruptedException,
        InvalidContentException;

    String retrieve(String id, SecurityContext securityContext) throws AuthenticationException, AuthorizationException,
        ContentRelationNotFoundException, SystemException;

    String retrieve(String id, String authHandle, Boolean restAccess) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, SystemException;

    String retrieveContentRelations(Map parameterMap, SecurityContext securityContext)
        throws InvalidSearchQueryException, SystemException;

    String retrieveContentRelations(Map parameterMap, String authHandle, Boolean restAccess)
        throws InvalidSearchQueryException, SystemException;

    String retrieveProperties(String id, SecurityContext securityContext) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, SystemException;

    String retrieveProperties(String id, String authHandle, Boolean restAccess) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, SystemException;

    String update(String id, String xmlData, SecurityContext securityContext) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, OptimisticLockingException, InvalidContentException,
        InvalidStatusException, LockingException, MissingAttributeValueException, SystemException, InvalidXmlException,
        ReferencedResourceNotFoundException, RelationPredicateNotFoundException, MissingMethodParameterException;

    String update(String id, String xmlData, String authHandle, Boolean restAccess) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, OptimisticLockingException, InvalidContentException,
        InvalidStatusException, LockingException, MissingAttributeValueException, SystemException, InvalidXmlException,
        ReferencedResourceNotFoundException, RelationPredicateNotFoundException, MissingMethodParameterException;

    String assignObjectPid(String id, String taskParam, SecurityContext securityContext)
        throws AuthenticationException, AuthorizationException, ContentRelationNotFoundException, LockingException,
        MissingMethodParameterException, OptimisticLockingException, InvalidXmlException, SystemException,
        PidAlreadyAssignedException;

    String assignObjectPid(String id, String taskParam, String authHandle, Boolean restAccess)
        throws AuthenticationException, AuthorizationException, ContentRelationNotFoundException, LockingException,
        MissingMethodParameterException, OptimisticLockingException, InvalidXmlException, SystemException,
        PidAlreadyAssignedException;

    String retrieveMdRecords(String id, SecurityContext securityContext) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, SystemException;

    String retrieveMdRecords(String id, String authHandle, Boolean restAccess) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, SystemException;

    String retrieveRegisteredPredicates(SecurityContext securityContext) throws InvalidContentException,
        InvalidXmlException, SystemException;

    String retrieveRegisteredPredicates(String authHandle, Boolean restAccess) throws InvalidContentException,
        InvalidXmlException, SystemException;

    String retrieveMdRecord(String id, String name, SecurityContext securityContext) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, MdRecordNotFoundException, SystemException;

    String retrieveMdRecord(String id, String name, String authHandle, Boolean restAccess)
        throws AuthenticationException, AuthorizationException, ContentRelationNotFoundException,
        MdRecordNotFoundException, SystemException;

    String retrieveResources(String id, SecurityContext securityContext) throws ContentRelationNotFoundException,
        AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException;

    String retrieveResources(String id, String authHandle, Boolean restAccess) throws ContentRelationNotFoundException,
        AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException;

}
