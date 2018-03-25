/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import javax.resource.spi.work.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumRunnableWork implements Work {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumRunnableWork.class);

    private final Runnable runnable;

    public EthereumRunnableWork(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void release() {
        LOGGER.debug("release");
    }

    @Override
    public void run() {
        try {
            this.runnable.run();
        } catch (Throwable e) {
            // we don't receive anything on a failing node...
            // TODO: we have to run the filters manually I guess...
            // will probably be the only way to detect this
            LOGGER.error("run error: " + e.getMessage(), e);
        }
    }
}
