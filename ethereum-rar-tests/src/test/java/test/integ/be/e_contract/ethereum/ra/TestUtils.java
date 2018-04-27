/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018 e-Contract.be BVBA.
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
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

public class TestUtils {

    public static EnterpriseArchive createBasicEAR() throws Exception {
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "test.ear");

        File rarFile = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("be.e-contract.ethereum-resource-adapter:ethereum-rar:rar:?")
                .withoutTransitivity().asSingleFile();
        ear.addAsModule(rarFile, "ethereum-rar.rar");

        File[] web3jDependencies = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("org.web3j:core")
                .withTransitivity().asFile();
        ear.addAsLibraries(web3jDependencies);

        File[] commonsLangDependencies = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("commons-lang:commons-lang")
                .withTransitivity().asFile();
        ear.addAsLibraries(commonsLangDependencies);

        return ear;
    }
}
