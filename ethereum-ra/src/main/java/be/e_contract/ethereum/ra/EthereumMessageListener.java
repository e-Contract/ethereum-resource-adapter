/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import java.math.BigInteger;

public interface EthereumMessageListener {

    void pendingTransaction(String transactionHash);
    
    void block(BigInteger blockNumber);
}
