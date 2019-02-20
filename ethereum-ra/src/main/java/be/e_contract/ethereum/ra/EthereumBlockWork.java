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
package be.e_contract.ethereum.ra;

import be.e_contract.ethereum.ra.api.EthereumMessageListener;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import javax.resource.spi.endpoint.MessageEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthLog;

public class EthereumBlockWork extends EthereumWork {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumBlockWork.class);

    private static final Method BLOCK_METHOD;

    static {
        try {
            BLOCK_METHOD = EthereumMessageListener.class.getMethod("block", String.class, Date.class);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    public EthereumBlockWork(String nodeLocation) {
        super(nodeLocation);
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
        Web3j web3j = Web3jFactory.createWeb3j(this.getNodeLocation());
        BigInteger filterId = web3j.ethNewBlockFilter().send().getFilterId();
        while (!this.isShutdown()) {
            List<EthLog.LogResult> logResultList
                    = web3j.ethGetFilterChanges(filterId).send().getResult();
            Date timestamp = new Date();
            if (logResultList.isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    if (!this.isShutdown()) {
                        LOGGER.error("sleep error: " + e.getMessage(), e);
                    }
                }
                continue;
            }
            for (EthLog.LogResult logResult : logResultList) {
                if (logResult instanceof EthLog.Hash) {
                    EthLog.Hash hash = (EthLog.Hash) logResult;
                    String blockHash = hash.get();
                    for (EthereumActivationSpec ethereumActivationSpec : this.getEthereumActivationSpecs()) {
                        Boolean deliverBlock = ethereumActivationSpec.isDeliverBlock();
                        if (null == deliverBlock) {
                            continue;
                        }
                        if (!deliverBlock) {
                            continue;
                        }
                        Boolean omitSyncing = ethereumActivationSpec.isOmitSyncing();
                        if (null != omitSyncing && omitSyncing) {
                            boolean syncing = web3j.ethSyncing().send().isSyncing();
                            if (syncing) {
                                continue;
                            }
                        }
                        EthereumMessageListener ethereumMessageListener
                                = ethereumActivationSpec.getEthereumMessageListener();
                        MessageEndpoint messageEndpoint = (MessageEndpoint) ethereumMessageListener;
                        messageEndpoint.beforeDelivery(BLOCK_METHOD);
                        try {
                            ethereumMessageListener.block(blockHash, timestamp);
                        } catch (Exception e) {
                            LOGGER.error("error invoking block: " + e.getMessage(), e);
                        }
                        messageEndpoint.afterDelivery();
                    }
                }
            }
        }
        // avoid NoClassDefFoundError here
        //web3j.ethUninstallFilter(filterId);
    }
}
