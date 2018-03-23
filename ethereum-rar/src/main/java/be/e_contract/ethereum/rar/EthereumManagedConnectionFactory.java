/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.rar;

import java.io.PrintWriter;
import java.util.Set;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionDefinition;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.security.auth.Subject;

@ConnectionDefinition(connectionFactory = EthereumConnectionFactory.class,
        connectionFactoryImpl = EthereumConnectionFactoryImpl.class,
        connection = EthereumConnection.class,
        connectionImpl = EthereumConnectionImpl.class)
public class EthereumManagedConnectionFactory implements ManagedConnectionFactory, ResourceAdapterAssociation {

    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResourceAdapter getResourceAdapter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
        throw new UnsupportedOperationException();
    }
}
