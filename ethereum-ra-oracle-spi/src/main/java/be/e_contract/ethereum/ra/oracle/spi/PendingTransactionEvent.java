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
package be.e_contract.ethereum.ra.oracle.spi;

import java.util.Date;

/**
 * CDI event fired when a new pending transaction becomes available.
 *
 * @author Frank Cornelis
 */
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
