/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2023-2024 e-Contract.be BV.
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

import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(ArquillianExtension.class)
public class AdminTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminTest.class);

    @Deployment
    public static EnterpriseArchive createDeployment() throws Exception {
        EnterpriseArchive ear = TestUtils.createBasicEAR();

        JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "ejb.jar")
                .addClass(AdminBean.class)
                .addAsManifestResource("admin-jboss-ejb3.xml", "jboss-ejb3.xml")
                .addAsManifestResource("admin-ejb-jar.xml", "ejb-jar.xml");
        ear.addAsModule(ejbJar);

        JavaArchive libJar = ShrinkWrap.create(JavaArchive.class, "lib.jar")
                .addClasses(AdminTest.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        ear.addAsLibraries(libJar);

        return ear;
    }

    @Inject
    private AdminBean adminBean;

    @Test
    public void testEthereumAdmin() throws Exception {
        String implementationVersion = this.adminBean.getImplementationVersion();
        assertNotNull(implementationVersion);
        LOGGER.debug("implementation version: {}", implementationVersion);
    }
}
