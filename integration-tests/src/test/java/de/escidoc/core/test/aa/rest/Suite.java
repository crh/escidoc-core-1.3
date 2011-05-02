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
package de.escidoc.core.test.aa.rest;

import org.junit.runner.RunWith;

/**
 * The AA test suite (REST).
 *
 * @author Torsten Tetteroo
 */
@RunWith(org.junit.runners.Suite.class)
@org.junit.runners.Suite.SuiteClasses( { AdministratorRestTest.class, DefaultPoliciesRestTest.class,
    DepositorRestTest.class, ModeratorRestTest.class, PrivilegedViewerRestTest.class, StatisticEditorRestTest.class,
    StatisticReaderRestTest.class, CollaboratorRestTest.class, CollaboratorModifierRestTest.class,
    CollaboratorModifierAddRemoveMembersRestTest.class, CollaboratorModifierAddRemoveAnyMembersRestTest.class,
    CollaboratorModifierUpdateDirectMembersRestTest.class, CollaboratorModifierUpdateAnyMembersRestTest.class,
    ContentRelationManagerRestTest.class, ContentRelationModifierRestTest.class, AudienceRestTest.class,
    UserGroupAdminRestTest.class, UserGroupInspectorRestTest.class, UserAccountAdminRestTest.class,
    UserAccountInspectorRestTest.class, OrgUnitAdminRestTest.class, ContextAdminRestTest.class,
    ContextModifierRestTest.class,

    PdpRestTest.class, RoleRestTest.class,

    UserAccountGrantRestTest.class, UserAccountRestTest.class, UserAttributeRestTest.class,
    UserManagementWrapperRestTest.class, UserPreferenceRestTest.class, UserGroupRestTest.class,
    UserGroupGrantRestTest.class,

    GrantFilterRestTest.class })
public class Suite {

}
