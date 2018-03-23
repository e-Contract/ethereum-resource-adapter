/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.rar.demo;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.jboss.ejb3.annotation.ResourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MessageDriven(name = "EthereumDemoMDB", activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/queue/EthereumDemo")})
@ResourceAdapter("ethereum-ra.rar")
public class EthereumDemoMDB implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumDemoMDB.class);

    @Override
    public void onMessage(Message message) {
        LOGGER.debug("onMessage");
    }
}
