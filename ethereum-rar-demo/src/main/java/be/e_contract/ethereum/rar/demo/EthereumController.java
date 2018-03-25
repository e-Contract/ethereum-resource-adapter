/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.rar.demo;

import be.e_contract.ethereum.ra.oracle.GasPriceOracleBean;
import be.e_contract.ethereum.rar.demo.model.EthereumBean;
import be.e_contract.ethereum.rar.demo.model.RollbackException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
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
}
