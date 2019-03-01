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
package be.e_contract.ethereum.rar.demo.model;

import be.e_contract.ethereum.ra.api.EthereumConnection;
import be.e_contract.ethereum.ra.api.EthereumConnectionFactory;
import be.e_contract.ethereum.ra.api.EthereumConnectionSpec;
import be.e_contract.ethereum.ra.api.EthereumException;
import be.e_contract.ethereum.ra.api.TransactionConfirmation;
import be.e_contract.ethereum.rar.demo.model.contract.DemoContract;
import java.math.BigInteger;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.resource.ResourceException;
import javax.resource.cci.LocalTransaction;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;

@Stateless
public class EthereumBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumBean.class);

    @Resource(name = "EthereumConnectionFactory")
    private EthereumConnectionFactory ethereumConnectionFactory;

    @EJB
    private ContractGasProvider contractGasProvider;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public BigInteger getGasPrice(String nodeLocation, boolean rollback) throws RollbackException {
        EthereumConnectionSpec ethereumConnectionSpec;
        if (StringUtils.isEmpty(nodeLocation)) {
            ethereumConnectionSpec = null;
        } else {
            ethereumConnectionSpec = new EthereumConnectionSpec(nodeLocation);
        }
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection(ethereumConnectionSpec)) {
            if (rollback) {
                throw new RollbackException();
            }
            return ethereumConnection.getGasPrice();
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }

    public String sendRawTransaction(String rawTransaction, boolean rollback) throws RollbackException, EthereumException {
        String transactionHash;
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            transactionHash = ethereumConnection.sendRawTransaction(rawTransaction);
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
        if (rollback) {
            throw new RollbackException();
        }
        return transactionHash;
    }

    public String sendRawTransactionLocalTransaction(String rawTransaction, boolean rollback) throws RollbackException, EthereumException {
        String transactionHash;
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            LocalTransaction localTransaction = ethereumConnection.getLocalTransaction();
            localTransaction.begin();
            transactionHash = ethereumConnection.sendRawTransaction(rawTransaction);
            if (rollback) {
                localTransaction.rollback();
            } else {
                localTransaction.commit();
            }
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
        return transactionHash;
    }

    public TransactionConfirmation getTransactionConfirmation(String transactionHash) {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.getTransactionConfirmation(transactionHash);
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }

    public List<String> getAccounts() {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.getAccounts();
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }

    public String newAccount(String password) {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.newAccount(password);
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }

    public BigInteger getBalance(String account) {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.getBalance(account);
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }

    public void unlockAccount(String account, String password) throws EthereumException {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            ethereumConnection.unlockAccount(account, password);
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
        }
    }

    public String sendAccountTransaction(String account, String to, BigInteger value, BigInteger gasPrice, BigInteger nonce) throws EthereumException {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.sendAccountTransaction(account, to, value, gasPrice, nonce);
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }

    public BigInteger getNonce(String address) {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.getTransactionCount(address);
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }

    public TransactionReceipt deployDemoContract(Credentials credentials) throws EthereumException {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            Long chainId = ethereumConnection.getChainId();
            LOGGER.debug("chain id: {}", chainId);
            Long _chainId;
            if (null == chainId) {
                _chainId = null;
            } else if (chainId > 255) {
                // TODO: chainId can be larger than byte... web3j TODO
                // dev network has ip155 option to 0
                _chainId = null;
            } else {
                _chainId = chainId;
            }
            return ethereumConnection.deploy(DemoContract.class, this.contractGasProvider, credentials, _chainId);
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }

    public String invokeContract(String contractAddress, Credentials credentials, BigInteger value, boolean rollback) throws Exception {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            Long chainId = ethereumConnection.getChainId();
            LOGGER.debug("chain id: {}", chainId);
            Long _chainId;
            if (null == chainId) {
                _chainId = null;
            } else if (chainId > 46) {
                // web3j cannot handle chainId above 46
                // TODO: chainId can be larger than byte... web3j TODO
                // dev network has ip155 option to 0
                _chainId = null;
            } else {
                _chainId = chainId;
            }
            DemoContract demoContract = ethereumConnection.load(DemoContract.class, contractAddress,
                    credentials, _chainId, this.contractGasProvider);
            String transactionHash = demoContract.setValue(value).send().getTransactionHash();
            if (rollback) {
                throw new RollbackException("rollback");
            }
            return transactionHash;
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }

    public BigInteger readContract(String contractAddress, Credentials credentials) throws Exception {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            Long chainId = ethereumConnection.getChainId();
            LOGGER.debug("chain id: {}", chainId);
            Long _chainId;
            if (null == chainId) {
                _chainId = null;
            } else if (chainId > 46) {
                // web3j cannot handle chainId above 46
                // TODO: chainId can be larger than byte... web3j TODO
                // dev network has ip155 option to 0
                _chainId = null;
            } else {
                _chainId = chainId;
            }
            DemoContract demoContract = ethereumConnection.load(DemoContract.class, contractAddress,
                    credentials, _chainId, this.contractGasProvider);
            return demoContract.getValue().send();
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }

    public String getClientVersion() throws Exception {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.getClientVersion();
        }
    }

    public String getNetVersion() throws Exception {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.getNetVersion();
        }
    }

    public String getProtocolVersion() throws Exception {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.getProtocolVersion();
        }
    }

    public BigInteger getPeerCount() throws Exception {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.getPeerCount();
        }
    }

    public boolean isSyncing() throws Exception {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.isSyncing();
        }
    }
}
