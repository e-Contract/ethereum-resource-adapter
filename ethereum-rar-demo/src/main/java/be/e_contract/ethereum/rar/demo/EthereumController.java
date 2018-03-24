/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.rar.demo;

import java.io.Serializable;
import java.math.BigInteger;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("ethereumController")
@RequestScoped
public class EthereumController implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumController.class);

    @Inject
    private EthereumBean ethereumBean;

    private String nodeLocation;

    private boolean rollback;

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

    public BigInteger getOracleGasPrice() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            return this.ethereumBean.getGasPrice(this.nodeLocation, this.rollback);
        } catch (RollbackException ex) {
            facesContext.addMessage(null, new FacesMessage("rollback error"));
        }
        return null;
    }
}
