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

import be.e_contract.ethereum.ra.tx.EthereumLocalTransaction;
import be.e_contract.ethereum.ra.tx.EthereumTransactionCommit;
import be.e_contract.ethereum.ra.tx.EthereumXAResource;
import be.e_contract.ethereum.ra.web3j.EthereumTransactionManager;
import be.e_contract.ethereum.ra.web3j.ParityNextNonce;
import be.e_contract.ethereum.ra.web3j.Web3jFactory;
import be.e_contract.ethereum.ra.api.EthereumConnection;
import be.e_contract.ethereum.ra.api.EthereumException;
import be.e_contract.ethereum.ra.api.TransactionConfirmation;
import be.e_contract.ethereum.ra.web3j.ChainIdResponse;
import be.e_contract.ethereum.ra.web3j.EthereumTransactionReceiptProcessor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.SignedRawTransaction;
import org.web3j.crypto.TransactionDecoder;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.ChainId;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Numeric;

/**
 *
 * @author Frank Cornelis
 */
public class EthereumManagedConnection implements ManagedConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumManagedConnection.class);

    private final Set<ConnectionEventListener> listeners;

    private final EthereumConnectionRequestInfo ethereumConnectionRequestInfo;

    private final Set<EthereumConnectionImpl> ethereumConnections;

    private Admin web3;

    private Web3jService service;

    private EthereumLocalTransaction ethereumLocalTransaction;

    private PrintWriter logWriter;

    private EthereumXAResource ethereumXAResource;

    private final EthereumResourceAdapter resourceAdapter;

    private boolean destroyed;

    private EthereumManagedConnectionMetaData metadata;

    public EthereumManagedConnection(EthereumConnectionRequestInfo ethereumConnectionRequestInfo,
            EthereumResourceAdapter resourceAdapter) {
        LOGGER.debug("constructor: {}", ethereumConnectionRequestInfo.getNodeLocation());
        this.listeners = new HashSet<>();
        this.ethereumConnectionRequestInfo = ethereumConnectionRequestInfo;
        this.resourceAdapter = resourceAdapter;
        this.ethereumConnections = new HashSet<>();
        this.destroyed = false;
    }

    public Admin getWeb3j() throws Exception {
        if (this.web3 != null) {
            return this.web3;
        }
        String location = this.ethereumConnectionRequestInfo.getNodeLocation();
        this.web3 = Web3jFactory.createWeb3j(location);
        return this.web3;
    }

    Web3jService getWeb3jService() {
        if (null != this.service) {
            return service;
        }
        String location = this.ethereumConnectionRequestInfo.getNodeLocation();
        this.service = Web3jFactory.getWeb3jService(location);
        return this.service;
    }

    void checkIfDestroyed() throws ResourceException {
        if (this.destroyed) {
            throw new ResourceException("Managed connection has been destroyed");
        }
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        LOGGER.debug("getConnection: {}", cxRequestInfo);
        if (null == cxRequestInfo) {
            LOGGER.debug("ConnectionRequestInfo is null");
        }
        checkIfDestroyed();
        EthereumConnectionImpl ethereumConnection = new EthereumConnectionImpl(this);
        this.ethereumConnections.add(ethereumConnection);
        return ethereumConnection;
    }

    @Override
    public void destroy() throws ResourceException {
        if (this.destroyed) {
            LOGGER.warn("already destroyed");
            return;
        }
        this.destroyed = true;
        LOGGER.debug("destroy: {}", this.ethereumConnectionRequestInfo);
        for (EthereumConnectionImpl ethereumConnection : this.ethereumConnections) {
            ethereumConnection.invalidate();
        }
        this.ethereumConnections.clear();
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
        checkIfDestroyed();
        LOGGER.debug("cleanup: {}", this.ethereumConnectionRequestInfo);
        for (EthereumConnectionImpl ethereumConnection : this.ethereumConnections) {
            ethereumConnection.invalidate();
        }
        this.ethereumConnections.clear();
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        LOGGER.debug("associateConnection");
        checkIfDestroyed();
        if (!(connection instanceof EthereumConnectionImpl)) {
            throw new ResourceException("invalid connection type");
        }
        EthereumConnectionImpl ethereumConnection = (EthereumConnectionImpl) connection;
        ethereumConnection.associateConnection(this);
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        LOGGER.debug("addConnectionEventListener: {}", listener);
        this.listeners.add(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        LOGGER.debug("removeConnectionEventListener: {}", listener);
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
        checkIfDestroyed();
        if (null == this.metadata) {
            this.metadata = new EthereumManagedConnectionMetaData(this);
        }
        return this.metadata;
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

    void fireConnectionEvent(EthereumConnection connection, int eventId, Exception exception) {
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
                    LOGGER.warn("connection error occurred: {}", connection);
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

    BigInteger getGasPrice() throws ResourceException {
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

    boolean match(ConnectionRequestInfo connectionRequestInfo) {
        if (null == this.ethereumConnectionRequestInfo && null == connectionRequestInfo) {
            return true;
        }
        if (connectionRequestInfo == null) {
            String nodeLocation = this.resourceAdapter.getNodeLocation();
            connectionRequestInfo = new EthereumConnectionRequestInfo(nodeLocation);
        }
        LOGGER.debug("this connection request info: {}", this.ethereumConnectionRequestInfo);
        LOGGER.debug("match connection request info: {}", connectionRequestInfo);
        return this.ethereumConnectionRequestInfo.equals(connectionRequestInfo);
    }

    BigInteger getBlockNumber() throws ResourceException {
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

    public String sendRawTransaction(String rawTransaction) throws ResourceException, EthereumException {
        LOGGER.debug("send raw transaction: {}", rawTransaction);
        RawTransaction decodedRawTransaction;
        try {
            decodedRawTransaction = TransactionDecoder.decode(rawTransaction);
        } catch (Exception ex) {
            LOGGER.error("transaction decoding error: " + ex.getMessage(), ex);
            throw new EthereumException("transaction decoding error: " + ex.getMessage());
        }
        if (!(decodedRawTransaction instanceof SignedRawTransaction)) {
            LOGGER.error("unsigned transaction");
            throw new EthereumException("unsigned transaction");
        }
        SignedRawTransaction signedRawTransaction = (SignedRawTransaction) decodedRawTransaction;
        LOGGER.debug("r size: {}", signedRawTransaction.getSignatureData().getR().length);
        LOGGER.debug("s size: {}", signedRawTransaction.getSignatureData().getS().length);
        String from;
        try {
            from = signedRawTransaction.getFrom();
        } catch (Exception ex) {
            LOGGER.error("transaction signature error: " + ex.getMessage(), ex);
            throw new EthereumException("transaction signature error: " + ex.getMessage());
        }
        LOGGER.debug("from: {}", from);
        LOGGER.debug("to: {}", signedRawTransaction.getTo());
        LOGGER.debug("nonce: {}", signedRawTransaction.getNonce());
        LOGGER.debug("chain id: {}", signedRawTransaction.getChainId());
        // we want to support JCA transactions here
        // important to check local transaction first because of CCI local transactions
        String transactionHash = Hash.sha3(rawTransaction);
        if (null != this.ethereumLocalTransaction) {
            LOGGER.debug("scheduling for local transaction");
            this.ethereumLocalTransaction.scheduleRawTransaction(rawTransaction);
            return transactionHash;
        }
        if (null != this.ethereumXAResource) {
            LOGGER.debug("scheduling for XA transaction");
            this.ethereumXAResource.scheduleRawTransaction(rawTransaction);
            return transactionHash;
        }
        LOGGER.debug("directly sending transaction");
        EthereumTransactionCommit ethereumTransactionCommit = new EthereumTransactionCommit(this);
        ethereumTransactionCommit.addRawTransaction(rawTransaction);
        ethereumTransactionCommit.commit();
        return transactionHash;
    }

    TransactionConfirmation getTransactionConfirmation(String transactionHash) throws Exception {
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
        if (!transactionReceipt.isStatusOK()) {
            LOGGER.debug("Transaction has failed with status: " + transactionReceipt.getStatus());
            transactionConfirmation.setFailed(true);
            return transactionConfirmation;
        }
        BigInteger transactionBlockNumber = transactionReceipt.getBlockNumber();

        EthBlock ethBlock = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(transactionBlockNumber), false).send();
        EthBlock.Block block = ethBlock.getBlock();

        BigInteger timestamp = block.getTimestamp();
        Date timestampDate = new Date(timestamp.multiply(BigInteger.valueOf(1000)).longValue());

        BigInteger latestBlockNumber = web3j.ethBlockNumber().send().getBlockNumber();
        // add one, since the transaction block also serves as confirmation
        BigInteger blocksOnTop = latestBlockNumber.subtract(transactionBlockNumber).add(BigInteger.ONE);

        transactionConfirmation.setInfo(transactionReceipt, blocksOnTop.longValueExact(), timestampDate);

        return transactionConfirmation;
    }

    Transaction findTransaction(String transactionHash) throws Exception {
        Web3j web3j = getWeb3j();
        Optional<Transaction> transactionOptional = web3j.ethGetTransactionByHash(transactionHash).send().getTransaction();
        if (!transactionOptional.isPresent()) {
            return null;
        }
        return transactionOptional.get();
    }

    EthBlock.Block getBlock(String blockHash, boolean returnFullTransactionObjects) throws Exception {
        Web3j web3j = getWeb3j();
        EthBlock.Block block = web3j.ethGetBlockByHash(blockHash, returnFullTransactionObjects).send().getBlock();
        return block;
    }

    BigInteger getBalance(String address) throws Exception {
        Web3j web3j = getWeb3j();
        BigInteger balance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance();
        return balance;
    }

    BigInteger getTransactionCount(String address) throws Exception {
        Web3j web3j = getWeb3j();
        BigInteger transactionCount = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send().getTransactionCount();
        return transactionCount;
    }

    BigInteger getParityNextNonce(String address) throws Exception {
        Web3j web3j = getWeb3j();
        String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
        LOGGER.debug("client version: {}", clientVersion);
        if (null == clientVersion) {
            return null;
        }
        if (!clientVersion.startsWith("Parity")) {
            return null;
        }
        Web3jService web3jService = getWeb3jService();
        Request<?, ParityNextNonce> request = new Request<>(
                "parity_nextNonce",
                Arrays.asList(address),
                web3jService,
                ParityNextNonce.class);
        ParityNextNonce parityNextNonce = request.send();
        if (parityNextNonce.hasError()) {
            LOGGER.warn("parity next nonce error: {}", parityNextNonce.getError().getMessage());
            return null;
        }
        BigInteger nextNonce = request.send().getNextNonce();
        return nextNonce;
    }

    public BigInteger getNextNonce(String address) throws Exception {
        Map<String, BigInteger> nonces = this.resourceAdapter.getNonces();
        synchronized (nonces) {
            BigInteger nonce = nonces.get(address);
            if (nonce != null) {
                nonces.put(address, nonce.add(BigInteger.ONE));
                return nonce;
            }
            nonce = getParityNextNonce(address);
            if (null == nonce) {
                Web3j web3j = getWeb3j();
                nonce = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send().getTransactionCount();
            }
            nonces.put(address, nonce.add(BigInteger.ONE));
            return nonce;
        }
    }

    List<String> getAccounts() throws Exception {
        Admin admin = getWeb3j();
        return admin.ethAccounts().send().getAccounts();
    }

    String newAccount(String password) throws Exception {
        Admin admin = getWeb3j();
        return admin.personalNewAccount(password).send().getAccountId();
    }

    boolean unlockAccount(String account, String password) throws Exception {
        Admin admin = getWeb3j();
        PersonalUnlockAccount personalUnlockAccount = admin.personalUnlockAccount(account, password).send();
        if (personalUnlockAccount.hasError()) {
            Response.Error error = personalUnlockAccount.getError();
            LOGGER.error("personal unlock account error: {}", error.getMessage());
            throw new EthereumException(error.getCode(), error.getMessage());
        }
        return personalUnlockAccount.accountUnlocked();
    }

    String sendAccountTransaction(String account, String to, BigInteger value, BigInteger gasPrice, BigInteger nonce) throws Exception {
        Web3j web3j = getWeb3j();
        BigInteger gasLimit = BigInteger.valueOf(21000);
        org.web3j.protocol.core.methods.request.Transaction transaction
                = org.web3j.protocol.core.methods.request.Transaction.createEtherTransaction(account, nonce, gasPrice, gasLimit, to, value);
        EthSendTransaction ethSendTransaction = web3j.ethSendTransaction(transaction).send();
        if (ethSendTransaction.hasError()) {
            Response.Error error = ethSendTransaction.getError();
            LOGGER.error("send transaction error: {}", error.getMessage());
            throw new EthereumException(error.getCode(), error.getMessage());
        }
        return ethSendTransaction.getTransactionHash();
    }

    TransactionReceipt deploy(Class<? extends Contract> contractClass, ContractGasProvider contractGasProvider,
            Credentials credentials, Long chainId) throws Exception {
        Web3j web3j = getWeb3j();
        Field binaryField = contractClass.getDeclaredField("BINARY");
        binaryField.setAccessible(true);
        String binary = (String) binaryField.get(null);
        binaryField.setAccessible(false);
        byte _chainId;
        if (null == chainId) {
            _chainId = ChainId.NONE;
        } else {
            _chainId = chainId.byteValue();
        }
        EthereumTransactionReceiptProcessor ethereumTransactionReceiptProcessor = new EthereumTransactionReceiptProcessor();
        TransactionManager transactionManager = new EthereumTransactionManager(this, credentials, _chainId, ethereumTransactionReceiptProcessor);
        Contract contract;
        try {
            contract = Contract.deployRemoteCall(contractClass, web3j, transactionManager, contractGasProvider, binary, "").send();
        } catch (Exception ex) {
            LOGGER.debug("could not deploy contract: " + ex.getMessage(), ex);
            throw new EthereumException("could not deploy contract: " + ex.getMessage());
        }
        return contract.getTransactionReceipt().get();
    }

    <T extends Contract> T load(Class<T> contractClass, String contractAddress,
            Credentials credentials, Long chainId, ContractGasProvider contractGasProvider) throws Exception {
        Method method = contractClass.getMethod("load", String.class, Web3j.class,
                TransactionManager.class, ContractGasProvider.class);
        Web3j web3j = getWeb3j();
        byte _chainId;
        if (null == chainId) {
            _chainId = ChainId.NONE;
        } else {
            _chainId = chainId.byteValue();
        }
        EthereumTransactionReceiptProcessor ethereumTransactionReceiptProcessor = new EthereumTransactionReceiptProcessor();
        TransactionManager transactionManager = new EthereumTransactionManager(this, credentials, _chainId, ethereumTransactionReceiptProcessor);
        T contract = (T) method.invoke(null, contractAddress, web3j, transactionManager, contractGasProvider);
        if (!contract.isValid()) {
            throw new EthereumException("contract is invalid: " + contractAddress);
        }
        return contract;
    }

    Long getChainId() throws Exception {
        Web3jService web3jService = getWeb3jService();
        Request<?, ChainIdResponse> request = new Request<>(
                "eth_chainId",
                null,
                web3jService,
                ChainIdResponse.class);
        ChainIdResponse response = request.send();
        if (!response.hasError()) {
            return response.getChainId().longValueExact();
        }
        // else we try to retrieve the chain id from transactions within the latest block
        Web3j web3j = getWeb3j();
        BigInteger blockNumber = web3j.ethBlockNumber().send().getBlockNumber();
        while (!blockNumber.equals(BigInteger.ZERO)) {
            EthBlock.Block block = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true).send().getBlock();
            List<EthBlock.TransactionResult> transactions = block.getTransactions();
            if (!transactions.isEmpty()) {
                EthBlock.TransactionResult transactionResult = transactions.get(0);
                EthBlock.TransactionObject transactionObject = (EthBlock.TransactionObject) transactionResult;
                Transaction transaction = transactionObject.get();
                Long chainId = transaction.getChainId();
                LOGGER.debug("getChainId: {}", chainId);
                return chainId;
            }
            blockNumber = blockNumber.subtract(BigInteger.ONE);
        }
        throw new EthereumException("could not determine chain id");
    }

    public EthereumResourceAdapter getResourceAdapter() {
        return this.resourceAdapter;
    }

    String sendTransaction(Credentials credentials, String to, BigInteger value, BigInteger gasPrice, Long chainId) throws Exception {
        BigInteger nonce = getNextNonce(credentials.getAddress());
        BigInteger gasLimit = BigInteger.valueOf(21000);
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, to, value);
        byte _chainId;
        if (null != chainId) {
            LOGGER.debug("Chain Id: {}", chainId);
            // https://github.com/web3j/web3j/issues/234
            if (chainId > 46) {
                LOGGER.warn("web3j cannot sign with chainId > 46");
                _chainId = ChainId.NONE;
            } else {
                _chainId = chainId.byteValue();
            }
        } else {
            _chainId = ChainId.NONE;
        }

        byte[] signedTransaction;
        if (_chainId != ChainId.NONE) {
            signedTransaction = TransactionEncoder.signMessage(rawTransaction, _chainId, credentials);
        } else {
            signedTransaction = TransactionEncoder.signMessage(rawTransaction, credentials);
        }
        String hexValue = Numeric.toHexString(signedTransaction);
        return sendRawTransaction(hexValue);
    }

    EthBlock.Block getBlock(DefaultBlockParameter defaultBlockParameter, boolean returnFullTransactionObjects) throws Exception {
        Web3j web3j = getWeb3j();
        EthBlock.Block block = web3j.ethGetBlockByNumber(defaultBlockParameter, returnFullTransactionObjects).send().getBlock();
        return block;
    }

    String getNetVersion() throws Exception {
        Web3j web3j = getWeb3j();
        return web3j.netVersion().send().getNetVersion();
    }

    BigInteger getPeerCount() throws Exception {
        Web3j web3j = getWeb3j();
        return web3j.netPeerCount().send().getQuantity();
    }

    String getProtocolVersion() throws Exception {
        Web3j web3j = getWeb3j();
        return web3j.ethProtocolVersion().send().getProtocolVersion();
    }

    boolean isSyncing() throws Exception {
        Web3j web3j = getWeb3j();
        return web3j.ethSyncing().send().isSyncing();
    }

    String getClientVersion() throws Exception {
        Web3j web3j = getWeb3j();
        return web3j.web3ClientVersion().send().getWeb3ClientVersion();
    }

    EthGetTransactionReceipt getTransactionReceipt(String transactionHash) throws Exception {
        Web3j web3j = getWeb3j();
        return web3j.ethGetTransactionReceipt(transactionHash).send();
    }

    void removeConnection(EthereumConnectionImpl ethereumConnection) {
        this.ethereumConnections.remove(ethereumConnection);
    }

    void addConnection(EthereumConnectionImpl ethereumConnection) {
        this.ethereumConnections.add(ethereumConnection);
    }

    @Override
    public String toString() {
        return "EthereumManagedConnection{" + "ethereumConnectionRequestInfo=" + this.ethereumConnectionRequestInfo + '}';
    }
}
