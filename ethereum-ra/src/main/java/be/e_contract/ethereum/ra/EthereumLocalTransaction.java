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

import java.util.LinkedList;
import java.util.List;
import javax.resource.ResourceException;
import javax.resource.spi.LocalTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumLocalTransaction implements LocalTransaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumLocalTransaction.class);

    private final List<String> rawTransactions;

    private final EthereumManagedConnection ethereumManagedConnection;

    public EthereumLocalTransaction(EthereumManagedConnection ethereumManagedConnection) {
        this.ethereumManagedConnection = ethereumManagedConnection;
        this.rawTransactions = new LinkedList<>();
    }

    @Override
    public void begin() throws ResourceException {
        LOGGER.debug("begin");
        this.rawTransactions.clear();
    }

    @Override
    public void commit() throws ResourceException {
        LOGGER.debug("commit");
        EthereumTransactionCommit ethereumTransactionCommit = new EthereumTransactionCommit(this.rawTransactions, this.ethereumManagedConnection);
        ethereumTransactionCommit.commit();
        this.rawTransactions.clear();
    }

    @Override
    public void rollback() throws ResourceException {
        LOGGER.debug("rollback");
        LOGGER.debug("number of raw transactions in queue: {}", this.rawTransactions.size());
        this.rawTransactions.clear();
    }

    public void scheduleRawTransaction(String rawTransaction) {
        LOGGER.debug("schedule raw transaction: {}", rawTransaction);
        this.rawTransactions.add(rawTransaction);
    }
}
