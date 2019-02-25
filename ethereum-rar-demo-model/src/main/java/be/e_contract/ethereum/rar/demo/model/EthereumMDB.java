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
package be.e_contract.ethereum.rar.demo.model;

import be.e_contract.ethereum.ra.api.EthereumMessageListener;
import be.e_contract.ethereum.utils.EthereumTransactionManager;
import java.util.Date;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "deliverPending", propertyValue = "true")
    ,
    @ActivationConfigProperty(propertyName = "deliverBlock", propertyValue = "true")
})
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class EthereumMDB implements EthereumMessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumMDB.class);

    @EJB
    private EthereumBean ethereumBean;

    @Inject
    private EthereumTransactionManager ethereumTransactionManager;

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
        this.ethereumBean.getGasPrice(null, false);
        this.ethereumTransactionManager.block(blockHash);
    }

    @Override
    public void connectionStatus(boolean connected) throws Exception {
        LOGGER.debug("connected: {}", connected);
    }
}
