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
package be.e_contract.ethereum.ra.inflow;

import be.e_contract.ethereum.ra.api.EthereumMessageListener;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumWorkListener implements WorkListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumWorkListener.class);

    private boolean disconnected;

    private final WorkManager workManager;

    public EthereumWorkListener(WorkManager workManager) {
        this.workManager = workManager;
    }

    @Override
    public void workAccepted(WorkEvent e) {
        LOGGER.debug("workAccepted");
    }

    @Override
    public void workRejected(WorkEvent e) {
        LOGGER.debug("workRejected");
    }

    @Override
    public void workStarted(WorkEvent workEvent) {
        LOGGER.debug("workStarted");
        if (this.disconnected) {
            this.disconnected = false;
            EthereumWork ethereumWork = (EthereumWork) workEvent.getWork();
            for (EthereumActivationSpec ethereumActivationSpec : ethereumWork.getEthereumActivationSpecs()) {
                try {
                    EthereumMessageListener messageListener = ethereumActivationSpec.getEthereumMessageListener();
                    messageListener.connectionStatus(!this.disconnected);
                } catch (Exception ex) {
                    LOGGER.error("messaging error: {}", ex.getMessage(), ex);
                    LOGGER.debug("error type: {}", ex.getClass().getName());
                }
            }
        }
    }

    @Override
    public void workCompleted(WorkEvent workEvent) {
        LOGGER.debug("workCompleted");
        LOGGER.debug("work event: {}", workEvent);
        LOGGER.debug("has work exception: {}", workEvent.getException() != null);
        LOGGER.debug("work exception: {}", workEvent.getException());
        LOGGER.debug("event type: {}", workEvent.getType());
        EthereumWork ethereumWork = (EthereumWork) workEvent.getWork();
        boolean shutdown = ethereumWork.isShutdown();
        if (shutdown) {
            return;
        }
        LOGGER.warn("disconnected from Ethereum node: {}", ethereumWork.getNodeLocation());
        // else we try to reconnect
        this.disconnected = true;
        for (EthereumActivationSpec ethereumActivationSpec : ethereumWork.getEthereumActivationSpecs()) {
            try {
                EthereumMessageListener messageListener = ethereumActivationSpec.getEthereumMessageListener();
                messageListener.connectionStatus(!this.disconnected);
            } catch (Exception ex) {
                LOGGER.error("messaging error: {}", ex.getMessage(), ex);
                LOGGER.debug("error type: {}", ex.getClass().getName());
            }
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            shutdown = ethereumWork.isShutdown();
            if (shutdown) {
                return;
            }
            LOGGER.error("sleep error: " + e.getMessage(), e);
        }
        if (ethereumWork.isShutdown()) {
            return;
        }
        LOGGER.warn("trying to reconnect to Ethereum node: {}", ethereumWork.getNodeLocation());
        try {
            this.workManager.scheduleWork(ethereumWork, WorkManager.INDEFINITE, null, this);
        } catch (WorkException ex) {
            LOGGER.debug("work error: {}", ex.getMessage(), ex);
        }
    }
}
