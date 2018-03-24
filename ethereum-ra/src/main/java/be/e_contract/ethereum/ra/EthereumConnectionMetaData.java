/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import javax.resource.ResourceException;
import javax.resource.cci.ConnectionMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumConnectionMetaData implements ConnectionMetaData {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumConnectionMetaData.class);

    @Override
    public String getEISProductName() throws ResourceException {
        LOGGER.debug("getEISProductName");
        throw new UnsupportedOperationException();
    }

    @Override
    public String getEISProductVersion() throws ResourceException {
        LOGGER.debug("getEISProductName");
        throw new UnsupportedOperationException();
    }

    @Override
    public String getUserName() throws ResourceException {
        LOGGER.debug("getEISProductName");
        throw new UnsupportedOperationException();
    }
}
