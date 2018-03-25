/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import be.e_contract.ethereum.ra.api.EthereumConnection;
import be.e_contract.ethereum.ra.api.TransactionConfirmation;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
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
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

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

    private EthereumLocalTransaction ethereumLocalTransaction;

    private PrintWriter logWriter;

    private EthereumXAResource ethereumXAResource;

    public EthereumManagedConnection(EthereumConnectionRequestInfo ethereumConnectionRequestInfo) {
        LOGGER.debug("constructor");
        this.listeners = new HashSet<>();
        this.ethereumConnectionRequestInfo = ethereumConnectionRequestInfo;
    }

    public Web3j getWeb3j() throws Exception {
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
        if (null != this.web3) {
            this.web3.shutdown();
        }
        if (this.ethereumLocalTransaction != null) {
            this.ethereumLocalTransaction = null;
        }
        if (null != this.ethereumXAResource) {
            this.ethereumXAResource = null;
        }
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
        if (null != this.ethereumXAResource) {
            return this.ethereumXAResource;
        }
        this.ethereumXAResource = new EthereumXAResource(this);
        return this.ethereumXAResource;
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        LOGGER.debug("getLocalTransaction");
        // for outbound only
        if (null != this.ethereumLocalTransaction) {
            return this.ethereumLocalTransaction;
        }
        this.ethereumLocalTransaction = new EthereumLocalTransaction(this);
        return this.ethereumLocalTransaction;
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        LOGGER.debug("getMetadata");
        return new EthereumManagedConnectionMetaData();
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

    public BigInteger getBlockNumber() throws ResourceException {
        Web3j web3j;
        try {
            web3j = getWeb3j();
        } catch (Exception ex) {
            LOGGER.error("error retrieving block number: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
        BigInteger blockNumber;
        try {
            blockNumber = web3j.ethBlockNumber().send().getBlockNumber();
        } catch (IOException ex) {
            LOGGER.error("error retrieving block number: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
        return blockNumber;
    }

    public String sendRawTransaction(String rawTransaction) throws ResourceException {
        LOGGER.debug("send raw transaction: {}", rawTransaction);
        // we want to support JCA transactions here
        if (null != this.ethereumXAResource) {
            LOGGER.debug("scheduling for XA transaction");
            this.ethereumXAResource.scheduleRawTransaction(rawTransaction);
            return null;
        }
        if (null != this.ethereumLocalTransaction) {
            LOGGER.debug("scheduling for local transaction");
            this.ethereumLocalTransaction.scheduleRawTransaction(rawTransaction);
            return null;
        }
        LOGGER.debug("directly sending transaction");
        Web3j web3j;
        try {
            web3j = getWeb3j();
        } catch (Exception ex) {
            LOGGER.error("web3j error: " + ex.getMessage(), ex);
            throw new ResourceException(ex);
        }
        web3j.ethSendRawTransaction(rawTransaction);
        return null;
    }

    public TransactionConfirmation getTransactionConfirmation(String transactionHash) throws Exception {
        Web3j web3j = getWeb3j();

        TransactionConfirmation transactionConfirmation = new TransactionConfirmation(transactionHash);

        EthGetTransactionReceipt getTransactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send();
        Optional<TransactionReceipt> transactionReceiptOptional = getTransactionReceipt.getTransactionReceipt();
        if (!transactionReceiptOptional.isPresent()) {
            LOGGER.debug("transaction receipt not available");
            EthBlock pendingEthBlock = web3j.ethGetBlockByNumber(DefaultBlockParameterName.PENDING, true).send();
            EthBlock.Block pendingBlock = pendingEthBlock.getBlock();
            boolean pendingTransaction = false;
            for (EthBlock.TransactionResult transactionResult : pendingBlock.getTransactions()) {
                EthBlock.TransactionObject transactionObject = (EthBlock.TransactionObject) transactionResult;
                Transaction transaction = transactionObject.get();
                if (transactionHash.equals(transaction.getHash())) {
                    pendingTransaction = true;
                    break;
                }
            }
            transactionConfirmation.setPendingTransaction(pendingTransaction);
            return transactionConfirmation;
        }
        TransactionReceipt transactionReceipt = transactionReceiptOptional.get();
        if (!"0x1".equals(transactionReceipt.getStatus())) {
            LOGGER.debug("Transaction has failed with status: " + transactionReceipt.getStatus());
            transactionConfirmation.setFailed(true);
            return transactionConfirmation;
        }
        String from = transactionReceipt.getFrom();
        String to = transactionReceipt.getTo();
        BigInteger transactionBlockNumber = transactionReceipt.getBlockNumber();
        BigInteger gasUsed = transactionReceipt.getGasUsed();

        EthBlock ethBlock = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(transactionBlockNumber), false).send();
        EthBlock.Block block = ethBlock.getBlock();

        BigInteger timestamp = block.getTimestamp();
        Date timestampDate = new Date(timestamp.multiply(BigInteger.valueOf(1000)).longValue());

        BigInteger latestBlockNumber = web3j.ethBlockNumber().send().getBlockNumber();
        // add one, since the transaction block also serves as confirmation
        BigInteger blocksOnTop = latestBlockNumber.subtract(transactionBlockNumber).add(BigInteger.ONE);

        transactionConfirmation.setInfo(from, to, transactionBlockNumber, gasUsed, blocksOnTop.longValueExact(), timestampDate);

        return transactionConfirmation;
    }
}
