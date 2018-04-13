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

import be.e_contract.ethereum.ra.api.EthereumException;
import be.e_contract.ethereum.rar.demo.model.EthereumBean;
import be.e_contract.ethereum.rar.demo.model.RollbackException;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("ethereumDemoRawController")
@RequestScoped
public class EthereumDemoRawController implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumDemoRawController.class);

    @EJB
    private EthereumBean ethereumBean;

    private String rawTransaction;

    private boolean rollback;

    private boolean localTransaction;

    @PostConstruct
    public void postConstruct() {
        this.rawTransaction = "Raw transaction here...";
    }

    public boolean isRollback() {
        return this.rollback;
    }

    public void setRollback(boolean rollback) {
        this.rollback = rollback;
    }

    public boolean isLocalTransaction() {
        return this.localTransaction;
    }

    public void setLocalTransaction(boolean localTransaction) {
        this.localTransaction = localTransaction;
    }

    public String getRawTransaction() {
        return this.rawTransaction;
    }

    public void setRawTransaction(String rawTransaction) {
        this.rawTransaction = rawTransaction;
    }

    public String sendRawTransaction() {
        LOGGER.debug("send raw transaction: {}", this.rawTransaction);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            String transactionHash;
            if (this.localTransaction) {
                transactionHash = this.ethereumBean.sendRawTransactionLocalTransaction(this.rawTransaction, this.rollback);
            } else {
                transactionHash = this.ethereumBean.sendRawTransaction(this.rawTransaction, this.rollback);
            }
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "transaction hash: " + transactionHash, null));
        } catch (RollbackException ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "rollback error", null));
        } catch (EthereumException ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ethereum error: " + ex.getMessage(), null));
        }
        return "/raw/result";
    }
}
