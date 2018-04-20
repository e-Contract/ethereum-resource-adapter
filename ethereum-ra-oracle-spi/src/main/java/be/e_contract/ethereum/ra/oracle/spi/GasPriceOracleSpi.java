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
package be.e_contract.ethereum.ra.oracle.spi;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Service Provider Interface for gas price oracles.
 *
 * @author Frank Cornelis
 */
public interface GasPriceOracleSpi extends Serializable {

    /**
     * Gives back the oracle gas price, given the maximum transaction duration.
     *
     * @param maxDuration the optional maximal transaction duration.
     * @return the gas price in wei.
     */
    BigInteger getGasPrice(Integer maxDuration);
}
