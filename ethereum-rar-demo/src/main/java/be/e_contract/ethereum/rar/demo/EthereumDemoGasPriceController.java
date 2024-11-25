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
package be.e_contract.ethereum.rar.demo;

import be.e_contract.ethereum.ra.oracle.api.GasPriceOracle;
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

@Named("ethereumDemoGasPriceController")
@RequestScoped
public class EthereumDemoGasPriceController implements Serializable {

    @EJB
    private GasPriceOracle gasPriceOracleBean;

    @EJB
    private EthereumBean ethereumBean;

    private Integer maxDuration;

    public Integer getMaxDuration() {
        return this.maxDuration;
    }

    public void setMaxDuration(Integer maxDuration) {
        this.maxDuration = maxDuration;
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

    public BigInteger getGasPrice() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            return this.ethereumBean.getGasPrice(null, false);
        } catch (RollbackException ex) {
            facesContext.addMessage(null, new FacesMessage("rollback error"));
        }
        return null;
    }

    public BigInteger getMaxPriorityFeePerGas() {
        return this.ethereumBean.getMaxPriorityFeePerGas();
    }

    public String refresh() {
        // pass our controller, so we get an updated max duration input
        return "/gas-price";
    }
}
