/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import javax.resource.ResourceException;
import javax.resource.spi.LocalTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumLocalTransaction implements LocalTransaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumLocalTransaction.class);

    @Override
    public void begin() throws ResourceException {
        LOGGER.debug("begin");
    }

    @Override
    public void commit() throws ResourceException {
        LOGGER.debug("commit");
    }

    @Override
    public void rollback() throws ResourceException {
        LOGGER.debug("rollback");
    }
}
