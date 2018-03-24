/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumConnectionFactoryImpl implements EthereumConnectionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumConnectionFactoryImpl.class);

    private final ManagedConnectionFactory managedConnectionFactory;

    private final ConnectionManager connectionManager;

    private Reference reference;

    EthereumConnectionFactoryImpl(ManagedConnectionFactory managedConnectionFactory, ConnectionManager cxManager) {
        LOGGER.debug("constructor");
        this.managedConnectionFactory = managedConnectionFactory;
        this.connectionManager = cxManager;
    }

    @Override
    public EthereumConnection getConnection() throws ResourceException {
        return getConnection(null);
    }

    @Override
    public EthereumConnection getConnection(EthereumConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        LOGGER.debug("getConnection");
        return (EthereumConnection) this.connectionManager.allocateConnection(this.managedConnectionFactory, connectionRequestInfo);
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

}
