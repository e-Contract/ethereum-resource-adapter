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
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;

/**
 * Interface for Ethereum network connections.
 *
 * @author Frank Cornelis
 */
public interface EthereumConnection extends Connection, AutoCloseable {

    /**
     * Gives back the node gas price.
     *
     * @return the gas price in wei.
     * @throws javax.resource.ResourceException
     */
    BigInteger getGasPrice() throws ResourceException;

    /**
     * Gives back the latest block number.
     *
     * @return the block number.
     * @throws ResourceException
     */
    BigInteger getBlockNumber() throws ResourceException;

    /**
     * Sends a raw transaction. Supports JCA transactions.
     *
     * @param rawTransaction
     * @return the transaction hash.
     * @throws ResourceException
     * @throws be.e_contract.ethereum.ra.api.EthereumException
     */
    String sendRawTransaction(String rawTransaction) throws ResourceException, EthereumException;

    /**
     * Gives back the status of a transaction.
     *
     * @param transactionHash
     * @return the transaction confirmation.
     * @throws ResourceException
     */
    TransactionConfirmation getTransactionConfirmation(String transactionHash) throws ResourceException;

    /**
     * Gives back the transaction for a given transaction hash. Can return null.
     *
     * @param transactionHash
     * @return the transaction, if available. Otherwise, null.
     * @throws ResourceException
     */
    Transaction findTransaction(String transactionHash) throws ResourceException;

    /**
     * Gives back a block for the given block hash.
     *
     * @param blockHash
     * @param fullTransactions
     * @return the Ethereum block.
     * @throws ResourceException
     */
    EthBlock.Block getBlock(String blockHash, boolean fullTransactions) throws ResourceException;

    /**
     * Gives back a block for the given block number.
     *
     * @param defaultBlockParameter
     * @param fullTransactions
     * @return the Ethereum block.
     * @throws ResourceException
     */
    EthBlock.Block getBlock(DefaultBlockParameter defaultBlockParameter, boolean fullTransactions) throws ResourceException;

    /**
     * Gives back the balance for the address.
     *
     * @param address
     * @return the balance in wei.
     * @throws ResourceException
     */
    BigInteger getBalance(String address) throws ResourceException;

    /**
     * Gives back the transaction nonce for the given address.
     *
     * @param address
     * @return the transaction count, which equals nonce in case there are no
     * pending transactions.
     * @throws ResourceException
     */
    BigInteger getTransactionCount(String address) throws ResourceException;

    /**
     * Gives back a list of all accounts managed by the client node.
     *
     * @return the list of account addresses.
     * @throws ResourceException
     */
    List<String> getAccounts() throws ResourceException;

    /**
     * Create a new account managed by the client node.
     *
     * @param password
     * @return the address of the new account.
     * @throws ResourceException
     */
    String newAccount(String password) throws ResourceException;

    /**
     * Unlocks the given client node managed account.
     *
     * @param account
     * @param password
     * @return true on successful unlock.
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
     * @return the transaction hash.
     * @throws ResourceException
     * @throws be.e_contract.ethereum.ra.api.EthereumException
     */
    String sendAccountTransaction(String account, String to, BigInteger value,
            BigInteger gasPrice, BigInteger nonce) throws ResourceException, EthereumException;

    /**
     * Deploys a given contract. Contract should have been compiled via web3j.
     * Contract deployment is part of the JTA transaction.
     *
     * @param contractClass
     * @param gasPrice
     * @param gasLimit
     * @param credentials
     * @param chainId
     * @return the contract transaction receipt, containing the transaction hash
     * and the contract address.
     * @throws javax.resource.ResourceException
     * @throws be.e_contract.ethereum.ra.api.EthereumException
     */
    TransactionReceipt deploy(Class<? extends Contract> contractClass, BigInteger gasPrice,
            BigInteger gasLimit, Credentials credentials, Integer chainId) throws ResourceException, EthereumException;

    /**
     * Loads a contract. Ethereum contract transactions are part of the JTA
     * transaction. The nonce is managed internally, so fast transactions are
     * possible.
     *
     * @param <T>
     * @param contractClass
     * @param contractAddress
     * @param credentials
     * @param chainId
     * @param gasPrice
     * @param gasLimit
     * @return a contract instance.
     * @throws ResourceException
     */
    <T extends Contract> T load(Class<T> contractClass, String contractAddress,
            Credentials credentials, Integer chainId, BigInteger gasPrice, BigInteger gasLimit) throws ResourceException;

    /**
     * Retrieve the chain identifier of the Ethereum network.
     *
     * @return the chain identifier, or null of EIP-155 is not active on the
     * network.
     * @throws ResourceException
     * @throws be.e_contract.ethereum.ra.api.EthereumException
     */
    Integer getChainId() throws ResourceException, EthereumException;

    /**
     * Sends an Ethereum transaction. Supports JTA transactions. The nonce is
     * managed internally, so fast transactions are possible.
     *
     * @param credentials
     * @param to
     * @param value
     * @param gasPrice
     * @param chainId
     * @return the transaction hash.
     * @throws javax.resource.ResourceException
     * @throws be.e_contract.ethereum.ra.api.EthereumException
     */
    String sendTransaction(Credentials credentials, String to, BigInteger value,
            BigInteger gasPrice, Integer chainId) throws ResourceException, EthereumException;

    /**
     * Gives back the net version.
     *
     * @return the net version.
     * @throws ResourceException
     */
    String getNetVersion() throws ResourceException;

    /**
     * Gives back the peer count of the Ethereum client node.
     *
     * @return the peer count.
     * @throws ResourceException
     */
    BigInteger getPeerCount() throws ResourceException;

    /**
     * Gives back the protocol version of the Ethereum network.
     *
     * @return the protocol version.
     * @throws ResourceException
     */
    String getProtocolVersion() throws ResourceException;

    /**
     * Returns true if the Ethereum client node is synchronizing.
     *
     * @return true if the client node is still syncing.
     * @throws ResourceException
     */
    boolean isSyncing() throws ResourceException;
}
