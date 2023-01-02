/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2023 e-Contract.be BV.
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

import be.e_contract.ethereum.rar.demo.model.EthereumAdminBean;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named("ethereumDemoAdminController")
@RequestScoped
public class EthereumDemoAdminController {

    @EJB
    private EthereumAdminBean ethereumAdminBean;

    public String getImplementationVersion() {
        return this.ethereumAdminBean.getImplementationVersion();
    }

    public String getDefaultNodeLocation() {
        return this.ethereumAdminBean.getDefaultNodeLocation();
    }

    public String getDefaultWebSocketNodeLocation() {
        return this.ethereumAdminBean.getDefaultWebSocketNodeLocation();
    }

    public void clearNouncesCache() {
        this.ethereumAdminBean.clearNouncesCache();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage globalFacesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, "Nounce cache cleared.", null);
        facesContext.addMessage(null, globalFacesMessage);
    }
}
