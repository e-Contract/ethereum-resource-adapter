/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra.oracle.std;

import java.math.BigInteger;
import org.joda.time.DateTime;

public class PendingTransaction {

    private final BigInteger gasPrice;
    private final DateTime created;

    public PendingTransaction(BigInteger gasPrice) {
        this.created = new DateTime();
        this.gasPrice = gasPrice;
    }

    public BigInteger getGasPrice() {
        return this.gasPrice;
    }

    public DateTime getCreated() {
        return this.created;
    }
}
