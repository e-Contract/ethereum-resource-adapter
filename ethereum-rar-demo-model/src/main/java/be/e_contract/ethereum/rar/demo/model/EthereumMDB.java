/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.rar.demo.model;

import be.e_contract.ethereum.ra.api.EthereumMessageListener;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

@MessageDriven(messageListenerInterface = EthereumMessageListener.class, activationConfig = {
    @ActivationConfigProperty(propertyName = "nodeLocation", propertyValue = "http://localhost:8545"),
    @ActivationConfigProperty(propertyName = "fullBlock", propertyValue = "true"),
    @ActivationConfigProperty(propertyName = "deliverPending", propertyValue = "true"),
    @ActivationConfigProperty(propertyName = "deliverBlock", propertyValue = "true")
})
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class EthereumMDB implements EthereumMessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumMDB.class);

    public EthereumMDB() {
        LOGGER.debug("constructor");
    }

    @Override
    public void pendingTransaction(Transaction transaction) throws Exception {
    }

    @Override
    public void block(EthBlock.Block block) throws Exception {
        LOGGER.debug("block: {}", block.getNumber());
    }
}
