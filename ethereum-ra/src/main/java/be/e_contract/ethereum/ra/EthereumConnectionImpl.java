/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import java.math.BigInteger;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.Interaction;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.ResultSetInfo;
import javax.resource.spi.ConnectionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumConnectionImpl implements EthereumConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumConnectionImpl.class);

    private final EthereumManagedConnection ethereumManagedConnection;

    public EthereumConnectionImpl(EthereumManagedConnection ethereumManagedConnection) {
        LOGGER.debug("constructor");
        this.ethereumManagedConnection = ethereumManagedConnection;
    }

    @Override
    public BigInteger getGasPrice() {
        LOGGER.debug("getGasPrice");
        return null;
    }

    @Override
    public BigInteger getGasPrice(int maxDuration) {
        LOGGER.debug("getGasPrice with max duration: {}", maxDuration);
        // TODO: later on we implement our own gas price oracle
        return getGasPrice();
    }

    @Override
    public Interaction createInteraction() throws ResourceException {
        LOGGER.debug("createInteraction");
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        LOGGER.debug("getLocalTransaction");
        throw new UnsupportedOperationException();
    }

    @Override
    public ConnectionMetaData getMetaData() throws ResourceException {
        LOGGER.debug("getMetaData");
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSetInfo getResultSetInfo() throws ResourceException {
        LOGGER.debug("getResultSetInfo");
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws ResourceException {
        LOGGER.debug("close");
        this.ethereumManagedConnection.fireConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED, null);
    }
}
