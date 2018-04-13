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

import be.e_contract.ethereum.ra.api.EthereumException;
import java.io.IOException;
import java.math.BigInteger;
import javax.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.ContractUtils;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.ChainId;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.response.TransactionReceiptProcessor;
import org.web3j.utils.Numeric;

public class EthereumTransactionManager extends TransactionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumTransactionManager.class);

    private final EthereumManagedConnection ethereumManagedConnection;

    private final Credentials credentials;
    private final byte chainId;

    public EthereumTransactionManager(EthereumManagedConnection ethereumManagedConnection,
            Credentials credentials, byte chainId) throws Exception {
        super((TransactionReceiptProcessor) null, null);
        this.ethereumManagedConnection = ethereumManagedConnection;
        this.credentials = credentials;
        this.chainId = chainId;
    }

    @Override
    public EthSendTransaction sendTransaction(BigInteger gasPrice, BigInteger gasLimit, String to, String data, BigInteger value) throws IOException {
        // we don't use this, overriding executeTransaction instead
        return null;
    }

    @Override
    protected TransactionReceipt executeTransaction(BigInteger gasPrice, BigInteger gasLimit, String to, String data, BigInteger value) throws IOException, TransactionException {
        BigInteger nonce;
        try {
            nonce = this.ethereumManagedConnection.getNextNonce(this.credentials.getAddress());
        } catch (Exception ex) {
            throw new IOException("could not determince next nonce: " + ex.getMessage(), ex);
        }
        LOGGER.debug("nonce: {}", nonce);
        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                gasPrice,
                gasLimit,
                to,
                value,
                data);
        byte[] signedMessage;
        if (this.chainId > ChainId.NONE) {
            signedMessage = TransactionEncoder.signMessage(rawTransaction, this.chainId, this.credentials);
        } else {
            signedMessage = TransactionEncoder.signMessage(rawTransaction, this.credentials);
        }
        String hexValue = Numeric.toHexString(signedMessage);
        try {
            this.ethereumManagedConnection.sendRawTransaction(hexValue);
        } catch (ResourceException ex) {
            LOGGER.error("error sending raw transaction: " + ex.getMessage(), ex);
            throw new IOException(ex);
        } catch (EthereumException ex) {
            LOGGER.error("error sending raw transaction: " + ex.getMessage(), ex);
            throw new IOException(ex);
        }
        String transactionHash = Hash.sha3(hexValue);
        String contractAddress = ContractUtils.generateContractAddress(this.credentials.getAddress(), nonce);
        EthereumTransactionReceipt ethereumTransactionReceipt = new EthereumTransactionReceipt(transactionHash, contractAddress);
        return ethereumTransactionReceipt;
    }
}
