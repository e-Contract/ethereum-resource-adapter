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
import java.util.List;
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

    /**
     * Gives back the transaction nonce for the given address.
     *
     * @param address
     * @return
     * @throws ResourceException
     */
    BigInteger getTransactionCount(String address) throws ResourceException;

    /**
     * Gives back a list of all accounts managed by the client node.
     *
     * @return
     * @throws ResourceException
     */
    List<String> getAccounts() throws ResourceException;

    /**
     * Create a new account managed by the client node.
     *
     * @param password
     * @return
     * @throws ResourceException
     */
    String newAccount(String password) throws ResourceException;

    /**
     * Unlocks the given client node managed account.
     *
     * @param account
     * @param password
     * @return
     * @throws ResourceException
     * @throws be.e_contract.ethereum.ra.api.EthereumException
     */
    boolean unlockAccount(String account, String password) throws ResourceException, EthereumException;

    /**
     * Send transaction via client node account. This type of Ethereum
     * transaction is not part of the JTA transaction.
     *
     * @param account
     * @param to
     * @param value
     * @param gasPrice
     * @param nonce
     * @return
     * @throws ResourceException
     * @throws be.e_contract.ethereum.ra.api.EthereumException
     */
    String sendAccountTransaction(String account, String to, BigInteger value,
            BigInteger gasPrice, BigInteger nonce) throws ResourceException, EthereumException;
}
