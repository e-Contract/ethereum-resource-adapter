/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2019 e-Contract.be BVBA.
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
package test.integ.be.e_contract.ethereum.ra;

import be.e_contract.ethereum.ra.api.EthereumMessageListener;
import java.util.Date;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.UserTransaction;
import org.jboss.ejb3.annotation.ResourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "deliverPending", propertyValue = "true")
    ,
    @ActivationConfigProperty(propertyName = "deliverBlock", propertyValue = "true")
})
@ResourceAdapter("#ethereum-ra.rar")
@TransactionManagement(TransactionManagementType.BEAN)
public class RollbackMDB implements EthereumMessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RollbackMDB.class);

    @Resource
    private UserTransaction userTransaction;

    @Override
    public void block(String blockHash, Date timestamp) throws Exception {
        LOGGER.debug("block hash: {}", blockHash);
    }

    @Override
    public void pendingTransaction(String transactionHash, Date timestamp) throws Exception {
        LOGGER.debug("pending transaction hash: {}", transactionHash);
        this.userTransaction.begin();
        this.userTransaction.rollback();
    }

    @Override
    public void connectionStatus(boolean connected) throws Exception {
        LOGGER.debug("connection status: {}", connected);
    }
}
