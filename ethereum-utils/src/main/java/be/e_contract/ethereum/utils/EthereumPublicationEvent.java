/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2019-2024 e-Contract.be BV.
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
package be.e_contract.ethereum.utils;

import java.io.Serializable;
import java.math.BigInteger;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

/**
 * This CDI event gets fired when transactions have been published on the
 * Ethereum network.
 *
 * @author Frank Cornelis
 */
public class EthereumPublicationEvent {

    private final String transactionHash;

    private final TransactionReceipt transactionReceipt;

    private final BigInteger publicationBlockNumber;

    private final EthereumFinalState finalState;

    private final Serializable info;

    public EthereumPublicationEvent(String transactionHash, TransactionReceipt transactionReceipt,
            BigInteger publicationBlockNumber, Serializable info) {
        this.transactionHash = transactionHash;
        this.transactionReceipt = transactionReceipt;
        this.publicationBlockNumber = publicationBlockNumber;
        this.finalState = EthereumFinalState.SUCCEEDED;
        this.info = info;
    }

    public EthereumPublicationEvent(String transactionHash, EthereumFinalState finalState,
            Serializable info) {
        this.transactionHash = transactionHash;
        this.transactionReceipt = null;
        this.publicationBlockNumber = null;
        this.finalState = finalState;
        this.info = info;
    }

    /**
     * Gives back the hash of the transactions.
     *
     * @return the transaction hash value.
     */
    public String getTransactionHash() {
        return this.transactionHash;
    }

    /**
     * Gives back the web3j transaction receipt.
     *
     * @return
     */
    public TransactionReceipt getTransactionReceipt() {
        return this.transactionReceipt;
    }

    /**
     * Gives back the block number on which the transaction has been published
     * within the Ethereum network.
     *
     * @return the block number.
     */
    public BigInteger getPublicationBlockNumber() {
        return this.publicationBlockNumber;
    }

    /**
     * Gives back the final state of the transaction within the network.
     *
     * @return the final state.
     * @see EthereumFinalState
     */
    public EthereumFinalState getFinalState() {
        return this.finalState;
    }

    /**
     * Gives back the info object parameter
     *
     * @return the info object.
     * @see EthereumTransactionManager#monitorTransaction(java.lang.String,
     * java.io.Serializable)
     */
    public Serializable getInfo() {
        return this.info;
    }
}
