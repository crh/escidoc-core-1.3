package de.escidoc.core.aa.business;

import de.escidoc.core.aa.business.authorisation.FinderModuleHelper;
import de.escidoc.core.aa.business.persistence.EscidocRole;
import de.escidoc.core.aa.business.persistence.EscidocRoleDaoInterface;
import de.escidoc.core.aa.business.persistence.UserAccount;
import de.escidoc.core.aa.business.persistence.UserAccountDaoInterface;
import de.escidoc.core.aa.business.persistence.UserGroup;
import de.escidoc.core.aa.business.persistence.UserGroupDaoInterface;
import de.escidoc.core.common.business.fedora.TripleStoreUtility;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.notfound.ScopeNotFoundException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.system.IntegritySystemException;
import de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
import de.escidoc.core.common.util.string.StringUtility;
import de.escidoc.core.common.util.xml.XmlUtility;
import de.escidoc.core.sm.service.interfaces.ScopeHandlerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves objectType and objectTitle for given objectId.
 *
 * @author Michael Hoppe
 */
public class ObjectAttributeResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectAttributeResolver.class);

    private UserAccountDaoInterface userAccountDao;

    private UserGroupDaoInterface userGroupDao;

    private EscidocRoleDaoInterface roleDao;

    private ScopeHandlerInterface scopeHandler;

    private TripleStoreUtility tsu;

    public static final String ATTR_OBJECT_TYPE = "objectType";

    public static final String ATTR_OBJECT_TITLE = "objectTitle";

    /**
     * Try to retrieve ObjectType and objectTitle for given id.
     *
     * @param objectId The objectId.
     * @return HashMap with objectType and objectTitle
     * @throws MissingMethodParameterException
     *                                 e
     * @throws SystemException         e
     * @throws AuthorizationException  e
     * @throws AuthenticationException e
     * @throws de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException
     * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
     * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
     */
    public Map<String, String> resolveObjectAttributes(final String objectId) throws MissingMethodParameterException,
        SystemException, AuthorizationException, AuthenticationException, IntegritySystemException,
        TripleStoreSystemException, SqlDatabaseSystemException {
        return resolveObjectAttributes(objectId, false);
    }

    /**
     * Try to retrieve ObjectType for given id.
     *
     * @param objectId The objectId.
     * @return String objectType
     * @throws MissingMethodParameterException
     *                                 e
     * @throws SystemException         e
     * @throws AuthorizationException  e
     * @throws AuthenticationException e
     * @throws de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException
     * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
     * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
     */
    public String resolveObjectType(final String objectId) throws MissingMethodParameterException, SystemException,
        AuthorizationException, AuthenticationException, IntegritySystemException, TripleStoreSystemException,
        SqlDatabaseSystemException {
        final Map<String, String> objectAttributes = resolveObjectAttributes(objectId, true);
        return objectAttributes != null ? objectAttributes.get(ATTR_OBJECT_TYPE) : null;
    }

    /**
     * Try to retrieve ObjectType and objectTitle for given id.
     *
     * @param objectId The objectId.
     * @param typeOnly set to true if only objectType should get returned.
     * @return HashMap with objectType and objectTitle
     * @throws MissingMethodParameterException
     *                                 e
     * @throws SystemException         e
     * @throws AuthorizationException  e
     * @throws AuthenticationException e
     * @throws de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException
     * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
     * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
     */
    private Map<String, String> resolveObjectAttributes(final String objectId, final boolean typeOnly)
        throws MissingMethodParameterException, SystemException, AuthorizationException, AuthenticationException,
        IntegritySystemException, TripleStoreSystemException, SqlDatabaseSystemException {

        // try getting attributes from Triple-Store
        Map<String, String> objectAttributes = getObjectFromTripleStore(objectId, typeOnly);

        // try getting attributes from user-account
        if (objectAttributes == null) {
            objectAttributes = getObjectFromUserAccount(objectId, typeOnly);
        }

        // try getting attributes from user-group
        if (objectAttributes == null) {
            objectAttributes = getObjectFromUserGroup(objectId, typeOnly);
        }

        // try getting attributes from role
        if (objectAttributes == null) {
            objectAttributes = getObjectFromRole(objectId, typeOnly);
        }

        // try getting attributes from grant
        if (objectAttributes == null) {
            objectAttributes = getObjectFromGrant(objectId, typeOnly);
        }

        // try getting attributes from Statistic-scopes
        if (objectAttributes == null) {
            objectAttributes = getObjectFromScopeHandler(objectId, typeOnly);
        }

        return objectAttributes;

    }

    /**
     * Try to retrieve ObjectType for given id from tripleStore. Also retreive title if objectType was resolvable
     *
     * @param objectId The objectId.
     * @param typeOnly set to true if only objectType should get returned.
     * @return HashMap with objectType and objectTitle
     * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
     * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
     */
    private Map<String, String> getObjectFromTripleStore(final String objectId, final boolean typeOnly)
        throws TripleStoreSystemException, IntegritySystemException {
        final Map<String, String> result = new HashMap<String, String>();
        final String objectType = tsu.getObjectType(objectId);
        if (objectType != null) {
            // object information is stored in the triple store, title
            // can be get from information stored in triple store
            result.put(ATTR_OBJECT_TYPE, FinderModuleHelper.convertObjectType(objectType, true));
            if (!typeOnly) {
                result.put(ATTR_OBJECT_TITLE, tsu.getTitle(objectId));
            }
        }
        else {
            return null;
        }

        return result;
    }

    /**
     * Try to retrieve ObjectType for given id from scopeHandler. Also retrieve title if objectType was resolvable
     *
     * @param objectId The objectId.
     * @param typeOnly set to true if only objectType should get returned.
     * @return HashMap with objectType and objectTitle
     * @throws MissingMethodParameterException
     *                                 e
     * @throws SystemException         e
     * @throws AuthorizationException  e
     * @throws AuthenticationException e
     */
    private Map<String, String> getObjectFromScopeHandler(final String objectId, final boolean typeOnly)
        throws MissingMethodParameterException, SystemException, AuthorizationException, AuthenticationException {
        final Map<String, String> result = new HashMap<String, String>();
        try {
            scopeHandler.retrieve(objectId);
            // we got a scope for the id, therefore objectType is set to
            // scope
            result.put(ATTR_OBJECT_TYPE, XmlUtility.NAME_SCOPE);
            if (!typeOnly) {
                result.put(ATTR_OBJECT_TITLE, StringUtility.convertToUpperCaseLetterFormat(XmlUtility.NAME_SCOPE) + " "
                    + objectId);
            }
        }
        catch (final ScopeNotFoundException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Error on getting object.");
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error on getting object.", e);
            }
            return null;
        }

        return result;
    }

    /**
     * Try to retrieve ObjectType for given id from userAccountHandler. Also retrieve title if objectType was
     * resolvable
     *
     * @param objectId The objectId.
     * @param typeOnly set to true if only objectType should get returned.
     * @return HashMap with objectType and objectTitle
     * @throws de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException
     */
    private Map<String, String> getObjectFromUserAccount(final String objectId, final boolean typeOnly)
        throws SqlDatabaseSystemException {
        final Map<String, String> result = new HashMap<String, String>();
        if (typeOnly) {
            if (userAccountDao.userAccountExists(objectId)) {
                result.put(ATTR_OBJECT_TYPE, XmlUtility.NAME_USER_ACCOUNT);
            }
            else {
                return null;
            }
        }
        else {
            final UserAccount userAccount = userAccountDao.retrieveUserAccount(objectId);
            if (userAccount != null) {
                // we got a user account for the id,
                // therefore objectType is set to
                // user-account
                result.put(ATTR_OBJECT_TYPE, XmlUtility.NAME_USER_ACCOUNT);
                result.put(ATTR_OBJECT_TITLE, userAccount.getName());
            }
            else {
                return null;
            }
        }

        return result;
    }

    /**
     * Try to retrieve ObjectType for given id from userGroupHandler. Also retrieve title if objectType was resolvable
     *
     * @param objectId The objectId.
     * @param typeOnly set to true if only objectType should get returned.
     * @return HashMap with objectType and objectTitle
     * @throws de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException
     */
    private Map<String, String> getObjectFromUserGroup(final String objectId, final boolean typeOnly)
        throws SqlDatabaseSystemException {
        final Map<String, String> result = new HashMap<String, String>();
        if (typeOnly) {
            if (userGroupDao.userGroupExists(objectId)) {
                result.put(ATTR_OBJECT_TYPE, XmlUtility.NAME_USER_GROUP);
            }
            else {
                return null;
            }
        }
        else {
            final UserGroup userGroup = userGroupDao.retrieveUserGroup(objectId);
            if (userGroup != null) {
                // we got a user-group for the id,
                // therefore objectType is set to
                // user-group
                result.put(ATTR_OBJECT_TYPE, XmlUtility.NAME_USER_GROUP);
                result.put(ATTR_OBJECT_TITLE, userGroup.getName());
            }
            else {
                return null;
            }
        }

        return result;
    }

    /**
     * Resolve the object in case of resource is a role.
     *
     * @param objectId The objectId.
     * @param typeOnly set to true if only objectType should get returned.
     * @return HashMap with objectType and objectTitle
     * @throws de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException
     */
    private Map<String, String> getObjectFromRole(final String objectId, final boolean typeOnly)
        throws SqlDatabaseSystemException {
        final Map<String, String> result = new HashMap<String, String>();
        if (typeOnly) {
            if (roleDao.roleExists(objectId)) {
                result.put(ATTR_OBJECT_TYPE, XmlUtility.NAME_ROLE);
            }
            else {
                return null;
            }
        }
        else {
            final EscidocRole role = roleDao.retrieveRole(objectId);
            if (role != null) {
                // we got a role for the id,
                // therefore objectType is set to
                // role
                result.put(ATTR_OBJECT_TYPE, XmlUtility.NAME_ROLE);
                result.put(ATTR_OBJECT_TITLE, role.getRoleName());
            }
            else {
                return null;
            }
        }

        return result;
    }

    /**
     * Resolve the object in case of resource is a grant.
     *
     * @param objectId The objectId.
     * @param typeOnly set to true if only objectType should get returned.
     * @return HashMap with objectType and objectTitle
     * @throws de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException
     */
    private Map<String, String> getObjectFromGrant(final String objectId, final boolean typeOnly)
        throws SqlDatabaseSystemException {
        final Map<String, String> result = new HashMap<String, String>();
        if (userAccountDao.grantExists(objectId)) {
            result.put(ATTR_OBJECT_TYPE, XmlUtility.NAME_GRANT);
            if (!typeOnly) {
                result.put(ATTR_OBJECT_TITLE, StringUtility.convertToUpperCaseLetterFormat(XmlUtility.NAME_GRANT) + " "
                    + objectId);
            }
        }
        else {
            return null;
        }

        return result;
    }

    /**
     * Injects the scope handler bean.
     *
     * @param scopeHandler The {@link ScopeHandlerInterface} implementation.
     */
    public void setScopeHandler(final ScopeHandlerInterface scopeHandler) {
        this.scopeHandler = scopeHandler;
    }

    /**
     * Injects the triple store utility bean.
     *
     * @param tsu The {@link TripleStoreUtility}.
     */
    public void setTsu(final TripleStoreUtility tsu) {
        this.tsu = tsu;
    }

    /**
     * Injects the user account data access object.
     *
     * @param userAccountDao The data access object.
     */
    public void setUserAccountDao(final UserAccountDaoInterface userAccountDao) {
        this.userAccountDao = userAccountDao;
    }

    /**
     * Injects the user group data access object.
     *
     * @param userGroupDao The data access object.
     */
    public void setUserGroupDao(final UserGroupDaoInterface userGroupDao) {
        this.userGroupDao = userGroupDao;
    }

    /**
     * Injects the role dao.
     *
     * @param roleDao the {@link EscidocRoleDaoInterface} to inject.
     */
    public void setRoleDao(final EscidocRoleDaoInterface roleDao) {
        this.roleDao = roleDao;
    }

}
