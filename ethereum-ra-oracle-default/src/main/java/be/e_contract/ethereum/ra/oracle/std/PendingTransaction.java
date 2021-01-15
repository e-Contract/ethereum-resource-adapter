/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018-2021 e-Contract.be BV.
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
package be.e_contract.ethereum.ra.oracle.std;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class PendingTransaction implements Comparable<PendingTransaction> {

    private final String transactionHash;
    private final BigInteger gasPrice;
    private final LocalDateTime created;

    public PendingTransaction(String transactionHash, LocalDateTime created, BigInteger gasPrice) {
        this.transactionHash = transactionHash;
        this.created = created;
        this.gasPrice = gasPrice;
    }

    public String getTransactionHash() {
        return this.transactionHash;
    }

    public BigInteger getGasPrice() {
        return this.gasPrice;
    }

    public LocalDateTime getCreated() {
        return this.created;
    }

    @Override
    public int compareTo(PendingTransaction pt) {
        return this.created.compareTo(pt.getCreated());
    }
}
