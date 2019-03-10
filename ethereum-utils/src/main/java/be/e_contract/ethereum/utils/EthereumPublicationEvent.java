/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2019 e-Contract.be BVBA.
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

    public String getTransactionHash() {
        return this.transactionHash;
    }

    public TransactionReceipt getTransactionReceipt() {
        return this.transactionReceipt;
    }

    public BigInteger getPublicationBlockNumber() {
        return this.publicationBlockNumber;
    }

    public EthereumFinalState getFinalState() {
        return this.finalState;
    }

    public Serializable getInfo() {
        return this.info;
    }
}
