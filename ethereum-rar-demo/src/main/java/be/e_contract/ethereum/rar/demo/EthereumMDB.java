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
import javax.ejb.MessageDriven;
import org.jboss.ejb3.annotation.ResourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

// TODO: should find a cleaner way to refer to the RAR
@ResourceAdapter(value = "ethereum-rar-demo-deploy-1.0.0-SNAPSHOT.ear#ethereum-ra.rar")
@MessageDriven(messageListenerInterface = EthereumMessageListener.class, activationConfig = {
    @ActivationConfigProperty(propertyName = "nodeLocation", propertyValue = "http://localhost:8545"),
    @ActivationConfigProperty(propertyName = "fullBlock", propertyValue = "true")
})
public class EthereumMDB implements EthereumMessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumMDB.class);

    @Override
    public void pendingTransaction(Transaction transaction) {
        String transactionHash = transaction.getHash();
        LOGGER.debug("pending transaction: {}", transactionHash);
    }

    @Override
    public void block(EthBlock.Block block) {
        BigInteger blockNumber = block.getNumber();
        LOGGER.debug("block number: {}", blockNumber);
    }
}
