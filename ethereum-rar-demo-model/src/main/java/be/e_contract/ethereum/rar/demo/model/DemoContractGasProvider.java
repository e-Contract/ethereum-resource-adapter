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
package be.e_contract.ethereum.rar.demo.model;

import be.e_contract.ethereum.ra.api.EthereumConnection;
import be.e_contract.ethereum.ra.api.EthereumConnectionFactory;
import be.e_contract.ethereum.ra.api.EthereumException;
import be.e_contract.ethereum.ra.oracle.api.GasPriceOracle;
import be.e_contract.ethereum.ra.oracle.api.UnknownGasPriceOracleException;
import java.math.BigInteger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.tx.gas.ContractEIP1559GasProvider;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

@Stateless
@Local(ContractGasProvider.class)
public class DemoContractGasProvider implements ContractEIP1559GasProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoContractGasProvider.class);

    @EJB
    private GasPriceOracle gasPriceOracleBean;

    @Resource(name = "EthereumConnectionFactory")
    private EthereumConnectionFactory ethereumConnectionFactory;

    @Override
    public BigInteger getGasPrice(String contractFunc) {
        try {
            return this.gasPriceOracleBean.getGasPrice("default", Integer.MAX_VALUE);
        } catch (UnknownGasPriceOracleException ex) {
            return DefaultGasProvider.GAS_PRICE;
        }
    }

    @Override
    public BigInteger getGasPrice() {
        try {
            return this.gasPriceOracleBean.getGasPrice("default", Integer.MAX_VALUE);
        } catch (UnknownGasPriceOracleException ex) {
            return DefaultGasProvider.GAS_PRICE;
        }
    }

    @Override
    public BigInteger getGasLimit(String contractFunc) {
        return DefaultGasProvider.GAS_LIMIT;
    }

    @Override
    public BigInteger getGasLimit() {
        return DefaultGasProvider.GAS_LIMIT;
    }

    @Override
    public boolean isEIP1559Enabled() {
        LOGGER.debug("isEIP1559Enabled");
        return true;
    }

    @Override
    public long getChainId() {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.getChainId();
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return -1;
        } catch (EthereumException ex) {
            LOGGER.error("ethereum error: " + ex.getMessage(), ex);
            return -1;
        }
    }

    @Override
    public BigInteger getMaxFeePerGas(String contractFunc) {
        try {
            return this.gasPriceOracleBean.getMaxFeePerGas("default");
        } catch (UnknownGasPriceOracleException ex) {
            try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
                return ethereumConnection.getGasPrice().multiply(BigInteger.valueOf(2));
            } catch (ResourceException ex2) {
                LOGGER.error("JCA error: " + ex2.getMessage(), ex2);
                return null;
            }
        }
    }

    @Override
    public BigInteger getMaxPriorityFeePerGas(String contractFunc) {
        try {
            return this.gasPriceOracleBean.getMaxPriorityFeePerGas("default", null);
        } catch (UnknownGasPriceOracleException ex) {
            try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
                return ethereumConnection.getMaxPriorityFeePerGas();
            } catch (ResourceException ex2) {
                LOGGER.error("JCA error: " + ex2.getMessage(), ex2);
                return null;
            }
        }
    }
}
