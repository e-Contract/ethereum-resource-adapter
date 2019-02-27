/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2019 e-Contract.be BVBA.
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

    @Resource(mappedName = "java:/EthereumConnectionFactory")
    private EthereumConnectionFactory ethereumConnectionFactory;

    @Inject
    private Event<EthereumPublicationEvent> ethereumPublicationEvent;

    // transaction hash -> expiration block number;
    private Map<String, BigInteger> transactions;

    private BigInteger latestBlockNumber;

    @PostConstruct
    public void postConstruct() {
        this.transactions = new ConcurrentHashMap<>();
    }

    /**
     * Request the transaction manager to look for confirmation on the given
     * transaction.
     *
     * @param transactionHash
     * @throws javax.resource.ResourceException
     */
    public void monitorTransaction(String transactionHash) throws ResourceException {
        if (this.transactions.containsKey(transactionHash)) {
            return;
        }
        if (null == this.latestBlockNumber) {
            try (EthereumConnection ethereumConnection = (EthereumConnection) this.ethereumConnectionFactory.getConnection()) {
                this.latestBlockNumber = ethereumConnection.getBlockNumber();
            }
        }
        long expirationDelta = 60 * 60 / 12; // about 1 hour
        BigInteger expirationBlockNumber = this.latestBlockNumber.add(BigInteger.valueOf(expirationDelta));
        this.transactions.put(transactionHash, expirationBlockNumber);
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
        try (EthereumConnection ethereumConnection = (EthereumConnection) this.ethereumConnectionFactory.getConnection()) {
            this.latestBlockNumber = ethereumConnection.getBlock(blockHash, false).getNumber();
            EthBlock.Block pendingBlock = ethereumConnection.getBlock(DefaultBlockParameterName.PENDING, true);
            for (Map.Entry<String, BigInteger> transactionEntry : this.transactions.entrySet()) {
                String transactionHash = transactionEntry.getKey();
                BigInteger expirationBlockNumber = transactionEntry.getValue();
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
                        publicationEvents.add(new EthereumPublicationEvent(transactionHash, EthereumFinalState.DISAPPEARED));
                    }
                    continue;
                }
                TransactionReceipt transactionReceipt = transactionReceiptOptional.get();
                if (!transactionReceipt.isStatusOK()) {
                    LOGGER.debug("Transaction has failed with status: " + transactionReceipt.getStatus());
                    publicationEvents.add(new EthereumPublicationEvent(transactionHash, EthereumFinalState.FAILED));
                    continue;
                }
                BigInteger transactionBlockNumber = transactionReceipt.getBlockNumber();
                long confirmingBlocks = this.latestBlockNumber.subtract(transactionBlockNumber).add(BigInteger.ONE).longValueExact();
                if (confirmingBlocks < 12) {
                    LOGGER.debug("transaction {} confirming blocks {}", transactionHash, confirmingBlocks);
                    continue;
                }
                BigInteger publicationBlockNumber = transactionReceipt.getBlockNumber();
                publicationEvents.add(new EthereumPublicationEvent(transactionHash, publicationBlockNumber));
            }
        }
        for (EthereumPublicationEvent publicationEvent : publicationEvents) {
            this.ethereumPublicationEvent.fire(publicationEvent);
            this.transactions.remove(publicationEvent.getTransactionHash());
        }
    }
}
