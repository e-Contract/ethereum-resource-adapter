/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra.api;

import java.math.BigInteger;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

public interface EthereumConnection extends Connection, AutoCloseable {

    /**
     * Gives back the node gas price.
     *
     * @return
     * @throws javax.resource.ResourceException
     */
    BigInteger getGasPrice() throws ResourceException;

    /**
     * Gives back the latest block number.
     *
     * @return
     * @throws ResourceException
     */
    BigInteger getBlockNumber() throws ResourceException;

    /**
     * Sends a raw transaction. Supports JCA transactions.
     *
     * @param rawTransaction
     * @return
     * @throws ResourceException
     */
    String sendRawTransaction(String rawTransaction) throws ResourceException;

    /**
     * Gives back the status of a transaction.
     *
     * @param transactionHash
     * @return
     * @throws ResourceException
     */
    TransactionConfirmation getTransactionConfirmation(String transactionHash) throws ResourceException;

    /**
     * Gives back the transaction for a given transaction hash. Can return null.
     *
     * @param transactionHash
     * @return
     * @throws ResourceException
     */
    Transaction findTransaction(String transactionHash) throws ResourceException;

    EthBlock.Block getBlock(String blockHash, boolean fullTransactions) throws ResourceException;
}
