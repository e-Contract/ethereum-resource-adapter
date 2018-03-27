/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra.oracle;

import java.util.Date;

public class PendingTransactionEvent {

    private final Date timestamp;

    private final String transactionHash;

    public PendingTransactionEvent(String transactionHash, Date timestamp) {
        this.transactionHash = transactionHash;
        this.timestamp = timestamp;
    }

    public String getTransactionHash() {
        return this.transactionHash;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }
}
