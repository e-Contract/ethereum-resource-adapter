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
package be.e_contract.ethereum.ra;

import static be.e_contract.ethereum.ra.EthereumManagedConnectionMetaData.EIS_PRODUCT_NAME;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumConnectionMetaData implements ConnectionMetaData {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumConnectionMetaData.class);

    @Override
    public String getEISProductName() throws ResourceException {
        LOGGER.debug("getEISProductName");
        return EIS_PRODUCT_NAME;
    }

    @Override
    public String getEISProductVersion() throws ResourceException {
        LOGGER.debug("getEISProductName");
        return Version.getImplementationVersion();
    }

    @Override
    public String getUserName() throws ResourceException {
        LOGGER.debug("getEISProductName");
        throw new UnsupportedOperationException();
    }
}
