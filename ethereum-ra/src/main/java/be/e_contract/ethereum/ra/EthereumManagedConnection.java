/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;

/**
 *
 * @author Frank Cornelis
 */
public class EthereumManagedConnection implements ManagedConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumManagedConnection.class);

    private final Set<ConnectionEventListener> listeners;

    private final EthereumConnectionRequestInfo ethereumConnectionRequestInfo;

    private EthereumConnectionImpl ethereumConnection;

    private Web3j web3;

    public EthereumManagedConnection(EthereumConnectionRequestInfo ethereumConnectionRequestInfo) {
        LOGGER.debug("constructor");
        this.listeners = new HashSet<>();
        this.ethereumConnectionRequestInfo = ethereumConnectionRequestInfo;
    }

    private Web3j getWeb3j() throws Exception {
        if (this.web3 != null) {
            return this.web3;
        }
        String location;
        if (null == this.ethereumConnectionRequestInfo) {
            // TODO: get this from the resource adapter config
            location = "http://localhost:8545";
        } else {
            location = this.ethereumConnectionRequestInfo.getNodeLocation();
        }
        this.web3 = Web3jFactory.createWeb3j(location);
        return this.web3;
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        LOGGER.debug("getConnection");
        this.ethereumConnection = new EthereumConnectionImpl(this);
        return this.ethereumConnection;
    }

    @Override
    public void destroy() throws ResourceException {
        LOGGER.debug("destroy: {}", this.ethereumConnectionRequestInfo);
        if (null != this.ethereumConnection) {
            this.ethereumConnection.invalidate();
        }
        this.listeners.clear();
        this.web3.shutdown();
    }

    @Override
    public void cleanup() throws ResourceException {
        LOGGER.debug("cleanup: {}", this.ethereumConnectionRequestInfo);
        if (null != this.ethereumConnection) {
            this.ethereumConnection.invalidate();
        }
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        LOGGER.debug("associateConnection");
        EthereumConnectionImpl ethereumConnection = (EthereumConnectionImpl) connection;
        ethereumConnection.setManagedConnection(this);
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
        // for outbound only
        return new EthereumXAResource();
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        LOGGER.debug("getLocalTransaction");
        // for outbound only
        return new EthereumLocalTransaction();
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        LOGGER.debug("getMetadata");
        return new EthereumManagedConnectionMetaData();
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

    public void fireConnectionEvent(final EthereumConnection connection, final int eventId, final Exception exception) {
        ConnectionEvent event;
        if (exception == null) {
            event = new ConnectionEvent(this, eventId);
        } else {
            event = new ConnectionEvent(this, eventId, exception);
        }
        event.setConnectionHandle(connection);
        for (ConnectionEventListener listener : this.listeners) {
            switch (eventId) {
                case ConnectionEvent.CONNECTION_CLOSED:
                    listener.connectionClosed(event);
                    break;

                case ConnectionEvent.CONNECTION_ERROR_OCCURRED:
                    listener.connectionErrorOccurred(event);
                    break;

                case ConnectionEvent.LOCAL_TRANSACTION_COMMITTED:
                    listener.localTransactionCommitted(event);
                    break;

                case ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK:
                    listener.localTransactionRolledback(event);
                    break;

                case ConnectionEvent.LOCAL_TRANSACTION_STARTED:
                    listener.localTransactionStarted(event);
                    break;
            }
        }
    }

    public BigInteger getGasPrice() throws ResourceException {
        Web3j web3j;
        try {
            web3j = getWeb3j();
        } catch (Exception ex) {
            LOGGER.error("error retrieving gas price: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
        BigInteger gasPrice;
        try {
            gasPrice = web3j.ethGasPrice().send().getGasPrice();
        } catch (IOException ex) {
            LOGGER.error("error retrieving gas price: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
        return gasPrice;
    }

    public boolean match(ConnectionRequestInfo connectionRequestInfo) {
        if (null == this.ethereumConnectionRequestInfo && null == connectionRequestInfo) {
            return true;
        }
        if (connectionRequestInfo == null) {
            // TODO: should not be hardcoded
            connectionRequestInfo = new EthereumConnectionRequestInfo("http://localhost:8545");
        }
        return this.ethereumConnectionRequestInfo.equals(connectionRequestInfo);
    }
}
