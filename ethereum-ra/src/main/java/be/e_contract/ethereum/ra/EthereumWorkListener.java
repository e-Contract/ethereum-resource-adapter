/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

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
