/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import be.e_contract.ethereum.ra.api.EthereumMessageListener;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;

public class EthereumWork implements Work {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumWork.class);

    private final EthereumActivationSpec ethereumActivationSpec;

    private final WorkManager workManager;

    private final EthereumMessageListener ethereumMessageListener;

    private Web3j web3j;

    public EthereumWork(EthereumMessageListener ethereumMessageListener, EthereumActivationSpec ethereumActivationSpec, WorkManager workManager) {
        this.ethereumMessageListener = ethereumMessageListener;
        this.ethereumActivationSpec = ethereumActivationSpec;
        this.workManager = workManager;
    }

    @Override
    public void release() {
        LOGGER.debug("release");
    }

    public void shutdown() {
        LOGGER.debug("shutdown");
        if (this.web3j != null) {
            this.web3j.shutdown();
            this.web3j = null;
        }
    }

    @Override
    public void run() {
        LOGGER.debug("run");
        String nodeLocation = this.ethereumActivationSpec.getNodeLocation();
        LOGGER.debug("node location: {}", nodeLocation);
        int cpuCount = Runtime.getRuntime().availableProcessors();
        ScheduledExecutorService scheduledExecutorService
                = Executors.newScheduledThreadPool(cpuCount, new EthereumThreadFactory(this.workManager));
        try {
            this.web3j = Web3jFactory.createWeb3j(nodeLocation, scheduledExecutorService);
        } catch (IOException ex) {
            LOGGER.error("error creating web3j client: {}", ex.getMessage(), ex);
            return;
        }
        Boolean deliverPending = this.ethereumActivationSpec.getDeliverPending();
        if (null == deliverPending) {
            deliverPending = false;
        }
        if (deliverPending) {
            // TODO: manually use the filters here, to be able to detect a down node.
            this.web3j.pendingTransactionObservable().subscribe(tx -> {
                try {
                    this.ethereumMessageListener.pendingTransaction(tx);
                } catch (Exception e) {
                    LOGGER.error("MDB exception: " + e.getMessage(), e);
                }
            }, error -> {
                LOGGER.error("error: " + error.getMessage(), error);
            });
        }
        // full block should be activation parameter
        Boolean fullBlock = this.ethereumActivationSpec.getFullBlock();
        if (null == fullBlock) {
            fullBlock = false;
        }
        Boolean deliverBlock = this.ethereumActivationSpec.getDeliverBlock();
        if (null == deliverBlock) {
            deliverBlock = false;
        }
        if (deliverBlock) {
            LOGGER.debug("full block: {}", fullBlock);
            this.web3j.blockObservable(fullBlock).subscribe(ethBlock -> {
                EthBlock.Block block = ethBlock.getBlock();
                try {
                    this.ethereumMessageListener.block(block);
                } catch (Exception e) {
                    LOGGER.error("MDB exception: " + e.getMessage(), e);
                }
            }, error -> {
                LOGGER.error("error: " + error.getMessage(), error);
            });
        }
    }
}
