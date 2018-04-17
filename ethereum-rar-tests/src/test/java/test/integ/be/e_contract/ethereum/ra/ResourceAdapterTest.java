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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration Test to verify whether we can load the RAR within an EAR.
 *
 * @author Frank Cornelis
 */
@RunWith(Arquillian.class)
public class ResourceAdapterTest {

    @Deployment
    public static EnterpriseArchive createDeployment() throws Exception {
        File rarFile = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("be.e-contract.ethereum-resource-adapter:ethereum-rar:rar:1.0.0-SNAPSHOT")
                .withoutTransitivity().asSingleFile();
        ResourceAdapterArchive rar = ShrinkWrap.createFromZipFile(ResourceAdapterArchive.class, rarFile);
        JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "ejb.jar")
                .addClass(HelloBean.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        JavaArchive libJar = ShrinkWrap.create(JavaArchive.class, "lib.jar")
                .addClasses(ResourceAdapterTest.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        return ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                .addAsModule(ejbJar).addAsModule(rar).addAsLibraries(libJar);
    }

    @Test
    public void testLoading() throws Exception {

    }
}
