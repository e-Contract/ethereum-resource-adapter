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
package be.e_contract.ethereum.ra.oracle.node;

import be.e_contract.ethereum.ra.api.EthereumConnection;
import be.e_contract.ethereum.ra.api.EthereumConnectionFactory;
import be.e_contract.ethereum.ra.oracle.spi.GasPriceOracleSpi;
import be.e_contract.ethereum.ra.oracle.spi.GasPriceOracleType;
import be.e_contract.ethereum.ra.oracle.spi.OracleEthereumConnectionFactory;
import java.math.BigInteger;
import javax.inject.Inject;
import javax.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Most simple Gas Price Oracle based on the gas price of the client node.
 *
 * @author Frank Cornelis
 */
@GasPriceOracleType("node")
public class NodeGasPriceOracle implements GasPriceOracleSpi {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeGasPriceOracle.class);

    @Inject
    @OracleEthereumConnectionFactory
    private EthereumConnectionFactory ethereumConnectionFactory;

    @Override
    public BigInteger getGasPrice(Integer maxDuration) {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.getGasPrice();
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public BigInteger getMaxFeePerGas() {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            BigInteger gasPrice = ethereumConnection.getGasPrice();
            BigInteger maxFeePerGas = gasPrice.multiply(BigInteger.valueOf(2));
            return maxFeePerGas;
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public BigInteger getMaxPriorityFeePerGas(Integer maxDuration) {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.getMaxPriorityFeePerGas();
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }
}
