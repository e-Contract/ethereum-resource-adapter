/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.rar.demo.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;

@Singleton
@Startup
public class EthereumGasPriceOracleBean {

    public static final String JNDI_NAME = "java:app/EthereumGasPriceOracleBean";

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumGasPriceOracleBean.class);

    private Map<String, PendingTransaction> pendingTransactions;

    private Map<BigInteger, Timing> gasPrices;

    @EJB
    private EthereumBean ethereumBean;

    @PostConstruct
    public void postConstruct() throws Exception {
        LOGGER.debug("postConstruct");
        this.pendingTransactions = new ConcurrentHashMap<>();
        this.gasPrices = new HashMap<>();
    }

    public void block(EthBlock.Block block) throws Exception {
        LOGGER.debug("block: {}", block.getNumber());
        boolean updated = false;
        for (EthBlock.TransactionResult<EthBlock.TransactionObject> transactionResult : block.getTransactions()) {
            EthBlock.TransactionObject transactionObject = transactionResult.get();
            Transaction transaction = transactionObject.get();
            String transactionHash = transaction.getHash();
            // remove the transaction, independent of the transaction type (regular or contract)
            PendingTransaction pendingTransaction = pendingTransactions.remove(transactionHash);
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
            Timing timing = gasPrices.get(gasPrice);
            if (null == timing) {
                timing = new Timing(pendingTransaction.getCreated());
                gasPrices.put(gasPrice, timing);
            } else {
                // we should not be using "now" here, but the block timestamp
                BigInteger timestamp = block.getTimestamp();
                Date timestampDate = new Date(timestamp.multiply(BigInteger.valueOf(1000)).longValue());
                DateTime timestampDateTime = new DateTime(timestampDate);
                timing.addTiming(pendingTransaction.getCreated(), timestampDateTime);
            }
        }

        if (!updated) {
            // only redo the table on changes
            return;
        }

        BigDecimal nodeGasPrice = new BigDecimal(this.ethereumBean.getGasPrice(null, false));
        BigDecimal nodeGasPriceGwei = Convert.fromWei(nodeGasPrice, Convert.Unit.GWEI);
        LOGGER.debug("node gas price: {} gwei", nodeGasPriceGwei);
        int count = 40;
        List<Map.Entry<BigInteger, Timing>> gasPricesList = new ArrayList<>(gasPrices.entrySet());
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

    public void pendingTransaction(Transaction transaction) throws Exception {
        this.pendingTransactions.put(transaction.getHash(), new PendingTransaction(transaction.getGasPrice()));
    }

    public BigInteger getGasPrice() {
        return BigInteger.ONE;
    }
}
