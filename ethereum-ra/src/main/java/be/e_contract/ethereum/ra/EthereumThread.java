/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import org.slf4j.LoggerFactory;

public class EthereumThread extends Thread {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EthereumThread.class);

    private final WorkManager workManager;

    private final Runnable work;

    public EthereumThread(WorkManager workManager, Runnable work) {
        this.workManager = workManager;
        this.work = work;
    }

    @Override
    public synchronized void start() {
        super.start();
        try {
            this.workManager.startWork(new Work() {
                @Override
                public void release() {

                }

                @Override
                public void run() {
                    EthereumThread.this.work.run();
                }
            });
        } catch (WorkException ex) {
            LOGGER.error("work error: " + ex.getMessage(), ex);
        }
    }
}
