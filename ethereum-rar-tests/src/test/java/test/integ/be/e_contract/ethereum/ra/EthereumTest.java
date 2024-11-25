/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2019-2024 e-Contract.be BV.
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
package test.integ.be.e_contract.ethereum.ra;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.core.methods.response.TxPoolStatus;
import org.web3j.protocol.http.HttpService;

public class EthereumTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumTest.class);

    @Test
    @Disabled
    public void testPoolStatus() throws Exception {
        Web3jService service = new HttpService();
        Web3j web3j = Web3j.build(service);
        TxPoolStatus poolStatus = web3j.txPoolStatus().send();
        Integer pending = poolStatus.getPending();
        Integer queued = poolStatus.getQueued();
        LOGGER.debug("pending transactions: {}", pending);
        LOGGER.debug("queued transactions: {}", queued);
    }

    @Test
    public void testChainId() throws Exception {
        Web3jService service = new HttpService();
        Web3j web3j = Web3j.build(service);
        EthChainId ethChainId = web3j.ethChainId().send();
        LOGGER.debug("chain id: {}", ethChainId.getChainId());
    }
}
