/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018-2024 e-Contract.be BV.
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

import be.e_contract.ethereum.ra.tx.EthereumCCILocalTransaction;
import be.e_contract.ethereum.ra.tx.EthereumLocalTransaction;
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
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.ContractGasProvider;

public class EthereumConnectionImpl implements EthereumConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumConnectionImpl.class);

    private EthereumManagedConnection ethereumManagedConnection;

    public EthereumConnectionImpl(EthereumManagedConnection ethereumManagedConnection) {
        LOGGER.debug("constructor");
        this.ethereumManagedConnection = ethereumManagedConnection;
    }

    public void checkIfClosed() throws ResourceException {
        if (null == this.ethereumManagedConnection) {
            throw new ResourceException("Connection is closed or invalid");
        }
    }

    @Override
    public BigInteger getGasPrice() throws ResourceException {
        LOGGER.debug("getGasPrice");
        checkIfClosed();
        BigInteger gasPrice = this.ethereumManagedConnection.getGasPrice();
        LOGGER.debug("gas price: {}", gasPrice);
        return gasPrice;
    }

    @Override
    public Interaction createInteraction() throws ResourceException {
        LOGGER.debug("createInteraction");
        throw new NotSupportedException("Interaction not supported");
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        LOGGER.debug("getLocalTransaction");
        checkIfClosed();
        EthereumLocalTransaction ethereumLocalTransaction
                = (EthereumLocalTransaction) this.ethereumManagedConnection.getLocalTransaction();
        LocalTransaction localTransaction = new EthereumCCILocalTransaction(ethereumLocalTransaction);
        return localTransaction;
    }

    @Override
    public ConnectionMetaData getMetaData() throws ResourceException {
        LOGGER.debug("getMetaData");
        checkIfClosed();
        return new EthereumConnectionMetaData();
    }

    @Override
    public ResultSetInfo getResultSetInfo() throws ResourceException {
        LOGGER.debug("getResultSetInfo");
        throw new NotSupportedException("ResultSetInfo not supported");
    }

    @Override
    public void close() throws ResourceException {
        LOGGER.debug("close");
        if (this.ethereumManagedConnection == null) {
            return;
        }
        this.ethereumManagedConnection.removeConnection(this);
        this.ethereumManagedConnection.fireConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED, null);
        this.ethereumManagedConnection = null;
    }

    void associateConnection(EthereumManagedConnection ethereumManagedConnection) throws ResourceException {
        LOGGER.debug("associateConnection");
        checkIfClosed();
        this.ethereumManagedConnection.removeConnection(this);
        ethereumManagedConnection.addConnection(this);
        this.ethereumManagedConnection = ethereumManagedConnection;
    }

    public void invalidate() {
        LOGGER.debug("invalidate");
        this.ethereumManagedConnection = null;
    }

    @Override
    public BigInteger getBlockNumber() throws ResourceException {
        checkIfClosed();
        return this.ethereumManagedConnection.getBlockNumber();
    }

    @Override
    public String sendRawTransaction(String rawTransaction) throws ResourceException, EthereumException {
        checkIfClosed();
        return this.ethereumManagedConnection.sendRawTransaction(rawTransaction);
    }

    @Override
    public TransactionConfirmation getTransactionConfirmation(String transactionHash) throws ResourceException {
        checkIfClosed();
        try {
            return this.ethereumManagedConnection.getTransactionConfirmation(transactionHash);
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public Transaction findTransaction(String transactionHash) throws ResourceException {
        checkIfClosed();
        try {
            return this.ethereumManagedConnection.findTransaction(transactionHash);
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public EthBlock.Block getBlock(String blockHash, boolean returnFullTransactionObjects) throws ResourceException {
        checkIfClosed();
        try {
            return this.ethereumManagedConnection.getBlock(blockHash, returnFullTransactionObjects);
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public BigInteger getBalance(String address) throws ResourceException {
        checkIfClosed();
        try {
            return this.ethereumManagedConnection.getBalance(address);
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public BigInteger getTransactionCount(String address) throws ResourceException {
        checkIfClosed();
        try {
            return this.ethereumManagedConnection.getTransactionCount(address);
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public List<String> getAccounts() throws ResourceException {
        checkIfClosed();
        try {
            return this.ethereumManagedConnection.getAccounts();
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public String sendAccountTransaction(String account, String to, BigInteger value,
            BigInteger gasPrice, BigInteger nonce) throws ResourceException, EthereumException {
        checkIfClosed();
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
    public TransactionReceipt deploy(Class<? extends Contract> contractClass, ContractGasProvider contractGasProvider,
            Credentials credentials, Long chainId) throws ResourceException, EthereumException {
        checkIfClosed();
        try {
            return this.ethereumManagedConnection.deploy(contractClass, contractGasProvider, credentials, chainId);
        } catch (EthereumException ex) {
            LOGGER.error("ethereum error: " + ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public <T extends Contract> T load(Class<T> contractClass, String contractAddress,
            Credentials credentials, Long chainId, ContractGasProvider contractGasProvider) throws ResourceException {
        checkIfClosed();
        try {
            return this.ethereumManagedConnection.load(contractClass, contractAddress, credentials,
                    chainId, contractGasProvider);
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public Long getChainId() throws ResourceException, EthereumException {
        checkIfClosed();
        try {
            return this.ethereumManagedConnection.getChainId();
        } catch (EthereumException ex) {
            LOGGER.error("ethereum error: " + ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public String sendTransaction(Credentials credentials, String to, BigInteger value,
            BigInteger gasPrice, Long chainId) throws ResourceException, EthereumException {
        checkIfClosed();
        try {
            return this.ethereumManagedConnection.sendTransaction(credentials, to, value, gasPrice, chainId);
        } catch (EthereumException ex) {
            LOGGER.error("ethereum error: " + ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public EthBlock.Block getBlock(DefaultBlockParameter defaultBlockParameter, boolean returnFullTransactionObjects) throws ResourceException {
        checkIfClosed();
        try {
            return this.ethereumManagedConnection.getBlock(defaultBlockParameter, returnFullTransactionObjects);
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public String getNetVersion() throws ResourceException {
        checkIfClosed();
        try {
            return this.ethereumManagedConnection.getNetVersion();
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public BigInteger getPeerCount() throws ResourceException {
        checkIfClosed();
        try {
            return this.ethereumManagedConnection.getPeerCount();
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public boolean isSyncing() throws ResourceException {
        checkIfClosed();
        try {
            return this.ethereumManagedConnection.isSyncing();
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public String getClientVersion() throws ResourceException {
        checkIfClosed();
        try {
            return this.ethereumManagedConnection.getClientVersion();
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public EthGetTransactionReceipt getTransactionReceipt(String transactionHash) throws ResourceException {
        checkIfClosed();
        try {
            return this.ethereumManagedConnection.getTransactionReceipt(transactionHash);
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
    }
}
