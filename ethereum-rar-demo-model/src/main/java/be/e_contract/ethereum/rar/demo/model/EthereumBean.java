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
package be.e_contract.ethereum.rar.demo.model;

import be.e_contract.ethereum.ra.api.EthereumConnection;
import be.e_contract.ethereum.ra.api.EthereumConnectionFactory;
import be.e_contract.ethereum.ra.api.EthereumConnectionSpec;
import be.e_contract.ethereum.ra.api.EthereumException;
import be.e_contract.ethereum.ra.api.TransactionConfirmation;
import java.math.BigInteger;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.resource.ResourceException;
import javax.resource.cci.LocalTransaction;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class EthereumBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumBean.class);

    @Resource(name = "EthereumConnectionFactory")
    private EthereumConnectionFactory ethereumConnectionFactory;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public BigInteger getGasPrice(String nodeLocation, boolean rollback) throws RollbackException {
        EthereumConnectionSpec ethereumConnectionSpec;
        if (StringUtils.isEmpty(nodeLocation)) {
            ethereumConnectionSpec = null;
        } else {
            ethereumConnectionSpec = new EthereumConnectionSpec(nodeLocation);
        }
        try (EthereumConnection ethereumConnection = (EthereumConnection) this.ethereumConnectionFactory.getConnection(ethereumConnectionSpec)) {
            if (rollback) {
                throw new RollbackException();
            }
            return ethereumConnection.getGasPrice();
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }

    public String sendRawTransaction(String rawTransaction, boolean rollback) throws RollbackException {
        String transactionHash;
        try (EthereumConnection ethereumConnection = (EthereumConnection) this.ethereumConnectionFactory.getConnection()) {
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

    public String sendRawTransactionLocalTransaction(String rawTransaction, boolean rollback) throws RollbackException {
        String transactionHash;
        try (EthereumConnection ethereumConnection = (EthereumConnection) this.ethereumConnectionFactory.getConnection()) {
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
        try (EthereumConnection ethereumConnection = (EthereumConnection) this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.getTransactionConfirmation(transactionHash);
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }

    public List<String> getAccounts() {
        try (EthereumConnection ethereumConnection = (EthereumConnection) this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.getAccounts();
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }

    public void newAccount(String password) {
        try (EthereumConnection ethereumConnection = (EthereumConnection) this.ethereumConnectionFactory.getConnection()) {
            ethereumConnection.newAccount(password);
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
        }
    }

    public BigInteger getBalance(String account) {
        try (EthereumConnection ethereumConnection = (EthereumConnection) this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.getBalance(account);
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }

    public void unlockAccount(String account, String password) throws EthereumException {
        try (EthereumConnection ethereumConnection = (EthereumConnection) this.ethereumConnectionFactory.getConnection()) {
            ethereumConnection.unlockAccount(account, password);
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
        }
    }

    public String sendAccountTransaction(String account, String to, BigInteger value, BigInteger gasPrice, BigInteger nonce) throws EthereumException {
        try (EthereumConnection ethereumConnection = (EthereumConnection) this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.sendAccountTransaction(account, to, value, gasPrice, nonce);
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }

    public BigInteger getNonce(String address) {
        try (EthereumConnection ethereumConnection = (EthereumConnection) this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.getTransactionCount(address);
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }
}
