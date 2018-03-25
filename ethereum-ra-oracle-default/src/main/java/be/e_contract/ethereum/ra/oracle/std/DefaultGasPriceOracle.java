/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra.oracle.std;

import be.e_contract.ethereum.ra.api.EthereumConnection;
import be.e_contract.ethereum.ra.api.EthereumConnectionFactory;
import be.e_contract.ethereum.ra.oracle.GasPriceOracle;
import be.e_contract.ethereum.ra.oracle.GasPriceOracleType;
import be.e_contract.ethereum.ra.oracle.LatestBlockEvent;
import be.e_contract.ethereum.ra.oracle.PendingTransactionEvent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
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
public class DefaultGasPriceOracle implements GasPriceOracle {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGasPriceOracle.class);

    @Resource(mappedName = "java:/EthereumConnectionFactory")
    private EthereumConnectionFactory ethereumConnectionFactory;

    private Map<String, PendingTransaction> pendingTransactions;

    private Map<BigInteger, Timing> gasPrices;

    @PostConstruct
    public void postConstruct() {
        LOGGER.debug("postConstruct");
        this.pendingTransactions = new ConcurrentHashMap<>();
        this.gasPrices = new HashMap<>();
    }

    @Override
    public BigInteger getGasPrice(Integer maxDuration) {
        BigInteger gasPrice;
        try (EthereumConnection ethereumConnection = (EthereumConnection) this.ethereumConnectionFactory.getConnection()) {
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
        Transaction transaction = pendingTransactionEvent.getTransaction();
        this.pendingTransactions.put(transaction.getHash(), new PendingTransaction(transaction.getGasPrice()));
    }

    private void observeLatestBlock(@Observes LatestBlockEvent latestBlockEvent) {
        EthBlock.Block block = latestBlockEvent.getBlock();
        LOGGER.debug("block: {}", block.getNumber());
        boolean updated = false;
        for (EthBlock.TransactionResult<EthBlock.TransactionObject> transactionResult : block.getTransactions()) {
            EthBlock.TransactionObject transactionObject = transactionResult.get();
            Transaction transaction = transactionObject.get();
            String transactionHash = transaction.getHash();
            // remove the transaction, independent of the transaction type (regular or contract)
            PendingTransaction pendingTransaction = this.pendingTransactions.remove(transactionHash);
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
            Timing timing = this.gasPrices.get(gasPrice);
            if (null == timing) {
                timing = new Timing(pendingTransaction.getCreated());
                this.gasPrices.put(gasPrice, timing);
            } else {
                // we should not be using "now" here, but the block timestamp
                BigInteger timestamp = block.getTimestamp();
                Date timestampDate = new Date(timestamp.multiply(BigInteger.valueOf(1000)).longValue());
                DateTime timestampDateTime = new DateTime(timestampDate);
                timing.addTiming(pendingTransaction.getCreated(), timestampDateTime);
            }
        }

        // TODO: we should cleanup old timings here... moving window
        if (!updated) {
            // only redo the table on changes
            return;
        }

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
            LOGGER.debug("gas price {} - average time {}", gasPriceGwei, gasPriceEntry.getValue().getAverageTime());
        }
    }
}
