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
package be.e_contract.ethereum.ra.api;

import javax.resource.ResourceException;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;

/**
 * Factory for Ethereum connections. The connections are Java EE JTA transaction
 * aware.
 *
 * @author Frank Cornelis
 * @see EthereumConnectionSpec
 * @see EthereumConnection
 */
public interface EthereumConnectionFactory extends ConnectionFactory {

    @Override
    EthereumConnection getConnection(ConnectionSpec properties) throws ResourceException;

    @Override
    EthereumConnection getConnection() throws ResourceException;
}
