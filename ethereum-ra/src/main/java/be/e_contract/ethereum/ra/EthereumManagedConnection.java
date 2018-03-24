/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Frank Cornelis
 */
public class EthereumManagedConnection implements ManagedConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumManagedConnection.class);

    private final Set<ConnectionEventListener> listeners;

    public EthereumManagedConnection() {
        LOGGER.debug("constructor");
        this.listeners = new HashSet<>();
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        LOGGER.debug("getConnection");
        return new EthereumConnectionImpl();
    }

    @Override
    public void destroy() throws ResourceException {
        LOGGER.debug("destroy");
    }

    @Override
    public void cleanup() throws ResourceException {
        LOGGER.debug("cleanup");
        throw new UnsupportedOperationException();
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        LOGGER.debug("associateConnection");
        throw new UnsupportedOperationException();
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        LOGGER.debug("addConnectionEventListener");
        this.listeners.add(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        LOGGER.debug("removeConnectionEventListener");
        this.listeners.remove(listener);
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        LOGGER.debug("getXAResource");
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        LOGGER.debug("getLocalTransaction");
        throw new UnsupportedOperationException();
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        LOGGER.debug("getMetadata");
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
}
