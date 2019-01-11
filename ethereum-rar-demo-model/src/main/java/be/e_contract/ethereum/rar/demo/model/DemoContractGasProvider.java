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
package be.e_contract.ethereum.rar.demo.model;

import be.e_contract.ethereum.ra.oracle.api.GasPriceOracle;
import be.e_contract.ethereum.ra.oracle.api.UnknownGasPriceOracleException;
import java.math.BigInteger;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

@Stateless
@Local(ContractGasProvider.class)
public class DemoContractGasProvider implements ContractGasProvider {

    @EJB
    private GasPriceOracle gasPriceOracleBean;

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
}
