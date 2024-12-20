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
package test.integ.be.e_contract.ethereum.ra;

import be.e_contract.ethereum.ra.api.EthereumConnection;
import be.e_contract.ethereum.ra.api.EthereumConnectionFactory;
import be.e_contract.ethereum.ra.api.TransactionConfirmation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.Security;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.resource.cci.LocalTransaction;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Convert;

@Stateless
public class TransactionBean {

    @Resource(lookup = "java:/EthereumConnectionFactory")
    private EthereumConnectionFactory ethereumConnectionFactory;

    public void performTransaction() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            List<String> accounts = ethereumConnection.getAccounts();
            String account = accounts.get(0);

            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            String address = "0x" + Keys.getAddress(ecKeyPair);

            BigInteger gasPrice = ethereumConnection.getGasPrice();
            BigInteger nonce = ethereumConnection.getTransactionCount(account);
            BigInteger value = Convert.toWei(BigDecimal.valueOf(10), Convert.Unit.ETHER).toBigInteger();
            String transactionHash = ethereumConnection.sendAccountTransaction(account, address, value, gasPrice, nonce);

            TransactionConfirmation transactionConfirmation = ethereumConnection.getTransactionConfirmation(transactionHash);
            while (transactionConfirmation.getConfirmingBlocks() == 0) {
                Thread.sleep(1000);
                transactionConfirmation = ethereumConnection.getTransactionConfirmation(transactionHash);
            }
            BigInteger balance = ethereumConnection.getBalance(address);
            assertEquals(value, balance);

            ECKeyPair ecKeyPair2 = Keys.createEcKeyPair();
            String address2 = "0x" + Keys.getAddress(ecKeyPair2);
            Credentials credentials = Credentials.create(ecKeyPair);
            BigInteger value2 = Convert.toWei(BigDecimal.valueOf(0.1), Convert.Unit.ETHER).toBigInteger();
            Long chainId = ethereumConnection.getChainId();

            LocalTransaction localTransaction = ethereumConnection.getLocalTransaction();
            localTransaction.begin();
            BigInteger totalValue2 = BigInteger.ZERO;
            for (int idx = 0; idx < 10; idx++) {
                totalValue2 = totalValue2.add(value2);
                transactionHash = ethereumConnection.sendTransaction(credentials, address2, value2, gasPrice, chainId);
            }
            localTransaction.commit();

            transactionConfirmation = ethereumConnection.getTransactionConfirmation(transactionHash);
            while (transactionConfirmation.getConfirmingBlocks() == 0) {
                Thread.sleep(1000);
                transactionConfirmation = ethereumConnection.getTransactionConfirmation(transactionHash);
            }
            BigInteger balance2 = ethereumConnection.getBalance(address2);
            assertEquals(totalValue2, balance2);
        } finally {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    public void performTransactionEIP1559() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            List<String> accounts = ethereumConnection.getAccounts();
            String account = accounts.get(0);

            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            String address = "0x" + Keys.getAddress(ecKeyPair);

            BigInteger gasPrice = ethereumConnection.getGasPrice();
            BigInteger nonce = ethereumConnection.getTransactionCount(account);
            BigInteger value = Convert.toWei(BigDecimal.valueOf(10), Convert.Unit.ETHER).toBigInteger();
            String transactionHash = ethereumConnection.sendAccountTransaction(account, address, value, gasPrice, nonce);

            TransactionConfirmation transactionConfirmation = ethereumConnection.getTransactionConfirmation(transactionHash);
            while (transactionConfirmation.getConfirmingBlocks() == 0) {
                Thread.sleep(1000);
                transactionConfirmation = ethereumConnection.getTransactionConfirmation(transactionHash);
            }
            BigInteger balance = ethereumConnection.getBalance(address);
            assertEquals(value, balance);

            ECKeyPair ecKeyPair2 = Keys.createEcKeyPair();
            String address2 = "0x" + Keys.getAddress(ecKeyPair2);
            Credentials credentials = Credentials.create(ecKeyPair);
            BigInteger value2 = Convert.toWei(BigDecimal.valueOf(0.1), Convert.Unit.ETHER).toBigInteger();
            Long chainId = ethereumConnection.getChainId();

            BigInteger maxFeePerGas = ethereumConnection.getGasPrice().multiply(BigInteger.valueOf(2));
            BigInteger maxPriorityFeePerGas = ethereumConnection.getMaxPriorityFeePerGas();

            LocalTransaction localTransaction = ethereumConnection.getLocalTransaction();
            localTransaction.begin();
            BigInteger totalValue2 = BigInteger.ZERO;
            for (int idx = 0; idx < 10; idx++) {
                totalValue2 = totalValue2.add(value2);
                transactionHash = ethereumConnection.sendTransaction(credentials, address2, value2, maxFeePerGas, maxPriorityFeePerGas,
                        chainId);
            }
            localTransaction.commit();

            transactionConfirmation = ethereumConnection.getTransactionConfirmation(transactionHash);
            while (transactionConfirmation.getConfirmingBlocks() == 0) {
                Thread.sleep(1000);
                transactionConfirmation = ethereumConnection.getTransactionConfirmation(transactionHash);
            }
            BigInteger balance2 = ethereumConnection.getBalance(address2);
            assertEquals(totalValue2, balance2);
        } finally {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    public void performSingleTransaction() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            List<String> accounts = ethereumConnection.getAccounts();
            String account = accounts.get(0);

            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            String address = "0x" + Keys.getAddress(ecKeyPair);

            BigInteger gasPrice = ethereumConnection.getGasPrice();
            BigInteger nonce = ethereumConnection.getTransactionCount(account);
            BigInteger value = Convert.toWei(BigDecimal.valueOf(10), Convert.Unit.ETHER).toBigInteger();
            String transactionHash = ethereumConnection.sendAccountTransaction(account, address, value, gasPrice, nonce);

            TransactionConfirmation transactionConfirmation = ethereumConnection.getTransactionConfirmation(transactionHash);
            while (transactionConfirmation.getConfirmingBlocks() == 0) {
                Thread.sleep(1000);
                transactionConfirmation = ethereumConnection.getTransactionConfirmation(transactionHash);
            }
            BigInteger balance = ethereumConnection.getBalance(address);
            assertEquals(value, balance);
        } finally {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void rollback() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            List<String> accounts = ethereumConnection.getAccounts();
            String account = accounts.get(0);

            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            String address = "0x" + Keys.getAddress(ecKeyPair);

            BigInteger gasPrice = ethereumConnection.getGasPrice();
            BigInteger nonce = ethereumConnection.getTransactionCount(account);
            BigInteger value = Convert.toWei(BigDecimal.valueOf(10), Convert.Unit.ETHER).toBigInteger();
            // next is not part of the JCA transaction
            String transactionHash = ethereumConnection.sendAccountTransaction(account, address, value, gasPrice, nonce);

            TransactionConfirmation transactionConfirmation = ethereumConnection.getTransactionConfirmation(transactionHash);
            while (transactionConfirmation.getConfirmingBlocks() == 0) {
                Thread.sleep(1000);
                transactionConfirmation = ethereumConnection.getTransactionConfirmation(transactionHash);
            }
            BigInteger balance = ethereumConnection.getBalance(address);
            assertEquals(value, balance);

            ECKeyPair ecKeyPair2 = Keys.createEcKeyPair();
            String address2 = "0x" + Keys.getAddress(ecKeyPair2);
            Credentials credentials = Credentials.create(ecKeyPair);
            BigInteger value2 = Convert.toWei(BigDecimal.valueOf(0.1), Convert.Unit.ETHER).toBigInteger();
            Long chainId = ethereumConnection.getChainId();

            // this one is part of the JCA transaction
            transactionHash = ethereumConnection.sendTransaction(credentials, address2, value2, gasPrice, chainId);

            throw new RollbackException(transactionHash);
        } finally {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }
}
