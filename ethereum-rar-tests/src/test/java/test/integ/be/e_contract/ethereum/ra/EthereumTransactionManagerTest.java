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

import java.io.File;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ArquillianExtension.class)
public class EthereumTransactionManagerTest {

    @Deployment
    public static EnterpriseArchive createDeployment() throws Exception {
        EnterpriseArchive ear = TestUtils.createBasicEAR();

        JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "ejb.jar")
                .addClasses(EthereumTransactionManagerMDB.class, EthereumTransactionManagerTestBean.class);
        ear.addAsModule(ejbJar);

        File utilsFile = Maven.configureResolver().workOffline().loadPomFromFile("pom.xml")
                .resolve("be.e-contract.ethereum-resource-adapter:ethereum-utils")
                .withoutTransitivity().asSingleFile();
        ear.addAsLibrary(utilsFile);

        JavaArchive libJar = ShrinkWrap.create(JavaArchive.class, "lib.jar")
                .addClasses(EthereumTransactionManagerTest.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        ear.addAsLibraries(libJar);

        return ear;
    }

    @Inject
    private EthereumTransactionManagerTestBean bean;

    @Test
    public void testEthereumTransactionManager() throws Exception {
        this.bean.run();
    }
}
