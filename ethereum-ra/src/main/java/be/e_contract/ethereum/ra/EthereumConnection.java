/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import java.math.BigInteger;
import javax.resource.cci.Connection;

public interface EthereumConnection extends Connection, AutoCloseable {

    /**
     * Gives back the node gas price.
     *
     * @return
     */
    BigInteger getGasPrice();

    /**
     * Gives back a gas price taking into account to maximum duration for a
     * transaction to be accepted.
     *
     * @param maxDuration
     * @return
     */
    BigInteger getGasPrice(int maxDuration);
}
