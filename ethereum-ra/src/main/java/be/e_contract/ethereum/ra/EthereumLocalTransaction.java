/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import java.util.LinkedList;
import java.util.List;
import javax.resource.ResourceException;
import javax.resource.spi.LocalTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;

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
        for (String rawTransaction : this.rawTransactions) {
            LOGGER.debug("commit raw transaction: {}", rawTransaction);
            try {
                Web3j web3j = this.ethereumManagedConnection.getWeb3j();
                web3j.ethSendRawTransaction(rawTransaction);
            } catch (Exception ex) {
                LOGGER.error("web3j error: " + ex.getMessage(), ex);
                throw new ResourceException();
            }
        }
        this.rawTransactions.clear();
    }

    @Override
    public void rollback() throws ResourceException {
        LOGGER.debug("rollback");
        this.rawTransactions.clear();
    }

    void scheduleRawTransaction(String rawTransaction) {
        LOGGER.debug("schedule raw transaction: {}", rawTransaction);
        this.rawTransactions.add(rawTransaction);
    }
}
