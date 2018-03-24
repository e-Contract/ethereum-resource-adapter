/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.utils.Async;

public class Web3jFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(Web3jFactory.class);

    public static Web3j createWeb3j(String nodeLocation) {
        return createWeb3j(nodeLocation, Async.defaultExecutorService());
    }

    public static Web3j createWeb3j(String nodeLocation, ScheduledExecutorService scheduledExecutorService) {
        Web3jService service;
        if (nodeLocation.startsWith("http")) {
            service = new HttpService(nodeLocation);
        } else {
            // https://github.com/web3j/web3j/pull/245
            LOGGER.warn("web3j IPC is not really stable");
            service = new UnixIpcService(nodeLocation);
        }
        // poll every half second
        return Web3j.build(service, 500, scheduledExecutorService);
    }
}
