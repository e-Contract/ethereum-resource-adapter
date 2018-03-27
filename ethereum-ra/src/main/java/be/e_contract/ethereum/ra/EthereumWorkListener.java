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
package be.e_contract.ethereum.ra;

import be.e_contract.ethereum.ra.api.EthereumMessageListener;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumWorkListener implements WorkListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumWorkListener.class);

    private final Work work;

    private final EthereumWork ethereumWork;

    private boolean disconnected;

    public EthereumWorkListener(Work work, EthereumWork ethereumWork) {
        this.work = work;
        this.ethereumWork = ethereumWork;
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
    public void workStarted(WorkEvent e) {
        LOGGER.debug("workStarted");
        if (this.disconnected) {
            this.disconnected = false;
            for (EthereumActivationSpec ethereumActivationSpec : this.ethereumWork.getEthereumActivationSpecs()) {
                try {
                    EthereumMessageListener messageListener = ethereumActivationSpec.getEthereumMessageListener();
                    messageListener.connectionStatus(!this.disconnected);
                } catch (Exception ex) {
                    LOGGER.error("messaging error: {}", ex.getMessage(), ex);
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
        boolean shutdown = this.ethereumWork.isShutdown();
        if (shutdown) {
            return;
        }
        LOGGER.warn("disconnected from Ethereum node: {}", this.ethereumWork.getNodeLocation());
        // else we try to reconnect
        this.disconnected = true;
        for (EthereumActivationSpec ethereumActivationSpec : this.ethereumWork.getEthereumActivationSpecs()) {
            try {
                EthereumMessageListener messageListener = ethereumActivationSpec.getEthereumMessageListener();
                messageListener.connectionStatus(!this.disconnected);
            } catch (Exception ex) {
                LOGGER.error("messaging error: {}", ex.getMessage(), ex);
            }
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            shutdown = this.ethereumWork.isShutdown();
            if (shutdown) {
                return;
            }
            LOGGER.error("sleep error: " + e.getMessage(), e);
        }
        LOGGER.warn("trying to reconnect to Ethereum node: {}", this.ethereumWork.getNodeLocation());
        WorkManager workManager = this.ethereumWork.getWorkManager();
        try {
            workManager.scheduleWork(this.work, WorkManager.INDEFINITE, null, this);
        } catch (WorkException ex) {
            LOGGER.debug("work error: {}", ex.getMessage(), ex);
        }
    }
}
