/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import javax.resource.ResourceException;
import javax.resource.cci.LocalTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumCCILocalTransaction implements LocalTransaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumCCILocalTransaction.class);

    private final EthereumLocalTransaction ethereumLocalTransaction;

    public EthereumCCILocalTransaction(EthereumLocalTransaction ethereumLocalTransaction) {
        this.ethereumLocalTransaction = ethereumLocalTransaction;
    }

    @Override
    public void begin() throws ResourceException {
        LOGGER.debug("begin");
        this.ethereumLocalTransaction.begin();
    }

    @Override
    public void commit() throws ResourceException {
        LOGGER.debug("commit");
        this.ethereumLocalTransaction.commit();
    }

    @Override
    public void rollback() throws ResourceException {
        LOGGER.debug("rollback");
        this.ethereumLocalTransaction.rollback();
    }
}
