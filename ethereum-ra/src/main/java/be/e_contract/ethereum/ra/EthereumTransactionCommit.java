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
import java.util.LinkedList;
import java.util.List;
import javax.resource.ResourceException;
import javax.transaction.xa.Xid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

public class EthereumTransactionCommit {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumTransactionCommit.class);

    private final List<String> rawTransactions;

    private final EthereumManagedConnection ethereumManagedConnection;

    private boolean prepared;

    private Xid xid;

    public EthereumTransactionCommit(List<String> rawTransactions, EthereumManagedConnection ethereumManagedConnection) {
        this.rawTransactions = rawTransactions;
        this.ethereumManagedConnection = ethereumManagedConnection;
    }

    public EthereumTransactionCommit(String rawTransaction, EthereumManagedConnection ethereumManagedConnection) {
        this(new LinkedList(Collections.singletonList(rawTransaction)), ethereumManagedConnection);
    }

    public EthereumTransactionCommit(EthereumManagedConnection ethereumManagedConnection, Xid xid) {
        this(new LinkedList<>(), ethereumManagedConnection);
        this.xid = xid;
    }

    public boolean isPrepared() {
        return this.prepared;
    }

    public void setPrepared(boolean prepared) {
        this.prepared = prepared;
    }

    public List<String> getRawTransactions() {
        return this.rawTransactions;
    }

    public void clear() {
        this.rawTransactions.clear();
    }

    public Xid getXid() {
        return this.xid;
    }

    public void commit() throws ResourceException {
        while (!this.rawTransactions.isEmpty()) {
            String rawTransaction = this.rawTransactions.get(0);
            int count = 10;
            LOGGER.debug("commit raw transaction: {}", rawTransaction);
            EthSendTransaction ethSendTransaction;
            while (true) {
                try {
                    Web3j web3j = this.ethereumManagedConnection.getWeb3j();
                    ethSendTransaction = web3j.ethSendRawTransaction(rawTransaction).sendAsync().get();
                    break;
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
            if (ethSendTransaction.hasError()) {
                LOGGER.warn("send transaction error: {}", ethSendTransaction.getError().getMessage());
                // do we fail the transaction here? Actually the JTA transaction itself was OK.
            }
            this.rawTransactions.remove(0);
        }
        this.prepared = false;
    }
}
