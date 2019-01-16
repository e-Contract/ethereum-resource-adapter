/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018-2019 e-Contract.be BVBA.
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
package be.e_contract.ethereum.ra;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumManagedConnectionMetaData implements ManagedConnectionMetaData {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumManagedConnectionMetaData.class);

    public static final String EIS_PRODUCT_NAME = "Ethereum";

    public EthereumManagedConnectionMetaData() {
        LOGGER.debug("constructor");
    }

    @Override
    public String getEISProductName() throws ResourceException {
        LOGGER.debug("getEISProductName");
        return EIS_PRODUCT_NAME;
    }

    @Override
    public String getEISProductVersion() throws ResourceException {
        LOGGER.debug("getEISProductVersion");
        return Version.getImplementationVersion();
    }

    @Override
    public int getMaxConnections() throws ResourceException {
        LOGGER.debug("getMaxConnections");
        return 0;
    }

    @Override
    public String getUserName() throws ResourceException {
        LOGGER.debug("getUserName");
        throw new ResourceException();
    }
}
