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
import io.reactivex.disposables.Disposable;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
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
import org.web3j.protocol.websocket.events.NotificationParams;
import org.web3j.protocol.websocket.events.PendingTransactionNotification;

public class EthereumPendingTransactionWork extends EthereumWork {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumPendingTransactionWork.class);

    private static final Method PENDING_TRANSACTION_METHOD;

    static {
        try {
            PENDING_TRANSACTION_METHOD = EthereumMessageListener.class.getMethod("pendingTransaction", String.class, Date.class);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    public EthereumPendingTransactionWork(String nodeLocation) {
        super(nodeLocation);
    }

    @Override
    public void run() {
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
        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", "http://localhost");
        WebSocketClient webSocketClient = new WebSocketClient(new URI(nodeLocation), headers);
        WebSocketService webSocketService = new WebSocketService(webSocketClient, false);
        webSocketService.connect();
        Request<?, EthSubscribe> subscribeRequest = new Request<>(
                "eth_subscribe",
                Arrays.asList("newPendingTransactions", Collections.emptyMap()),
                webSocketService,
                EthSubscribe.class);

        Flowable<PendingTransactionNotification> events = webSocketService.subscribe(
                subscribeRequest,
                "eth_unsubscribe",
                PendingTransactionNotification.class
        );

        events.subscribe(event -> {
            Date timestamp = new Date();
            NotificationParams<String> params = event.getParams();
            String transactionHash = params.getResult();
            for (EthereumActivationSpec ethereumActivationSpec : this.getEthereumActivationSpecs()) {
                Boolean deliverPending = ethereumActivationSpec.isDeliverPending();
                if (null == deliverPending) {
                    continue;
                }
                if (!deliverPending) {
                    continue;
                }
                // TODO: omitSyncing semantics
                EthereumMessageListener ethereumMessageListener
                        = ethereumActivationSpec.getEthereumMessageListener();
                MessageEndpoint messageEndpoint = (MessageEndpoint) ethereumMessageListener;
                messageEndpoint.beforeDelivery(PENDING_TRANSACTION_METHOD);
                try {
                    ethereumMessageListener.pendingTransaction(transactionHash, timestamp);
                } catch (Exception e) {
                    LOGGER.error("error invoking block: " + e.getMessage(), e);
                }
                messageEndpoint.afterDelivery();
            }
        });
        while (!this.isShutdown()) {
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
        BigInteger filterId = web3j.ethNewPendingTransactionFilter().send().getFilterId();
        while (!this.isShutdown()) {
            List<EthLog.LogResult> logResultList = web3j.ethGetFilterChanges(filterId).send().getResult();
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
                    String transactionHash = hash.get();
                    for (EthereumActivationSpec ethereumActivationSpec : this.getEthereumActivationSpecs()) {
                        Boolean deliverPending = ethereumActivationSpec.isDeliverPending();
                        if (null == deliverPending) {
                            continue;
                        }
                        if (!deliverPending) {
                            continue;
                        }
                        Boolean omitSyncing = ethereumActivationSpec.isOmitSyncing();
                        if (null != omitSyncing && omitSyncing) {
                            boolean syncing = web3j.ethSyncing().send().isSyncing();
                            if (syncing) {
                                continue;
                            }
                        }
                        EthereumMessageListener ethereumMessageListener = ethereumActivationSpec.getEthereumMessageListener();
                        MessageEndpoint messageEndpoint = (MessageEndpoint) ethereumMessageListener;
                        messageEndpoint.beforeDelivery(PENDING_TRANSACTION_METHOD);
                        try {
                            ethereumMessageListener.pendingTransaction(transactionHash, timestamp);
                        } catch (Exception e) {
                            LOGGER.error("error invoking pendingTransaction: " + e.getMessage(), e);
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
