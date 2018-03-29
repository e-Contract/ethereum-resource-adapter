/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018 e-Contract.be BVBA.
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

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.utils.Async;

public class Web3jFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(Web3jFactory.class);

    public static Web3j createWeb3j(String nodeLocation) throws IOException {
        Web3jService service;
        if (nodeLocation.startsWith("http")) {
            service = new HttpService(nodeLocation);
        } else {
            // https://github.com/web3j/web3j/pull/245
            LOGGER.warn("web3j IPC is not really stable");
            service = new UnixIpcService(nodeLocation);
        }
        // poll every half second
        Web3j web3j = Web3j.build(service, 500, Async.defaultExecutorService());
        BigInteger peerCount = web3j.netPeerCount().send().getQuantity();
        if (BigInteger.ZERO.equals(peerCount)) {
            LOGGER.warn("Node has no peers.");
            LOGGER.warn("Node probably just started.");
            LOGGER.warn("Results will be inaccurate!");
        }

        if (web3j.ethSyncing().send().isSyncing()) {
            LOGGER.warn("Node is still syncing.");
            LOGGER.warn("Results will be inaccurate!");
        } else {
            // not every node reports syncing status correctly, so also check latest block timestamp
            EthBlock.Block block = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().getBlock();
            BigInteger timestamp = block.getTimestamp();
            Date timestampDate = new Date(timestamp.multiply(BigInteger.valueOf(1000)).longValue());
            DateTime timestampDateTime = new DateTime(timestampDate);
            DateTime now = new DateTime();
            if (timestampDateTime.plusMinutes(1).isBefore(now)) {
                LOGGER.warn("latest block is more than 1 minute old.");
                LOGGER.warn("Node might be out-of-sync.");
                LOGGER.warn("Results might be inaccurate.");
            }
        }
        return web3j;
    }
}
