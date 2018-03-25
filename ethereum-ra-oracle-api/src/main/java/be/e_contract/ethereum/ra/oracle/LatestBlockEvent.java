/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra.oracle;

import org.web3j.protocol.core.methods.response.EthBlock;

public class LatestBlockEvent {

    private final EthBlock.Block block;

    public LatestBlockEvent(EthBlock.Block block) {
        this.block = block;
    }

    public EthBlock.Block getBlock() {
        return this.block;
    }
}
