/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Set;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionDefinition;
import javax.resource.spi.ConnectionDefinitions;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.security.auth.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ConnectionDefinitions({
    @ConnectionDefinition(connectionFactory = EthereumConnectionFactory.class,
            connectionFactoryImpl = EthereumConnectionFactoryImpl.class,
            connection = EthereumConnection.class,
            connectionImpl = EthereumConnectionImpl.class)
})
public class EthereumManagedConnectionFactory implements ManagedConnectionFactory, ResourceAdapterAssociation, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumManagedConnectionFactory.class);

    public EthereumManagedConnectionFactory() {
        LOGGER.debug("constructor");
    }

    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        LOGGER.debug("createConnectionFactory");
        throw new UnsupportedOperationException();
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        LOGGER.debug("createConnectionFactory");
        throw new UnsupportedOperationException();
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        LOGGER.debug("createManagedConnection");
        throw new UnsupportedOperationException();
    }

    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        LOGGER.debug("matchManagedConnections");
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        LOGGER.debug("setLogWriter");
        throw new UnsupportedOperationException();
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        LOGGER.debug("getLogWriter");
        throw new UnsupportedOperationException();
    }

    @Override
    public ResourceAdapter getResourceAdapter() {
        LOGGER.debug("getResourceAdapter");
        throw new UnsupportedOperationException();
    }

    @Override
    public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
        LOGGER.debug("setResourceAdapter");
        throw new UnsupportedOperationException();
    }
}
