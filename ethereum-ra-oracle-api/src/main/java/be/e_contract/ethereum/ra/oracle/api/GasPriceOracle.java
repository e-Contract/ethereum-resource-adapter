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
package be.e_contract.ethereum.ra.oracle.api;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;

/**
 * EJB3 local interface for the gas price oracle.
 *
 * @author Frank Cornelis
 */
@Local
public interface GasPriceOracle {

    /**
     * Gives back a list of all available gas price oracles.
     *
     * @return list of gas price oracle names.
     */
    List<String> getGasPriceOracleNames();

    /**
     * Gives back all gas prices from the available gas price oracles.
     *
     * @param maxDuration maximum duration for transaction commit in seconds.
     * @return map with key oracle name and value the gas price in wei.
     */
    Map<String, BigInteger> getGasPrices(Integer maxDuration);

    /**
     * Gives back the gas price by a certain gas price oracle.
     *
     * @param oracle the name of the oracle to be used.
     * @param maxDuration maximum duration for transaction commit in seconds.
     * @return the gas price in wei.
     * @throws
     * be.e_contract.ethereum.ra.oracle.api.UnknownGasPriceOracleException
     */
    BigInteger getGasPrice(String oracle, Integer maxDuration) throws UnknownGasPriceOracleException;

    /**
     * Gives back the oracle maximum fee per gas.
     *
     * @param oracle the name of the oracle to be used.
     * @return the maximum fee per gas in wei.
     * @throws
     * be.e_contract.ethereum.ra.oracle.api.UnknownGasPriceOracleException
     */
    BigInteger getMaxFeePerGas(String oracle) throws UnknownGasPriceOracleException;

    /**
     * Gives back the maximum priority fee per gas as determined by the oracle,
     * given the maximum transaction duration.
     *
     * @param oracle the name of the oracle to be used.
     * @param maxDuration the optional maximum transaction duration.
     * @return the maximum priority fee per gas in wei.
     * @throws
     * be.e_contract.ethereum.ra.oracle.api.UnknownGasPriceOracleException
     */
    BigInteger getMaxPriorityFeePerGas(String oracle, Integer maxDuration) throws UnknownGasPriceOracleException;
}
