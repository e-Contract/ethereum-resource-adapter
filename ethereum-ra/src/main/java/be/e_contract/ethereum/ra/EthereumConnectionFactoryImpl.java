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
package be.e_contract.ethereum.ra;

import be.e_contract.ethereum.ra.api.EthereumConnectionSpec;
import be.e_contract.ethereum.ra.api.EthereumConnectionFactory;
import be.e_contract.ethereum.ra.api.EthereumConnection;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumConnectionFactoryImpl implements EthereumConnectionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumConnectionFactoryImpl.class);

    private final ManagedConnectionFactory managedConnectionFactory;

    private final ConnectionManager connectionManager;

    private Reference reference;

    public EthereumConnectionFactoryImpl() {
        this(null, null);
        LOGGER.debug("default constructor");
    }

    public EthereumConnectionFactoryImpl(ManagedConnectionFactory managedConnectionFactory, ConnectionManager cxManager) {
        LOGGER.debug("constructor");
        this.managedConnectionFactory = managedConnectionFactory;
        this.connectionManager = cxManager;
    }

    @Override
    public EthereumConnection getConnection() throws ResourceException {
        return getConnection(null);
    }

    @Override
    public EthereumConnection getConnection(ConnectionSpec connectionSpec) throws ResourceException {
        LOGGER.debug("getConnection");
        EthereumConnectionRequestInfo ethereumConnectionRequestInfo;
        if (null != connectionSpec) {
            EthereumConnectionSpec ethereumConnectionSpec = (EthereumConnectionSpec) connectionSpec;
            ethereumConnectionRequestInfo = new EthereumConnectionRequestInfo(ethereumConnectionSpec.getNodeLocation());
        } else {
            ethereumConnectionRequestInfo = null;
        }
        return (EthereumConnection) this.connectionManager.allocateConnection(this.managedConnectionFactory, ethereumConnectionRequestInfo);
    }

    @Override
    public void setReference(Reference reference) {
        LOGGER.debug("setReference");
        this.reference = reference;
    }

    @Override
    public Reference getReference() throws NamingException {
        LOGGER.debug("getReference");
        return this.reference;
    }

    @Override
    public RecordFactory getRecordFactory() throws ResourceException {
        throw new NotSupportedException();
    }

    @Override
    public ResourceAdapterMetaData getMetaData() throws ResourceException {
        return new EthereumResourceAdapterMetaData();
    }
}
