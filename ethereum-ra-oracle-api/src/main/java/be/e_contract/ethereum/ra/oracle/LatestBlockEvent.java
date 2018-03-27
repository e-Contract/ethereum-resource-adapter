/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra.oracle;

import java.util.Date;

public class LatestBlockEvent {

    private final Date timestamp;

    private final String blockHash;

    public LatestBlockEvent(String blockHash, Date timestamp) {
        this.blockHash = blockHash;
        this.timestamp = timestamp;
    }

    public String getBlockHash() {
        return this.blockHash;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }
}
