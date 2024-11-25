/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018-2024 e-Contract.be BV.
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
package be.e_contract.ethereum.rar.demo.model;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;

@Singleton
@Startup
public class MemoryKeysBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryKeysBean.class);

    private Map<String, ECKeyPair> keys;

    @PostConstruct
    public void postConstruct() {
        this.keys = new HashMap<>();
    }

    public List<String> getKeys() {
        return new LinkedList(this.keys.keySet());
    }

    public String newKey() throws Exception {
        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        String address = "0x" + Keys.getAddress(ecKeyPair);
        this.keys.put(address, ecKeyPair);
        return address;
    }

    public String signTransaction(BigInteger nonce, BigInteger gasPrice, String from, String to,
            BigInteger value, Long chainId, boolean eip1559, BigInteger maxPriorityFeePerGas) {
        RawTransaction rawTransaction;
        if (eip1559) {
            long chainIdentifier;
            if (null == chainId) {
                chainIdentifier = 1;
            } else {
                chainIdentifier = chainId;
            }
            // What eth_GasPrice returns exactly is very unclear. Some sources say base fee, others say base fee + priority fee.
            // https://github.com/ethereum/pm/issues/328
            // Geth's implementation currently:
            // eth_gasPrice after London will return the exact same number based on the total fees paid (tip + base)
            BigInteger baseFee = gasPrice.subtract(maxPriorityFeePerGas); // rough estimation
            // max fee = 2 * base fee + max priority fee gives you assurance for the next 6 blocks the be accepted
            BigInteger maxFeePerGas = baseFee.multiply(BigInteger.valueOf(2)).add(maxPriorityFeePerGas);
            rawTransaction = RawTransaction.createEtherTransaction(chainIdentifier, nonce, BigInteger.valueOf(21000),
                    to, value,
                    maxPriorityFeePerGas, maxFeePerGas);
        } else {
            rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice,
                    BigInteger.valueOf(21000), to, value);
        }
        ECKeyPair fromKeyPair = this.keys.get(from);
        if (null == fromKeyPair) {
            throw new RuntimeException("unvalid from");
        }
        Credentials credentials = Credentials.create(fromKeyPair);
        byte[] signedTransaction;
        if (null != chainId && !chainId.equals((byte) 0)) {
            LOGGER.debug("chain id: {}", chainId);
            signedTransaction = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
        } else {
            signedTransaction = TransactionEncoder.signMessage(rawTransaction, credentials);
        }
        String hexValue = Numeric.toHexString(signedTransaction);
        return hexValue;
    }

    public Credentials getCredentials(String from) {
        ECKeyPair fromKeyPair = this.keys.get(from);
        Credentials credentials = Credentials.create(fromKeyPair);
        return credentials;
    }
}
