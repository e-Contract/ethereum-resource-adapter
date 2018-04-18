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
package be.e_contract.ethereum.rar.demo.model;

import java.security.Security;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
public class BouncyCastleLifecycleBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(BouncyCastleLifecycleBean.class);

    private boolean managed;

    @PostConstruct
    public void postConstruct() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            LOGGER.debug("we manage BouncyCastle");
            Security.addProvider(new BouncyCastleProvider());
            this.managed = true;
        }
    }

    @PreDestroy
    public void preDestroy() {
        if (this.managed) {
            LOGGER.debug("we unregister BouncyCastle");
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }
}
