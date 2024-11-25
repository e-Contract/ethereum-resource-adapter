/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018-2024 e-Contract.be BV.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see 
 * http://www.gnu.org/licenses/.
 */
package test.integ.be.e_contract.ethereum.ra;

import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShrinkWrapTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShrinkWrapTest.class);

    @Test
    public void testEAR() throws Exception {
        EnterpriseArchive ear = TestUtils.createBasicEAR();
        LOGGER.debug("EAR: {}", ear.toString(true));
    }

    @Test
    public void testEARWithOracles() throws Exception {
        EnterpriseArchive ear = TestUtils.createEARWithOracles();
        LOGGER.debug("EAR: {}", ear.toString(true));
    }
}
