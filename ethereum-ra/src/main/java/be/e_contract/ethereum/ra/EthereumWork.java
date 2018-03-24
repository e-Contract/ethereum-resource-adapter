/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
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

    private final MessageEndpointFactory endpointFactory;

    private Web3j web3j;

    public EthereumWork(MessageEndpointFactory endpointFactory, EthereumActivationSpec ethereumActivationSpec, WorkManager workManager) {
        this.endpointFactory = endpointFactory;
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
        this.web3j = Web3jFactory.createWeb3j(nodeLocation, scheduledExecutorService);
        this.web3j.pendingTransactionObservable().subscribe(tx -> {
            MessageEndpoint messageEndpoint;
            try {
                messageEndpoint = this.endpointFactory.createEndpoint(null);
            } catch (UnavailableException ex) {
                LOGGER.error("unavailable error: " + ex.getMessage(), ex);
                return;
            }
            EthereumMessageListener ethereumMessageListener = (EthereumMessageListener) messageEndpoint;
            ethereumMessageListener.pendingTransaction(tx);
        }, error -> {
            LOGGER.error("error: " + error.getMessage(), error);
        });
        // full block should be activation parameter
        Boolean fullBlock = this.ethereumActivationSpec.getFullBlock();
        if (null == fullBlock) {
            fullBlock = false;
        }
        LOGGER.debug("full block: {}", fullBlock);
        this.web3j.blockObservable(fullBlock).subscribe(ethBlock -> {
            EthBlock.Block block = ethBlock.getBlock();
            MessageEndpoint messageEndpoint;
            try {
                messageEndpoint = this.endpointFactory.createEndpoint(null);
            } catch (UnavailableException ex) {
                LOGGER.error("unavailable error: " + ex.getMessage(), ex);
                return;
            }
            EthereumMessageListener ethereumMessageListener = (EthereumMessageListener) messageEndpoint;
            ethereumMessageListener.block(block);
        }, error -> {
            LOGGER.error("error: " + error.getMessage(), error);
        });
    }
}