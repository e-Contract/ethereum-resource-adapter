/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018 e-Contract.be BVBA.
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
package be.e_contract.ethereum.ra.oracle;

import be.e_contract.ethereum.ra.oracle.spi.PendingTransactionEvent;
import be.e_contract.ethereum.ra.oracle.spi.LatestBlockEvent;
import be.e_contract.ethereum.ra.oracle.spi.ConnectionStatusEvent;
import be.e_contract.ethereum.ra.api.EthereumMessageListener;
import java.util.Date;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "fullBlock", propertyValue = "true"),
    @ActivationConfigProperty(propertyName = "deliverPending", propertyValue = "true"),
    @ActivationConfigProperty(propertyName = "deliverBlock", propertyValue = "true")
})
public class GasPriceOracleMDB implements EthereumMessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(GasPriceOracleMDB.class);

    @Inject
    private Event<PendingTransactionEvent> pendingTransactionEvent;

    @Inject
    private Event<LatestBlockEvent> latestBlockEvent;

    @Inject
    private Event<ConnectionStatusEvent> connectionStatusEvent;

    public GasPriceOracleMDB() {
        LOGGER.debug("constructor");
    }

    @Override
    public void pendingTransaction(String transactionHash, Date timestamp) throws Exception {
        PendingTransactionEvent pendingTransactionEvent = new PendingTransactionEvent(transactionHash, timestamp);
        this.pendingTransactionEvent.fire(pendingTransactionEvent);
    }

    @Override
    public void block(String blockHash, Date timestamp) throws Exception {
        LatestBlockEvent latestBlockEvent = new LatestBlockEvent(blockHash, timestamp);
        this.latestBlockEvent.fire(latestBlockEvent);
    }

    @Override
    public void connectionStatus(boolean connected) throws Exception {
        ConnectionStatusEvent connectionStatusEvent = new ConnectionStatusEvent(connected);
        this.connectionStatusEvent.fire(connectionStatusEvent);
    }
}
