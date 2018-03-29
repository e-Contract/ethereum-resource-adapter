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
package be.e_contract.ethereum.ra.oracle;

import be.e_contract.ethereum.ra.api.EthereumConnectionFactory;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;

@Stateless
public class OracleEthereumConnectionFactoryBean {

    @Resource(name = "EthereumConnectionFactory")
    private EthereumConnectionFactory ethereumConnectionFactory;

    @Produces
    @OracleEthereumConnectionFactory
    public EthereumConnectionFactory getOracleEthereumConnectionFactory() {
        return this.ethereumConnectionFactory;
    }
}