/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2019 e-Contract.be BVBA.
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

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

public class EthereumTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumTest.class);

    @Test
    public void testNextNonce() throws Exception {
        Web3jService service = new HttpService();
        Web3j web3j = Web3j.build(service);
        String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
        LOGGER.debug("client version: {}", clientVersion);
        if (null != clientVersion && clientVersion.startsWith("Parity")) {
            String address = "0x00A289B43e1e4825DbEDF2a78ba60a640634DC40";
            BigInteger nextNonce = getParityNextNonce(service, address);
            LOGGER.debug("next nonce: {}", nextNonce);
        }
    }

    @Test
    public void testChainId() throws Exception {
        Web3jService service = new HttpService();
        Request<?, ChainIdResponse> request = new Request<>(
                "eth_chainId",
                null,
                service,
                ChainIdResponse.class);
        ChainIdResponse response = request.send();
        LOGGER.debug("chain id: {}", response.getChainId());
    }

    private BigInteger getParityNextNonce(Web3jService service, String address) throws IOException {
        Request<?, ParityNextNonce> request = new Request<>(
                "parity_nextNonce",
                Arrays.asList(address),
                service,
                ParityNextNonce.class);
        ParityNextNonce parityNextNonce = request.send();
        if (parityNextNonce.hasError()) {
            LOGGER.warn("parity next nonce error: {}", parityNextNonce.getError().getMessage());
            return null;
        }
        BigInteger nextNonce = request.send().getNextNonce();
        return nextNonce;
    }

    public static class ChainIdResponse extends Response<String> {

        public BigInteger getChainId() {
            return Numeric.decodeQuantity(getResult());
        }
    }

    public static class ParityNextNonce extends Response<String> {

        public BigInteger getNextNonce() {
            return Numeric.decodeQuantity(getResult());
        }
    }
}
