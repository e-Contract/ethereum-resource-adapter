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
package test.integ.be.e_contract.ethereum.ra;

import be.e_contract.ethereum.ra.api.EthereumConnection;
import java.math.BigInteger;
import javax.resource.ResourceException;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

public class TestContractGasProvider implements ContractGasProvider {

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
}
