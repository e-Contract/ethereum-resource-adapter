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
package test.integ.be.e_contract.ethereum.ra;

import be.e_contract.ethereum.ra.api.EthereumConnection;
import be.e_contract.ethereum.ra.api.EthereumException;
import java.math.BigInteger;
import javax.resource.ResourceException;
import org.web3j.tx.gas.ContractEIP1559GasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

public class TestContractGasProvider implements ContractEIP1559GasProvider {

    private final EthereumConnection ethereumConnection;

    public TestContractGasProvider(EthereumConnection ethereumConnection) {
        this.ethereumConnection = ethereumConnection;
    }

    @Override
    public BigInteger getGasPrice(String contractFunc) {
        try {
            return this.ethereumConnection.getGasPrice();
        } catch (ResourceException ex) {
            return DefaultGasProvider.GAS_PRICE;
        }
    }

    @Override
    public BigInteger getGasPrice() {
        try {
            return this.ethereumConnection.getGasPrice();
        } catch (ResourceException ex) {
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
        return true;
    }

    @Override
    public long getChainId() {
        try {
            return this.ethereumConnection.getChainId();
        } catch (ResourceException | EthereumException ex) {
            return -1;
        }
    }

    @Override
    public BigInteger getMaxFeePerGas(String contractFunc) {
        try {
            return this.ethereumConnection.getGasPrice().multiply(BigInteger.valueOf(2));
        } catch (ResourceException ex2) {
            return null;
        }
    }

    @Override
    public BigInteger getMaxPriorityFeePerGas(String contractFunc) {
        try {
            return this.ethereumConnection.getMaxPriorityFeePerGas();
        } catch (ResourceException ex2) {
            return null;
        }
    }
}
