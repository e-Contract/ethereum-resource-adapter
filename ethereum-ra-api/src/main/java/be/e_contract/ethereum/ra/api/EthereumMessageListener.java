/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra.api;

import java.util.Date;

public interface EthereumMessageListener {

    void block(String blockHash, Date timestamp) throws Exception;

    void pendingTransaction(String transactionHash, Date timestamp) throws Exception;
}
