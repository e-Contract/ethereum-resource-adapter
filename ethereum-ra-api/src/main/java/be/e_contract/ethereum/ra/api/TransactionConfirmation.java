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
package be.e_contract.ethereum.ra.api;

import java.io.Serializable;
import java.util.Date;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class TransactionConfirmation implements Serializable {

    private final String transactionHash;

    private boolean pendingTransaction;

    private boolean failed;

    private long confirmingBlocks;

    private Date timestamp;

    private TransactionReceipt transactionReceipt;

    public TransactionConfirmation(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getTransactionHash() {
        return this.transactionHash;
    }

    public boolean isPendingTransaction() {
        return this.pendingTransaction;
    }

    public void setPendingTransaction(boolean pendingTransaction) {
        this.pendingTransaction = pendingTransaction;
    }

    public boolean isFailed() {
        return this.failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public void setInfo(TransactionReceipt transactionReceipt, long confirmingBlocks, Date timestamp) {
        this.transactionReceipt = transactionReceipt;
        this.confirmingBlocks = confirmingBlocks;
        this.timestamp = timestamp;
    }

    public long getConfirmingBlocks() {
        return this.confirmingBlocks;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public TransactionReceipt getTransactionReceipt() {
        return this.transactionReceipt;
    }
}
