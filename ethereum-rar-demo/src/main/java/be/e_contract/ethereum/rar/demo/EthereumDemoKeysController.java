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

import be.e_contract.ethereum.rar.demo.model.EthereumBean;
import be.e_contract.ethereum.rar.demo.model.MemoryKeysBean;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("ethereumDemoKeysController")
@RequestScoped
public class EthereumDemoKeysController implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumDemoKeysController.class);

    @EJB
    private MemoryKeysBean memoryKeysBean;

    @EJB
    private EthereumBean ethereumBean;

    private String selectedAddress;

    private BigInteger selectedAddressBalance;

    public List<String> getKeys() {
        return this.memoryKeysBean.getKeys();
    }

    public String newKey() {
        try {
            this.memoryKeysBean.newKey();
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
        }
        return "/keys/index";
    }

    public String getBalance(String address) {
        LOGGER.debug("getBalance: {}", address);
        this.selectedAddress = address;
        this.selectedAddressBalance = this.ethereumBean.getBalance(this.selectedAddress);
        return "/keys/balance";
    }

    public String getSelectedAddress() {
        return this.selectedAddress;
    }

    public BigInteger getSelectedAddressBalance() {
        return this.selectedAddressBalance;
    }
}
