/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import be.e_contract.ethereum.ra.api.EthereumConnection;
import be.e_contract.ethereum.ra.api.TransactionConfirmation;
import java.math.BigInteger;
import javax.resource.NotSupportedException;
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

    private EthereumManagedConnection ethereumManagedConnection;

    private boolean valid;

    public EthereumConnectionImpl(EthereumManagedConnection ethereumManagedConnection) {
        LOGGER.debug("constructor");
        this.ethereumManagedConnection = ethereumManagedConnection;
        this.valid = true;
    }

    @Override
    public BigInteger getGasPrice() throws ResourceException {
        LOGGER.debug("getGasPrice");
        return this.ethereumManagedConnection.getGasPrice();
    }

    @Override
    public Interaction createInteraction() throws ResourceException {
        LOGGER.debug("createInteraction");
        throw new NotSupportedException();
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        LOGGER.debug("getLocalTransaction");
        // TODO
        throw new NotSupportedException();
    }

    @Override
    public ConnectionMetaData getMetaData() throws ResourceException {
        LOGGER.debug("getMetaData");
        if (this.valid) {
            return new EthereumConnectionMetaData();
        } else {
            throw new ResourceException();
        }
    }

    @Override
    public ResultSetInfo getResultSetInfo() throws ResourceException {
        LOGGER.debug("getResultSetInfo");
        throw new NotSupportedException();
    }

    @Override
    public void close() throws ResourceException {
        LOGGER.debug("close");
        this.ethereumManagedConnection.fireConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED, null);
    }

    void setManagedConnection(EthereumManagedConnection ethereumManagedConnection) {
        LOGGER.debug("setManagedConnection");
        this.ethereumManagedConnection = ethereumManagedConnection;
    }

    public void invalidate() {
        LOGGER.debug("invalidate");
        this.valid = false;
        this.ethereumManagedConnection = null;
    }

    @Override
    public BigInteger getBlockNumber() throws ResourceException {
        return this.ethereumManagedConnection.getBlockNumber();
    }

    @Override
    public String sendRawTransaction(String rawTransaction) throws ResourceException {
        return this.ethereumManagedConnection.sendRawTransaction(rawTransaction);
    }

    @Override
    public TransactionConfirmation getTransactionConfirmation(String transactionHash) throws ResourceException {
        try {
            return this.ethereumManagedConnection.getTransactionConfirmation(transactionHash);
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }
}
