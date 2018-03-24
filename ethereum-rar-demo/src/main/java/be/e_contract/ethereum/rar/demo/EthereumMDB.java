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

// TODO: should find a cleaner way to refer to the RAR
@ResourceAdapter(value = "ethereum-rar-demo-deploy-1.0.0-SNAPSHOT.ear#ethereum-ra.rar")
@MessageDriven(messageListenerInterface = EthereumMessageListener.class, activationConfig = {
    @ActivationConfigProperty(propertyName = "nodeLocation", propertyValue = "http://localhost:8545")
})
public class EthereumMDB implements EthereumMessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumMDB.class);

    @Override
    public void pendingTransaction(String transactionHash) {
        LOGGER.debug("pending transaction: {}", transactionHash);
    }

    @Override
    public void block(BigInteger blockNumber) {
        LOGGER.debug("block number: {}", blockNumber);
    }
}
