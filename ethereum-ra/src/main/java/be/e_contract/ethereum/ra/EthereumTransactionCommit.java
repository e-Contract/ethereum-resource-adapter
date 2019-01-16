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

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.resource.ResourceException;
import javax.transaction.xa.Xid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.SignedRawTransaction;
import org.web3j.crypto.TransactionDecoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

public class EthereumTransactionCommit {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumTransactionCommit.class);

    private final List<String> rawTransactions;

    private final EthereumManagedConnection ethereumManagedConnection;

    private boolean prepared;

    private Xid xid;

    public EthereumTransactionCommit(EthereumManagedConnection ethereumManagedConnection, Xid xid) {
        this.rawTransactions = new LinkedList<>();
        this.ethereumManagedConnection = ethereumManagedConnection;
        this.xid = xid;
    }

    public EthereumTransactionCommit(EthereumManagedConnection ethereumManagedConnection) {
        this(ethereumManagedConnection, null);
    }

    public boolean isPrepared() {
        return this.prepared;
    }

    public void setPrepared(boolean prepared) {
        this.prepared = prepared;
    }

    public Xid getXid() {
        return this.xid;
    }

    public void clear() {
        this.rawTransactions.clear();
    }

    public int getTransactionCount() {
        return this.rawTransactions.size();
    }

    public void addRawTransaction(String rawTransaction) throws ResourceException {
        // perform some basic input validation first
        RawTransaction decodedRawTransaction = TransactionDecoder.decode(rawTransaction);
        if (!(decodedRawTransaction instanceof SignedRawTransaction)) {
            LOGGER.warn("transaction not signed");
            throw new ResourceException("transaction not signed");
        }
        SignedRawTransaction signedRawTransaction = (SignedRawTransaction) decodedRawTransaction;
        try {
            signedRawTransaction.getFrom();
        } catch (SignatureException ex) {
            LOGGER.error("transaction signature error: " + ex.getMessage(), ex);
            throw new ResourceException();
        }
        this.rawTransactions.add(rawTransaction);
    }

    public void rollback() {
        // we have to clear the relevant nonces cache here
        EthereumResourceAdapter ethereumResourceAdapter = this.ethereumManagedConnection.getResourceAdapter();
        Map<String, BigInteger> nonces = ethereumResourceAdapter.getNonces();
        for (String rawTransaction : this.rawTransactions) {
            SignedRawTransaction signedRawTransaction = (SignedRawTransaction) TransactionDecoder.decode(rawTransaction);
            String from;
            try {
                from = signedRawTransaction.getFrom();
            } catch (SignatureException ex) {
                LOGGER.error("transaction signature error: " + ex.getMessage(), ex);
                continue;
            }
            synchronized (nonces) {
                nonces.remove(from);
            }
        }
        this.rawTransactions.clear();
        this.prepared = false;
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
                // we need to reset the nonce cache for this from address
                SignedRawTransaction signedRawTransaction = (SignedRawTransaction) TransactionDecoder.decode(rawTransaction);
                String from = null;
                try {
                    from = signedRawTransaction.getFrom();
                } catch (SignatureException ex) {
                    LOGGER.error("transaction signature error: " + ex.getMessage(), ex);
                }
                if (from != null) {
                    EthereumResourceAdapter ethereumResourceAdapter = this.ethereumManagedConnection.getResourceAdapter();
                    Map<String, BigInteger> nonces = ethereumResourceAdapter.getNonces();
                    synchronized (nonces) {
                        nonces.remove(from);
                    }
                }
            }
            this.rawTransactions.remove(0);
        }
        this.prepared = false;
    }
}
