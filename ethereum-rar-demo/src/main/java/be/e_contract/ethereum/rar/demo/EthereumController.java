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
package be.e_contract.ethereum.rar.demo;

import be.e_contract.ethereum.ra.api.TransactionConfirmation;
import be.e_contract.ethereum.ra.oracle.GasPriceOracleBean;
import be.e_contract.ethereum.rar.demo.model.EthereumBean;
import be.e_contract.ethereum.rar.demo.model.RollbackException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("ethereumController")
@RequestScoped
public class EthereumController implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumController.class);

    @EJB
    private EthereumBean ethereumBean;

    @EJB
    private GasPriceOracleBean gasPriceOracleBean;

    private String nodeLocation;

    private boolean rollback;

    private Integer maxDuration;

    private String rawTransaction;

    private String transactionHash;

    private boolean localTransaction;

    @PostConstruct
    public void postConstruct() {
        this.rawTransaction = "Raw transaction here...";
    }

    public String getNodeLocation() {
        return this.nodeLocation;
    }

    public void setNodeLocation(String nodeLocation) {
        this.nodeLocation = nodeLocation;
    }

    public boolean isRollback() {
        return this.rollback;
    }

    public void setRollback(boolean rollback) {
        this.rollback = rollback;
    }

    public Integer getMaxDuration() {
        return this.maxDuration;
    }

    public void setMaxDuration(Integer maxDuration) {
        this.maxDuration = maxDuration;
    }

    public String getRawTransaction() {
        return this.rawTransaction;
    }

    public void setRawTransaction(String rawTransaction) {
        this.rawTransaction = rawTransaction;
    }

    public String getTransactionHash() {
        return this.transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public boolean isLocalTransaction() {
        return this.localTransaction;
    }

    public void setLocalTransaction(boolean localTransaction) {
        this.localTransaction = localTransaction;
    }

    public BigInteger getGasPrice() {
        LOGGER.debug("node location: {}", this.nodeLocation);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            return this.ethereumBean.getGasPrice(this.nodeLocation, this.rollback);
        } catch (RollbackException ex) {
            facesContext.addMessage(null, new FacesMessage("rollback error"));
        }
        return null;
    }

    public List<GasPrice> getGasPrices() {
        Map<String, BigInteger> gasPrices = this.gasPriceOracleBean.getGasPrices(this.maxDuration);
        List<GasPrice> result = new LinkedList<>();
        for (Map.Entry<String, BigInteger> gasPriceEntry : gasPrices.entrySet()) {
            GasPrice gasPrice = new GasPrice(gasPriceEntry.getKey(), gasPriceEntry.getValue());
            result.add(gasPrice);
        }
        return result;
    }

    public String sendRawTransaction() {
        LOGGER.debug("send raw transaction: {}", this.rawTransaction);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            if (this.localTransaction) {
                this.ethereumBean.sendRawTransactionLocalTransaction(this.rawTransaction, this.rollback);
            } else {
                this.ethereumBean.sendRawTransaction(this.rawTransaction, this.rollback);
            }
        } catch (RollbackException ex) {
            facesContext.addMessage(null, new FacesMessage("rollback error"));
        }
        return "/index";
    }

    public TransactionConfirmation getTransactionConfirmation() {
        if (StringUtils.isEmpty(this.transactionHash)) {
            return null;
        }
        return this.ethereumBean.getTransactionConfirmation(this.transactionHash);
    }
}
