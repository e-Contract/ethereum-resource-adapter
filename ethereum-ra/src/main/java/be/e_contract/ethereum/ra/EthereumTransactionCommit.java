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

import java.util.Collections;
import java.util.List;
import javax.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

public class EthereumTransactionCommit {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumTransactionCommit.class);

    private final List<String> rawTransactions;

    private final EthereumManagedConnection ethereumManagedConnection;

    public EthereumTransactionCommit(List<String> rawTransactions, EthereumManagedConnection ethereumManagedConnection) {
        this.rawTransactions = rawTransactions;
        this.ethereumManagedConnection = ethereumManagedConnection;
    }

    public EthereumTransactionCommit(String rawTransaction, EthereumManagedConnection ethereumManagedConnection) {
        this(Collections.singletonList(rawTransaction), ethereumManagedConnection);
    }

    public EthSendTransaction commit() throws ResourceException {
        for (String rawTransaction : this.rawTransactions) {
            int count = 10;
            LOGGER.debug("commit raw transaction: {}", rawTransaction);
            while (true) {
                try {
                    Web3j web3j = this.ethereumManagedConnection.getWeb3j();
                    EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(rawTransaction).sendAsync().get();
                    return ethSendTransaction;
                } catch (Exception ex) {
                    LOGGER.error("web3j error: " + ex.getMessage(), ex);
                    count--;
                    if (count == 0) {
                        throw new ResourceException();
                    }
                    LOGGER.warn("Retrying commit...");
                    try {
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException ex1) {
                        LOGGER.error("could not sleep: " + ex1.getMessage());
                        throw new ResourceException();
                    }
                }
            }
        }
        throw new ResourceException();
    }
}
