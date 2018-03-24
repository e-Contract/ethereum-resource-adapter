/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.rar.demo.model;

import java.math.BigInteger;
import javax.ejb.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

@Singleton
public class EthereumGasPriceOracleBean {

    public static final String JNDI_NAME = "java:app/EthereumGasPriceOracleBean";

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumGasPriceOracleBean.class);

    public void block(EthBlock.Block block) throws Exception {
        LOGGER.debug("block: {}", block.getNumber());
    }

    public void pendingTransaction(Transaction transaction) throws Exception {
        LOGGER.debug("pending transaction: {}", transaction.getHash());
    }
    
    public BigInteger getGasPrice() {
        return BigInteger.ONE;
    }
}
