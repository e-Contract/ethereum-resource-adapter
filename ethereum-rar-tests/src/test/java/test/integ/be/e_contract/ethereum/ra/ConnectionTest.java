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

import javax.inject.Inject;
import javax.resource.ResourceException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Integration Test to verify basic Ethereum connection.
 *
 * @author Frank Cornelis
 */
@ExtendWith(ArquillianExtension.class)
public class ConnectionTest {

    @Deployment
    public static EnterpriseArchive createDeployment() throws Exception {
        EnterpriseArchive ear = TestUtils.createBasicEAR();

        JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "ejb.jar")
                .addClass(ConnectionBean.class)
                .addAsManifestResource("connection-jboss-ejb3.xml", "jboss-ejb3.xml")
                .addAsManifestResource("connection-ejb-jar.xml", "ejb-jar.xml");
        ear.addAsModule(ejbJar);

        JavaArchive libJar = ShrinkWrap.create(JavaArchive.class, "lib.jar")
                .addClasses(ConnectionTest.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        ear.addAsLibraries(libJar);

        return ear;
    }

    @Inject
    private ConnectionBean connectionBean;

    @Test
    public void testConnection() throws Exception {
        this.connectionBean.connection();
    }

    @Test
    public void testConnectionError() throws Exception {
        try {
            this.connectionBean.connectionError();
            fail();
        } catch (ResourceException e) {
            // expected
        }
    }
}
