/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018-2021 e-Contract.be BV.
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
package be.e_contract.ethereum.ra.web3j;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.NetPeerCount;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.utils.Async;

public class Web3jFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(Web3jFactory.class);

    public static Admin createWeb3j(String nodeLocation) throws IOException {
        Web3jService service = getWeb3jService(nodeLocation);
        Admin web3j = Admin.build(service, 50, Async.defaultExecutorService());
        NetPeerCount netPeerCount = web3j.netPeerCount().send();
        if (netPeerCount.hasError()) {
            LOGGER.error("net peer count error: {}", netPeerCount.getError().getMessage());
            throw new IOException("net peer count error: " + netPeerCount.getError());
        }
        BigInteger peerCount = netPeerCount.getQuantity();
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
            Instant timestampInstant = Instant.ofEpochMilli(timestamp.multiply(BigInteger.valueOf(1000)).longValue());
            LocalDateTime timestampDateTime = timestampInstant.atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime now = LocalDateTime.now();
            // 1 minute is not enough for production
            if (timestampDateTime.plusMinutes(2).isBefore(now)) {
                LOGGER.warn("Latest block is more than 2 minutes old.");
                LOGGER.warn("Node might be out-of-sync.");
                LOGGER.warn("Results might be inaccurate.");
            }
        }
        return web3j;
    }

    public static Web3jService getWeb3jService(String nodeLocation) {
        if (nodeLocation.startsWith("http")) {
            return new HttpService(nodeLocation);
        } else {
            // https://github.com/web3j/web3j/pull/245
            LOGGER.warn("web3j IPC is not really stable");
            return new UnixIpcService(nodeLocation);
        }
    }
}
