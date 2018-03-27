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
package be.e_contract.ethereum.ra.api;

import java.math.BigInteger;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

/**
 * Interface for Ethereum network connections.
 *
 * @author Frank Cornelis
 */
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

    /**
     * Gives back a block for the given block hash.
     *
     * @param blockHash
     * @param fullTransactions
     * @return
     * @throws ResourceException
     */
    EthBlock.Block getBlock(String blockHash, boolean fullTransactions) throws ResourceException;

    /**
     * Gives back the balance for the address.
     *
     * @param address
     * @return
     * @throws ResourceException
     */
    BigInteger getBalance(String address) throws ResourceException;
}
