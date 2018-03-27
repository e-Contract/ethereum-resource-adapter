/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import be.e_contract.ethereum.ra.api.EthereumMessageListener;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import javax.resource.spi.work.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthLog;

public class EthereumBlockWork implements Work {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumBlockWork.class);

    private final EthereumWork ethereumWork;

    private boolean shutdown;

    public EthereumBlockWork(EthereumWork ethereumWork) {
        this.ethereumWork = ethereumWork;
    }

    @Override
    public void release() {
        LOGGER.debug("release");
    }

    @Override
    public void run() {
        LOGGER.debug("run");
        try {
            _run();
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
        }
    }

    public void _run() throws Exception {
        String nodeLocation = this.ethereumWork.getNodeLocation();
        Web3j web3j = Web3jFactory.createWeb3j(nodeLocation);
        BigInteger filterId = web3j.ethNewBlockFilter().send().getFilterId();
        while (!this.shutdown) {
            List<EthLog.LogResult> logResultList = web3j.ethGetFilterChanges(filterId).send().getResult();
            Date timestamp = new Date();
            if (logResultList.isEmpty()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    if (!this.shutdown) {
                        LOGGER.error("sleep error: " + e.getMessage(), e);
                    }
                }
            }
            for (EthLog.LogResult logResult : logResultList) {
                if (logResult instanceof EthLog.Hash) {
                    EthLog.Hash hash = (EthLog.Hash) logResult;
                    String blockHash = hash.get();
                    LOGGER.debug("block hash: {}", blockHash);
                    List<EthereumActivationSpec> ethereumActivationSpecs = this.ethereumWork.getEthereumActivationSpecs();
                    for (EthereumActivationSpec ethereumActivationSpec : ethereumActivationSpecs) {
                        Boolean deliverBlock = ethereumActivationSpec.getDeliverBlock();
                        if (null == deliverBlock) {
                            continue;
                        }
                        if (!deliverBlock) {
                            continue;
                        }
                        EthereumMessageListener ethereumMessageListener = ethereumActivationSpec.getEthereumMessageListener();
                        ethereumMessageListener.block(blockHash, timestamp);
                    }
                }
            }
        }
        web3j.ethUninstallFilter(filterId);
    }

    public void shutdown() {
        LOGGER.debug("shutdown");
        this.shutdown = true;
    }
}