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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration Test to check EAR based testing.
 *
 * @author Frank Cornelis
 */
@ExtendWith(ArquillianExtension.class)
public class EARTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EARTest.class);

    @Deployment
    public static EnterpriseArchive createDeployment() throws Exception {
        JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "ejb.jar")
                .addClass(HelloBean.class);
        JavaArchive libJar = ShrinkWrap.create(JavaArchive.class, "lib.jar")
                .addClasses(EARTest.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                .addAsModule(ejbJar).addAsLibraries(libJar);
        LOGGER.debug("EAR: {}", ear.toString(true));
        return ear;
    }

    @Inject
    private HelloBean helloBean;

    @Test
    public void testLoading() throws Exception {
        String message = "hello world";
        String result = this.helloBean.hello(message);
        assertEquals(message, result);
    }
}
