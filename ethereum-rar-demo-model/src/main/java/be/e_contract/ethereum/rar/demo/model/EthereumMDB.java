/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.rar.demo.model;

import be.e_contract.ethereum.ra.api.EthereumMessageListener;
import java.util.Date;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MessageDriven(activationConfig = {
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
    public void pendingTransaction(String transactionHash, Date timestamp) throws Exception {
        LOGGER.debug("pending transaction: {}", transactionHash);
    }

    @Override
    public void block(String blockHash, Date timestamp) throws Exception {
        LOGGER.debug("block hash: {}", blockHash);
    }

    @Override
    public void connectionStatus(boolean connected) throws Exception {
        LOGGER.debug("connected: {}", connected);
    }
}
