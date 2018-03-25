/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra.api;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

public class TransactionConfirmation implements Serializable {

    private final String transactionHash;

    private boolean pendingTransaction;

    private boolean failed;

    private BigInteger blockNumber;

    private long confirmingBlocks;

    private Date timestamp;

    private String to;

    private String from;

    private BigInteger gasUsed;

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

    public void setInfo(String from, String to, BigInteger blockNumber, BigInteger gasUsed, long confirmingBlocks, Date timestamp) {
        this.from = from;
        this.to = to;
        this.blockNumber = blockNumber;
        this.gasUsed = gasUsed;
        this.confirmingBlocks = confirmingBlocks;
        this.timestamp = timestamp;
    }

    public BigInteger getBlockNumber() {
        return this.blockNumber;
    }

    public long getConfirmingBlocks() {
        return this.confirmingBlocks;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public String getTo() {
        return this.to;
    }

    public String getFrom() {
        return this.from;
    }

    public BigInteger getGasUsed() {
        return this.gasUsed;
    }
}
