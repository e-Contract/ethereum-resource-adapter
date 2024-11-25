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
import javax.resource.ResourceException;
import javax.resource.cci.LocalTransaction;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Convert;
import test.integ.be.e_contract.ethereum.ra.contract.DemoContract;

@Stateless
public class ContractBean {

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

            Long chainId = ethereumConnection.getChainId();
            Credentials credentials = Credentials.create(ecKeyPair);
            LocalTransaction localTransaction = ethereumConnection.getLocalTransaction();
            localTransaction.begin();
            ContractGasProvider contractGasProvider = new TestContractGasProvider(ethereumConnection);
            TransactionReceipt contractTransactionReceipt = ethereumConnection.deploy(DemoContract.class, contractGasProvider, credentials, chainId);
            String contractAddress = contractTransactionReceipt.getContractAddress();
            localTransaction.commit();

            transactionConfirmation = ethereumConnection.getTransactionConfirmation(contractTransactionReceipt.getTransactionHash());
            while (transactionConfirmation.getConfirmingBlocks() == 0) {
                Thread.sleep(1000);
                transactionConfirmation = ethereumConnection.getTransactionConfirmation(contractTransactionReceipt.getTransactionHash());
            }

            DemoContract contract = ethereumConnection.load(DemoContract.class, contractAddress, credentials, chainId, contractGasProvider);
            assertEquals(BigInteger.ZERO, contract.getValue().send());

            BigInteger contractValue = BigInteger.valueOf(1234);
            localTransaction.begin();
            String contractTransactionHash = contract.setValue(contractValue).send().getTransactionHash();
            localTransaction.commit();

            transactionConfirmation = ethereumConnection.getTransactionConfirmation(contractTransactionHash);
            while (transactionConfirmation.getConfirmingBlocks() == 0) {
                Thread.sleep(1000);
                transactionConfirmation = ethereumConnection.getTransactionConfirmation(contractTransactionHash);
            }

            assertEquals(contractValue, contract.getValue().send());

            // fast transactions on contract
            localTransaction.begin();
            for (int idx = 0; idx < 100; idx++) {
                contractValue = BigInteger.valueOf(idx);
                contractTransactionHash = contract.setValue(contractValue).send().getTransactionHash();
            }
            localTransaction.commit();

            transactionConfirmation = ethereumConnection.getTransactionConfirmation(contractTransactionHash);
            while (transactionConfirmation.getConfirmingBlocks() == 0) {
                Thread.sleep(1000);
                transactionConfirmation = ethereumConnection.getTransactionConfirmation(contractTransactionHash);
            }

            assertEquals(contractValue, contract.getValue().send());
        } finally {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    public void testUnknownContract() throws Exception {
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

            Long chainId = ethereumConnection.getChainId();
            Credentials credentials = Credentials.create(ecKeyPair);
            LocalTransaction localTransaction = ethereumConnection.getLocalTransaction();
            localTransaction.begin();
            ContractGasProvider contractGasProvider = new TestContractGasProvider(ethereumConnection);
            TransactionReceipt contractTransactionReceipt = ethereumConnection.deploy(DemoContract.class, contractGasProvider, credentials, chainId);
            String contractAddress = contractTransactionReceipt.getContractAddress();
            localTransaction.commit();

            transactionConfirmation = ethereumConnection.getTransactionConfirmation(contractTransactionReceipt.getTransactionHash());
            while (transactionConfirmation.getConfirmingBlocks() == 0) {
                Thread.sleep(1000);
                transactionConfirmation = ethereumConnection.getTransactionConfirmation(contractTransactionReceipt.getTransactionHash());
            }

            DemoContract contract = ethereumConnection.load(DemoContract.class, contractAddress, credentials, chainId, contractGasProvider);

            localTransaction.begin();
            // TODO: solidity selfdestruct deprecated
            String contractTransactionHash = contract.kill().send().getTransactionHash();
            localTransaction.commit();

            transactionConfirmation = ethereumConnection.getTransactionConfirmation(contractTransactionHash);
            while (transactionConfirmation.getConfirmingBlocks() == 0) {
                Thread.sleep(1000);
                transactionConfirmation = ethereumConnection.getTransactionConfirmation(contractTransactionHash);
            }

            try {
                ethereumConnection.load(DemoContract.class, contractAddress, credentials, chainId, contractGasProvider);
                fail();
            } catch (ResourceException e) {
                // expected
            }
        } finally {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }
}
