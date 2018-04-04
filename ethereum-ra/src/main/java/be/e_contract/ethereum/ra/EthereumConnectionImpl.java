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

import be.e_contract.ethereum.ra.api.EthereumConnection;
import be.e_contract.ethereum.ra.api.EthereumException;
import be.e_contract.ethereum.ra.api.TransactionConfirmation;
import java.math.BigInteger;
import java.util.List;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.Interaction;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.ResultSetInfo;
import javax.resource.spi.ConnectionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.tx.Contract;

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
        EthereumLocalTransaction ethereumLocalTransaction = (EthereumLocalTransaction) this.ethereumManagedConnection.getLocalTransaction();
        LocalTransaction localTransaction = new EthereumCCILocalTransaction(ethereumLocalTransaction);
        return localTransaction;
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

    @Override
    public Transaction findTransaction(String transactionHash) throws ResourceException {
        try {
            return this.ethereumManagedConnection.findTransaction(transactionHash);
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public EthBlock.Block getBlock(String blockHash, boolean returnFullTransactionObjects) throws ResourceException {
        try {
            return this.ethereumManagedConnection.getBlock(blockHash, returnFullTransactionObjects);
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public BigInteger getBalance(String address) throws ResourceException {
        try {
            return this.ethereumManagedConnection.getBalance(address);
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public BigInteger getTransactionCount(String address) throws ResourceException {
        try {
            return this.ethereumManagedConnection.getTransactionCount(address);
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public List<String> getAccounts() throws ResourceException {
        try {
            return this.ethereumManagedConnection.getAccounts();
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public String newAccount(String password) throws ResourceException {
        try {
            return this.ethereumManagedConnection.newAccount(password);
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public boolean unlockAccount(String account, String password) throws ResourceException, EthereumException {
        try {
            return this.ethereumManagedConnection.unlockAccount(account, password);
        } catch (EthereumException ex) {
            LOGGER.error("ethereum error: " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public String sendAccountTransaction(String account, String to, BigInteger value,
            BigInteger gasPrice, BigInteger nonce) throws ResourceException, EthereumException {
        try {
            return this.ethereumManagedConnection.sendAccountTransaction(account, to, value, gasPrice, nonce);
        } catch (EthereumException ex) {
            LOGGER.error("ethereum error: " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public String deploy(Class<? extends Contract> contractClass, BigInteger gasPrice,
            BigInteger gasLimit, Credentials credentials) throws ResourceException, EthereumException {
        try {
            return this.ethereumManagedConnection.deploy(contractClass, gasPrice, gasLimit, credentials);
        } catch (EthereumException ex) {
            LOGGER.error("ethereum error: " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }
}
