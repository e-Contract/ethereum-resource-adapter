/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumConnectionImpl implements EthereumConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumConnectionImpl.class);

    public EthereumConnectionImpl() {
        LOGGER.debug("constructor");
    }

    @Override
    public BigInteger getGasPrice() {
        LOGGER.debug("getGasPrice");
        return null;
    }
}
