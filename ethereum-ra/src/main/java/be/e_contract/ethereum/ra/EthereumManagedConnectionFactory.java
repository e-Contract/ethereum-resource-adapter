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
package be.e_contract.ethereum.ra;

import be.e_contract.ethereum.ra.api.EthereumConnectionFactory;
import be.e_contract.ethereum.ra.api.EthereumConnection;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.Referenceable;
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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ConnectionDefinitions({
    @ConnectionDefinition(connectionFactory = EthereumConnectionFactory.class,
            connectionFactoryImpl = EthereumConnectionFactoryImpl.class,
            connection = EthereumConnection.class,
            connectionImpl = EthereumConnectionImpl.class)
})
public class EthereumManagedConnectionFactory implements ManagedConnectionFactory, ResourceAdapterAssociation, Serializable, Referenceable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumManagedConnectionFactory.class);

    private PrintWriter logWriter;

    private EthereumResourceAdapter resourceAdapter;

    private Reference reference;

    @ConfigProperty(type = String.class, description = "The location of the Ethereum client node.")
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
        return createConnectionFactory(null);
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
        EthereumManagedConnection ethereumManagedConnection = new EthereumManagedConnection(ethereumConnectionRequestInfo, this.resourceAdapter);
        try {
            ethereumManagedConnection.getClientVersion();
        } catch (Exception ex) {
            LOGGER.error("connection error?: " + ex.getMessage(), ex);
            // checking the connection here allows the application server to do an allocation retry
            // see also: ironjacamar.xml
            throw new ResourceException("connection error: " + ex.getMessage());
        }
        return ethereumManagedConnection;
    }

    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        LOGGER.debug("matchManagedConnections");
        if (connectionSet == null) {
            return null;
        }
        LOGGER.debug("connection set size: {}", connectionSet.size());
        LOGGER.debug("subject: {}", subject);
        LOGGER.debug("connection request info: {}", cxRequestInfo);
        Iterator iterator = connectionSet.iterator();
        while (iterator.hasNext()) {
            Object connection = iterator.next();
            if (!(connection instanceof EthereumManagedConnection)) {
                LOGGER.error("unexpected connection: {}", connection);
                continue;
            }
            EthereumManagedConnection ethereumManagedConnection = (EthereumManagedConnection) connection;
            if (ethereumManagedConnection.match(cxRequestInfo)) {
                LOGGER.debug("returning: {}", ethereumManagedConnection);
                return ethereumManagedConnection;
            }
        }
        LOGGER.warn("could not match connection: {}", cxRequestInfo);
        LOGGER.warn("connection set: {}", connectionSet);
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
        if (!(ra instanceof EthereumResourceAdapter)) {
            throw new ResourceException("incorrect resource adapter type: " + ra);
        }
        this.resourceAdapter = (EthereumResourceAdapter) ra;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        EthereumManagedConnectionFactory rhs = (EthereumManagedConnectionFactory) obj;
        return new EqualsBuilder()
                .append(this.nodeLocation, rhs.nodeLocation)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.nodeLocation).toHashCode();
    }

    @Override
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    @Override
    public Reference getReference() throws NamingException {
        if (null == this.reference) {
            throw new NamingException("reference has not been set");
        }
        return this.reference;
    }
}
