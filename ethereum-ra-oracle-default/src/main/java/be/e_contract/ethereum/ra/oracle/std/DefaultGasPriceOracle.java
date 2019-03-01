/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018-2019 e-Contract.be BVBA.
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
package be.e_contract.ethereum.ra.oracle.std;

import be.e_contract.ethereum.ra.api.EthereumConnection;
import be.e_contract.ethereum.ra.api.EthereumConnectionFactory;
import be.e_contract.ethereum.ra.oracle.spi.ConnectionStatusEvent;
import be.e_contract.ethereum.ra.oracle.spi.GasPriceOracleSpi;
import be.e_contract.ethereum.ra.oracle.spi.GasPriceOracleType;
import be.e_contract.ethereum.ra.oracle.spi.LatestBlockEvent;
import be.e_contract.ethereum.ra.oracle.spi.OracleEthereumConnectionFactory;
import be.e_contract.ethereum.ra.oracle.spi.PendingTransactionEvent;
import static be.e_contract.ethereum.ra.oracle.std.Timing.MOVING_WINDOW_SIZE_MINUTES;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.resource.ResourceException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;

/**
 * Default Gas Price Oracle.
 *
 * @author Frank Cornelis
 */
@GasPriceOracleType("default")
@ApplicationScoped
public class DefaultGasPriceOracle implements GasPriceOracleSpi {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGasPriceOracle.class);

    @Inject
    @OracleEthereumConnectionFactory
    private EthereumConnectionFactory ethereumConnectionFactory;

    private Map<String, PendingTransaction> pendingTransactions;
    private PriorityQueue<PendingTransaction> agingPendingTransactions;

    private Map<BigInteger, Timing> gasPrices;

    @PostConstruct
    public void postConstruct() {
        LOGGER.debug("postConstruct");
        this.pendingTransactions = new ConcurrentHashMap<>();
        this.agingPendingTransactions = new PriorityQueue<>();
        this.gasPrices = new HashMap<>();
    }

    @Override
    public BigInteger getGasPrice(Integer maxDuration) {
        BigInteger gasPrice;
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            gasPrice = ethereumConnection.getGasPrice();
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
        LOGGER.debug("node gas price: {}", gasPrice);
        if (maxDuration == null) {
            return gasPrice;
        }
        // now, let's try to get a better price
        List<Map.Entry<BigInteger, Timing>> gasPricesList = new ArrayList<>(this.gasPrices.entrySet());
        // sort on gas price
        gasPricesList.sort((o1, o2) -> o1.getKey().compareTo(o2.getKey()));
        for (Map.Entry<BigInteger, Timing> gasPriceEntry : gasPricesList) {
            if (gasPriceEntry.getValue().getCount() < 4) {
                // we want a usable average time
                continue;
            }
            if (gasPriceEntry.getKey().compareTo(gasPrice) == -1) {
                // already cheaper, but according to maxDuration?
                if (gasPriceEntry.getValue().getAverageTime() < maxDuration) {
                    LOGGER.debug("sharper gas price: {}", gasPriceEntry.getKey());
                    LOGGER.debug("average duration for this gas price: {} secs", gasPriceEntry.getValue().getAverageTime());
                    LOGGER.debug("number of transactions on this gas price: {}", gasPriceEntry.getValue().getCount());
                    return gasPriceEntry.getKey();
                }
            }
        }
        return gasPrice;
    }

    private void observePendingTransaction(@Observes PendingTransactionEvent pendingTransactionEvent) {
        String transactionHash = pendingTransactionEvent.getTransactionHash();
        DateTime timestamp = new DateTime(pendingTransactionEvent.getTimestamp());
        Transaction transaction;
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            transaction = ethereumConnection.findTransaction(transactionHash);
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return;
        }
        if (null == transaction) {
            return;
        }
        synchronized (this.pendingTransactions) {
            PendingTransaction pendingTransaction = new PendingTransaction(transactionHash, timestamp, transaction.getGasPrice());
            this.pendingTransactions.put(transaction.getHash(), pendingTransaction);
            this.agingPendingTransactions.add(pendingTransaction);
        }
    }

    private void observeLatestBlock(@Observes LatestBlockEvent latestBlockEvent) {
        String blockHash = latestBlockEvent.getBlockHash();
        EthBlock.Block block;
        BigInteger nodeGasPrice;
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            block = ethereumConnection.getBlock(blockHash, true);
            nodeGasPrice = ethereumConnection.getGasPrice();
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return;
        }
        LOGGER.debug("block: {}", block.getNumber());
        boolean updated = false;
        for (EthBlock.TransactionResult<EthBlock.TransactionObject> transactionResult : block.getTransactions()) {
            EthBlock.TransactionObject transactionObject = transactionResult.get();
            Transaction transaction = transactionObject.get();
            String transactionHash = transaction.getHash();
            // remove the transaction, independent of the transaction type (regular or contract)
            PendingTransaction pendingTransaction;
            synchronized (this.pendingTransactions) {
                pendingTransaction = this.pendingTransactions.remove(transactionHash);
                if (null != pendingTransaction) {
                    this.agingPendingTransactions.remove(pendingTransaction);
                }
            }
            if (!transaction.getGas().equals(BigInteger.valueOf(21000))) {
                // we only want stats on regular transactions
                continue;
            }
            if (null == pendingTransaction) {
                // transaction was not known as a pending one before
                continue;
            }
            updated = true;
            BigInteger gasPrice = pendingTransaction.getGasPrice();
            Timing timing;
            synchronized (this.gasPrices) {
                timing = this.gasPrices.get(gasPrice);
                if (null == timing) {
                    timing = new Timing(gasPrice);
                    this.gasPrices.put(gasPrice, timing);
                }
            }
            // we cannot use the block timestamp here, as it can happen before transaction received.
            // we use the time on which we actually received the block
            DateTime blockReceived = new DateTime(latestBlockEvent.getTimestamp());
            timing.addTiming(pendingTransaction.getCreated(), blockReceived);
        }

        DateTime now = new DateTime();
        // moving window on gas prices
        synchronized (this.gasPrices) {
            List<Timing> cleanupTimings = new LinkedList<>();
            for (Timing timing : this.gasPrices.values()) {
                if (timing.cleanMovingWindow(now)) {
                    cleanupTimings.add(timing);
                }
            }
            for (Timing cleanupTiming : cleanupTimings) {
                BigInteger gasPrice = cleanupTiming.getGasPrice();
                this.gasPrices.remove(gasPrice);
            }
            int totalCount = 0;
            for (Timing timing : this.gasPrices.values()) {
                totalCount += timing.getCount();
            }
            LOGGER.debug("total transactions in moving window: {}", totalCount);
        }

        // moving window on pending transactions
        int removedPendingTransactionsCount = 0;
        PendingTransaction pendingTransaction = this.agingPendingTransactions.peek();
        while (null != pendingTransaction) {
            DateTime created = pendingTransaction.getCreated();
            if (created.plusMinutes(MOVING_WINDOW_SIZE_MINUTES).isAfter(now)) {
                break;
            }
            // remove old entry
            synchronized (this.pendingTransactions) {
                pendingTransaction = this.agingPendingTransactions.poll();
                String transactionHash = pendingTransaction.getTransactionHash();
                this.pendingTransactions.remove(transactionHash);
            }
            removedPendingTransactionsCount++;
            pendingTransaction = this.agingPendingTransactions.peek();
        }
        LOGGER.debug("removed pending transaction from window: {}", removedPendingTransactionsCount);

        if (!updated) {
            // only redo the table on changes
            return;
        }
        LOGGER.debug("number of pending transactions: {}", this.pendingTransactions.size());
        LOGGER.debug("size of gas price table: {}", this.gasPrices.size());
        LOGGER.debug("node gas price: {}", Convert.fromWei(new BigDecimal(nodeGasPrice), Convert.Unit.GWEI));
        // next is just for debugging
        int count = 40;
        List<Map.Entry<BigInteger, Timing>> gasPricesList = new ArrayList<>(this.gasPrices.entrySet());
        // sort on gas price
        gasPricesList.sort((o1, o2) -> o1.getKey().compareTo(o2.getKey()));
        for (Map.Entry<BigInteger, Timing> gasPriceEntry : gasPricesList) {
            if (gasPriceEntry.getValue().getCount() < 4) {
                // we want a usable average time
                continue;
            }
            if (count-- == 0) {
                //only show top of the list
                break;
            }

            BigDecimal gasPriceGwei = Convert.fromWei(new BigDecimal(gasPriceEntry.getKey()), Convert.Unit.GWEI);
            LOGGER.debug("gas price {} - average time {} - count {}", gasPriceGwei, gasPriceEntry.getValue().getAverageTime(),
                    gasPriceEntry.getValue().getCount());
        }
    }

    public void observeConnectionStatus(@Observes ConnectionStatusEvent event) {
        LOGGER.debug("connected: {}", event.isConnected());
        if (!event.isConnected()) {
            // we cannot trust the timing anymore
            synchronized (this.pendingTransactions) {
                this.pendingTransactions.clear();
                this.agingPendingTransactions.clear();
            }
        }
    }
}
