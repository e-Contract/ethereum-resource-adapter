/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018-2019 e-Contract.be BVBA.
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
import be.e_contract.ethereum.ra.api.EthereumConnectionFactory;
import be.e_contract.ethereum.ra.api.EthereumConnectionSpec;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateless
public class ConnectionBean {

    @Resource(name = "EthereumConnectionFactory")
    private EthereumConnectionFactory ethereumConnectionFactory;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void connection() throws Exception {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            ethereumConnection.getGasPrice();
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void connectionError() throws Exception {
        EthereumConnectionSpec ethereumConnectionSpec = new EthereumConnectionSpec("http://localhost:1234");
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection(ethereumConnectionSpec)) {
            ethereumConnection.getGasPrice();
        }
    }
}
