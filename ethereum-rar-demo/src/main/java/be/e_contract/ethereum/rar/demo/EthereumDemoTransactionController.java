/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2019 e-Contract.be BVBA.
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

import be.e_contract.ethereum.utils.EthereumPublicationEvent;
import be.e_contract.ethereum.utils.EthereumTransactionManager;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.resource.ResourceException;

@Named("ethereumDemoTransactionController")
@ApplicationScoped
public class EthereumDemoTransactionController {

    @Inject
    private EthereumTransactionManager ethereumTransactionManager;

    private String transactionHash;

    private String result;

    @PostConstruct
    public void postConstruct() {
        this.result = "";
    }

    public String getTransactionHash() {
        return this.transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getResult() {
        return this.result;
    }

    public String add() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            this.ethereumTransactionManager.monitorTransaction(this.transactionHash);
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "monitoring transaction hash: " + this.transactionHash, null));
        } catch (ResourceException ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "error: " + this.transactionHash, null));
        }
        this.transactionHash = null;
        return "/transactions";
    }

    private void observeEthereumEvent(@Observes EthereumPublicationEvent event) {
        this.result += "transaction " + event.getTransactionHash() + ": ";
        this.result += event.getFinalState();
        if (event.getPublicationBlockNumber() != null) {
            this.result += " published within block " + event.getPublicationBlockNumber();
        }
        this.result += "\n";
    }
}
