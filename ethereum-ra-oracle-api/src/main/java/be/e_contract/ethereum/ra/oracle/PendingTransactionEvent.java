/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra.oracle;

import org.web3j.protocol.core.methods.response.Transaction;

public class PendingTransactionEvent {

    private final Transaction transaction;

    public PendingTransactionEvent(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return this.transaction;
    }
}
