/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.rar.demo;

import be.e_contract.ethereum.ra.EthereumMessageListener;
import java.math.BigInteger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

@MessageDriven(messageListenerInterface = EthereumMessageListener.class, activationConfig = {
    @ActivationConfigProperty(propertyName = "nodeLocation", propertyValue = "http://localhost:8545"),
    @ActivationConfigProperty(propertyName = "fullBlock", propertyValue = "true"),
    @ActivationConfigProperty(propertyName = "deliverPending", propertyValue = "false"),
    @ActivationConfigProperty(propertyName = "deliverBlock", propertyValue = "false")
})
public class EthereumMDB implements EthereumMessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumMDB.class);

    @EJB
    private EthereumBean ethereumBean;

    @Override
    public void pendingTransaction(Transaction transaction) throws Exception {
        String transactionHash = transaction.getHash();
        LOGGER.debug("pending transaction: {}", transactionHash);
        BigInteger gasPrice = this.ethereumBean.getGasPrice(null, false);
        LOGGER.debug("gas price: {}", gasPrice);
    }

    @Override
    public void block(EthBlock.Block block) throws Exception {
        BigInteger blockNumber = block.getNumber();
        LOGGER.debug("block number: {}", blockNumber);
        BigInteger gasPrice = this.ethereumBean.getGasPrice(null, false);
        LOGGER.debug("gas price: {}", gasPrice);
    }
}
