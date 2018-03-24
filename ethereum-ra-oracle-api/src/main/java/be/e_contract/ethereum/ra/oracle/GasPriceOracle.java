/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra.oracle;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Interface for gas price oracles.
 *
 * @author Frank Cornelis
 */
public interface GasPriceOracle extends Serializable {

    /**
     * Gives back the oracle gas price, given the maximum transaction duration.
     *
     * @param maxDuration
     * @return
     */
    BigInteger getGasPrice(int maxDuration);
}
