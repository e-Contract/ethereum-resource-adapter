/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import javax.resource.ResourceException;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.ConnectionDefinition;
import javax.resource.spi.ConnectionDefinitions;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.security.auth.Subject;
import org.apache.commons.lang.StringUtils;
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

    private PrintWriter logWriter;

    private EthereumResourceAdapter resourceAdapter;

    @ConfigProperty
    private String nodeLocation;

    public EthereumManagedConnectionFactory() {
        LOGGER.debug("constructor");
    }

    public String getNodeLocation() {
        return this.nodeLocation;
    }

    public void setNodeLocation(String nodeLocation) {
        this.nodeLocation = nodeLocation;
    }

    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        LOGGER.debug("createConnectionFactory(ConnectionManager)");
        return new EthereumConnectionFactoryImpl(this, cxManager);
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        LOGGER.debug("createConnectionFactory()");
        return new EthereumConnectionFactoryImpl(this, null);
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        LOGGER.debug("createManagedConnection(subject, connectionRequestInfo)");
        LOGGER.debug("subject: {}", subject);
        LOGGER.debug("connection request info: {}", cxRequestInfo);
        EthereumConnectionRequestInfo ethereumConnectionRequestInfo;
        if (null != cxRequestInfo) {
            ethereumConnectionRequestInfo = (EthereumConnectionRequestInfo) cxRequestInfo;
        } else if (StringUtils.isEmpty(this.nodeLocation)) {
            ethereumConnectionRequestInfo = new EthereumConnectionRequestInfo(this.resourceAdapter.getNodeLocation());
        } else {
            ethereumConnectionRequestInfo = new EthereumConnectionRequestInfo(this.nodeLocation);
        }
        return new EthereumManagedConnection(ethereumConnectionRequestInfo);
    }

    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        LOGGER.debug("matchManagedConnections");
        LOGGER.debug("connection set size: {}", connectionSet.size());
        LOGGER.debug("subject: {}", subject);
        LOGGER.debug("connection request info: {}", cxRequestInfo);
        Iterator iterator = connectionSet.iterator();
        while (iterator.hasNext()) {
            EthereumManagedConnection ethereumManagedConnection = (EthereumManagedConnection) iterator.next();
            if (ethereumManagedConnection.match(cxRequestInfo)) {
                LOGGER.debug("returning: {}", ethereumManagedConnection);
                return ethereumManagedConnection;
            }
        }
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        LOGGER.debug("setLogWriter");
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        LOGGER.debug("getLogWriter");
        return this.logWriter;
    }

    @Override
    public ResourceAdapter getResourceAdapter() {
        LOGGER.debug("getResourceAdapter");
        return this.resourceAdapter;
    }

    @Override
    public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
        LOGGER.debug("setResourceAdapter");
        this.resourceAdapter = (EthereumResourceAdapter) ra;
    }

    // implementation of equals and hashCode has to be provided
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
