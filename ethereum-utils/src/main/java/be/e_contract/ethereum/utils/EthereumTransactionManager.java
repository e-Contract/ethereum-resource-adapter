/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2019-2023 e-Contract.be BV.
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
package be.e_contract.ethereum.utils;

import be.e_contract.ethereum.ra.api.EthereumConnection;
import be.e_contract.ethereum.ra.api.EthereumConnectionFactory;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

/**
 * This CDI bean helps you to keep track of your Ethereum transactions.
 *
 * @author Frank Cornelis
 */
@ApplicationScoped
public class EthereumTransactionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumTransactionManager.class);

    public static final int DEFAULT_EXPIRATION_BLOCK_COUNT = 60 * 60 / 12; // about 1 hour

    public static final int DEFAULT_CONFIRMATION_BLOCK_COUNT = 12;

    @Resource(lookup = "java:/EthereumConnectionFactory")
    private EthereumConnectionFactory ethereumConnectionFactory;

    @Inject
    private Event<EthereumPublicationEvent> ethereumPublicationEvent;

    private static class TransactionInfo {

        private final BigInteger expirationBlockNumber;
        private final int confirmationBlockCount;
        private final Serializable info;

        public TransactionInfo(int confirmationBlockCount, BigInteger expirationBlockNumber,
                Serializable info) {
            this.confirmationBlockCount = confirmationBlockCount;
            this.expirationBlockNumber = expirationBlockNumber;
            this.info = info;
        }

        public BigInteger getExpirationBlockNumber() {
            return this.expirationBlockNumber;
        }

        public int getConfirmationBlockCount() {
            return this.confirmationBlockCount;
        }

        public Serializable getInfo() {
            return this.info;
        }
    }

    // key: transaction hash
    private Map<String, TransactionInfo> transactions;

    private BigInteger latestBlockNumber;

    private int defaultExpirationBlockCount;

    private int defaultConfirmationBlockCount;

    @PostConstruct
    public void postConstruct() {
        this.transactions = new ConcurrentHashMap<>();
        this.defaultConfirmationBlockCount = DEFAULT_CONFIRMATION_BLOCK_COUNT;
        this.defaultExpirationBlockCount = DEFAULT_EXPIRATION_BLOCK_COUNT;
    }

    /**
     * Sets the default transaction policy.
     *
     * @param defaultConfirmationBlockCount
     * @param defaultExpirationBlockCount
     */
    public void setPolicy(int defaultConfirmationBlockCount, int defaultExpirationBlockCount) {
        this.defaultConfirmationBlockCount = defaultConfirmationBlockCount;
        this.defaultExpirationBlockCount = defaultExpirationBlockCount;
    }

    /**
     * Request the transaction manager to look for confirmation on the given
     * transaction.
     *
     * @param transactionHash
     * @throws javax.resource.ResourceException
     */
    public void monitorTransaction(String transactionHash) throws ResourceException {
        monitorTransaction(transactionHash, null);
    }

    /**
     * Request the transaction manager to look for confirmation on the given
     * transaction.
     *
     * @param transactionHash
     * @param info the optional application information to be delivered via the
     * fired CDI event.
     * @throws javax.resource.ResourceException
     */
    public void monitorTransaction(String transactionHash, Serializable info) throws ResourceException {
        monitorTransaction(transactionHash, this.defaultConfirmationBlockCount, this.defaultExpirationBlockCount, info);
    }

    /**
     * Request the transaction manager to look for confirmation on the given
     * transaction.
     *
     * @param transactionHash
     * @param confirmationBlockCount number of block to wait for transaction
     * confirmation.
     * @param expirationBlockCount number of blocks to wait for marking
     * transaction as lost.
     * @throws javax.resource.ResourceException
     */
    public void monitorTransaction(String transactionHash, int confirmationBlockCount, int expirationBlockCount) throws ResourceException {
        monitorTransaction(transactionHash, confirmationBlockCount, expirationBlockCount, null);
    }

    /**
     * Request the transaction manager to look for confirmation on the given
     * transaction.
     *
     * @param transactionHash
     * @param confirmationBlockCount number of block to wait for transaction
     * confirmation.
     * @param expirationBlockCount number of blocks to wait for marking
     * transaction as lost.
     * @param info the optional application information to be delivered via the
     * fired CDI event.
     * @throws javax.resource.ResourceException
     */
    public void monitorTransaction(String transactionHash, int confirmationBlockCount,
            int expirationBlockCount, Serializable info) throws ResourceException {
        if (this.transactions.containsKey(transactionHash)) {
            return;
        }
        if (null == this.latestBlockNumber) {
            try ( EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
                this.latestBlockNumber = ethereumConnection.getBlockNumber();
            }
        }
        BigInteger expirationBlockNumber = this.latestBlockNumber.add(BigInteger.valueOf(expirationBlockCount));
        TransactionInfo transactionInfo = new TransactionInfo(confirmationBlockCount, expirationBlockNumber, info);
        this.transactions.put(transactionHash, transactionInfo);
        LOGGER.debug("monitoring transaction: {}", transactionHash);
    }

    private boolean isPending(String transactionHash, EthBlock.Block pendingBlock) {
        for (EthBlock.TransactionResult transactionResult : pendingBlock.getTransactions()) {
            EthBlock.TransactionObject transactionObject = (EthBlock.TransactionObject) transactionResult;
            Transaction transaction = transactionObject.get();
            if (transactionHash.equals(transaction.getHash())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Call this to signal that a new block has arrived.
     *
     * @param blockHash the block hash.
     * @throws javax.resource.ResourceException
     */
    public void block(String blockHash) throws ResourceException {
        List<EthereumPublicationEvent> publicationEvents = new LinkedList<>();
        try ( EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            this.latestBlockNumber = ethereumConnection.getBlock(blockHash, false).getNumber();
            EthBlock.Block pendingBlock = ethereumConnection.getBlock(DefaultBlockParameterName.PENDING, true);
            for (Map.Entry<String, TransactionInfo> transactionEntry : this.transactions.entrySet()) {
                String transactionHash = transactionEntry.getKey();
                TransactionInfo transactionInfo = transactionEntry.getValue();
                BigInteger expirationBlockNumber = transactionInfo.getExpirationBlockNumber();
                if (isPending(transactionHash, pendingBlock)) {
                    LOGGER.debug("transaction still pending: {}", transactionHash);
                    continue;
                }
                EthGetTransactionReceipt ethGetTransactionReceipt = ethereumConnection.getTransactionReceipt(transactionHash);
                Optional<TransactionReceipt> transactionReceiptOptional = ethGetTransactionReceipt.getTransactionReceipt();
                if (!transactionReceiptOptional.isPresent()) {
                    LOGGER.debug("no transaction receipt present");
                    LOGGER.debug("transaction probably still pending but not in pending block: {}", transactionHash);
                    if (this.latestBlockNumber.compareTo(expirationBlockNumber) > 0) {
                        publicationEvents.add(new EthereumPublicationEvent(transactionHash, EthereumFinalState.DISAPPEARED,
                                transactionInfo.getInfo()));
                    }
                    continue;
                }
                TransactionReceipt transactionReceipt = transactionReceiptOptional.get();
                if (!transactionReceipt.isStatusOK()) {
                    LOGGER.debug("Transaction has failed with status: " + transactionReceipt.getStatus());
                    publicationEvents.add(new EthereumPublicationEvent(transactionHash, EthereumFinalState.FAILED,
                            transactionInfo.getInfo()));
                    continue;
                }
                BigInteger transactionBlockNumber = transactionReceipt.getBlockNumber();
                long confirmingBlocks = this.latestBlockNumber.subtract(transactionBlockNumber).add(BigInteger.ONE).longValueExact();
                if (confirmingBlocks < transactionInfo.getConfirmationBlockCount()) {
                    LOGGER.debug("transaction {} confirming blocks {}", transactionHash, confirmingBlocks);
                    continue;
                }
                LOGGER.debug("transaction {} confirmed", transactionHash);
                LOGGER.debug("gas used: {} wei", transactionReceipt.getGasUsed());
                LOGGER.debug("cumulative gas used: {} wei", transactionReceipt.getCumulativeGasUsed());
                BigInteger publicationBlockNumber = transactionReceipt.getBlockNumber();
                publicationEvents.add(new EthereumPublicationEvent(transactionHash, transactionReceipt, publicationBlockNumber,
                        transactionInfo.getInfo()));
            }
        }
        for (EthereumPublicationEvent publicationEvent : publicationEvents) {
            try {
                this.ethereumPublicationEvent.fire(publicationEvent);
            } catch (Exception e) {
                LOGGER.error("CDI event exception: " + e.getMessage(), e);
            }
            this.transactions.remove(publicationEvent.getTransactionHash());
        }
    }
}
