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

import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import static org.junit.Assert.assertEquals;

/**
 * Simple Integration Test just to check whether Arquillian runtime is
 * functional.
 *
 * @author Frank Cornelis
 */
@RunWith(Arquillian.class)
public class ArquillianTest {

    @Inject
    private HelloBean helloBean;

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(HelloBean.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testArquillian() throws Exception {
        String message = "hello world";
        String result = this.helloBean.hello(message);
        assertEquals(message, result);
    }
}
