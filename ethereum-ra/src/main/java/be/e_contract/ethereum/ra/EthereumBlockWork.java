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
import io.reactivex.Flowable;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.resource.spi.endpoint.MessageEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthSubscribe;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.protocol.websocket.events.NewHead;
import org.web3j.protocol.websocket.events.NewHeadsNotification;
import org.web3j.protocol.websocket.events.NotificationParams;

public class EthereumBlockWork extends EthereumWork {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumBlockWork.class);

    private static final Method BLOCK_METHOD;

    private boolean error;

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
            String nodeLocation = this.getNodeLocation();
            if (nodeLocation.startsWith("ws:")) {
                _runWebSocket(nodeLocation);
            } else {
                _run(nodeLocation);
            }
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
        }
    }

    public void _runWebSocket(String nodeLocation) throws Exception {
        this.error = false;
        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", "http://localhost");
        WebSocketClient webSocketClient = new WebSocketClient(new URI(nodeLocation), headers);
        WebSocketService webSocketService = new WebSocketService(webSocketClient, false);
        webSocketService.connect();
        Request<?, EthSubscribe> subscribeRequest = new Request<>(
                "eth_subscribe",
                Arrays.asList("newHeads"),
                webSocketService,
                EthSubscribe.class);

        Flowable<NewHeadsNotification> events = webSocketService.subscribe(
                subscribeRequest,
                "eth_unsubscribe",
                NewHeadsNotification.class
        );

        events.subscribe(event -> {
            Date timestamp = new Date();
            NotificationParams<NewHead> params = event.getParams();
            NewHead newHead = params.getResult();
            String blockHash = newHead.getHash();
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
                    LOGGER.warn("omitSyncing not supported for web socket connections");
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
        }, error -> {
            LOGGER.error("web socket error: " + error.getMessage(), error);
            this.error = true;
        });
        while (!this.isShutdown() && !this.error) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                if (!this.isShutdown()) {
                    LOGGER.error("sleep error: " + e.getMessage(), e);
                }
            }
        }
    }

    public void _run(String nodeLocation) throws Exception {
        Web3j web3j = Web3jFactory.createWeb3j(nodeLocation);
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
