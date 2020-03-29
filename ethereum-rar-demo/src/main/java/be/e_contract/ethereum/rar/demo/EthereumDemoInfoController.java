/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2019-2020 e-Contract.be BV.
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

import be.e_contract.ethereum.rar.demo.model.EthereumBean;
import java.math.BigInteger;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named("ethereumDemoInfoController")
@RequestScoped
public class EthereumDemoInfoController {

    @EJB
    private EthereumBean ethereumBean;

    public String getClientVersion() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            return this.ethereumBean.getClientVersion();
        } catch (Exception ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "error: " + ex.getMessage(), null));
            return null;
        }
    }

    public String getNetVersion() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            return this.ethereumBean.getNetVersion();
        } catch (Exception ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "error: " + ex.getMessage(), null));
            return null;
        }
    }

    public String getProtocolVersion() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            return this.ethereumBean.getProtocolVersion();
        } catch (Exception ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "error: " + ex.getMessage(), null));
            return null;
        }
    }

    public BigInteger getPeerCount() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            return this.ethereumBean.getPeerCount();
        } catch (Exception ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "error: " + ex.getMessage(), null));
            return null;
        }
    }

    public boolean isSyncing() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            return this.ethereumBean.isSyncing();
        } catch (Exception ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "error: " + ex.getMessage(), null));
            return false;
        }
    }

    public Long getChainId() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            return this.ethereumBean.getChainId();
        } catch (Exception ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "error: " + ex.getMessage(), null));
            return null;
        }
    }
}
