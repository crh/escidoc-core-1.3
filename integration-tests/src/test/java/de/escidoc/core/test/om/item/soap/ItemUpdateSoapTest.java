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
package de.escidoc.core.test.om.item.soap;

import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
import de.escidoc.core.test.EscidocRestSoapTestBase;
import de.escidoc.core.test.common.client.servlet.Constants;
import de.escidoc.core.test.om.item.ItemTestBase;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Item tests with SOAP transport.
 *
 * @author Michael Schneider
 */
public class ItemUpdateSoapTest extends ItemTestBase {

    /**
     * Constructor.
     */
    public ItemUpdateSoapTest() {
        super(Constants.TRANSPORT_SOAP);
    }

    /**
     * No ItemID provided. Only SOAP because with REST an update without id is the same as a create request.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testOM_UCI_9() throws Exception {
        try {
            update(null, create(EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_ITEM_PATH + "/"
                + getTransport(false), "escidoc_item_198_for_create.xml")));
        }
        catch (final MissingMethodParameterException e) {
            return;
        }
        fail("Not expected exception");
    }
}
