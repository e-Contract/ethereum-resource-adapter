/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import java.util.concurrent.ThreadFactory;
import javax.resource.spi.work.WorkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WorkManager based ThreadFactory for web3j usage.
 *
 * @author Frank Cornelis
 */
public class EthereumThreadFactory implements ThreadFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumThreadFactory.class);

    private final WorkManager workManager;

    public EthereumThreadFactory(WorkManager workManager) {
        LOGGER.debug("constructor");
        this.workManager = workManager;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        LOGGER.debug("newThread");
        return new EthereumThread(this.workManager, runnable);
    }
}
