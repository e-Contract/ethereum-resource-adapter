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
package be.e_contract.ethereum.ra.tx;

import be.e_contract.ethereum.ra.EthereumManagedConnection;
import javax.resource.ResourceException;
import javax.resource.spi.LocalTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumLocalTransaction implements LocalTransaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumLocalTransaction.class);

    private final EthereumTransactionCommit ethereumTransactionCommit;

    public EthereumLocalTransaction(EthereumManagedConnection ethereumManagedConnection) {
        this.ethereumTransactionCommit = new EthereumTransactionCommit(ethereumManagedConnection);
    }

    @Override
    public void begin() throws ResourceException {
        LOGGER.debug("begin");
        this.ethereumTransactionCommit.clear();
    }

    @Override
    public void commit() throws ResourceException {
        LOGGER.debug("commit");
        this.ethereumTransactionCommit.commit();
    }

    @Override
    public void rollback() throws ResourceException {
        LOGGER.debug("rollback");
        LOGGER.debug("number of raw transactions in queue: {}",
                this.ethereumTransactionCommit.getTransactionCount());
        this.ethereumTransactionCommit.rollback();
    }

    public void scheduleRawTransaction(String rawTransaction) throws ResourceException {
        LOGGER.debug("schedule raw transaction: {}", rawTransaction);
        this.ethereumTransactionCommit.addRawTransaction(rawTransaction);
    }
}
