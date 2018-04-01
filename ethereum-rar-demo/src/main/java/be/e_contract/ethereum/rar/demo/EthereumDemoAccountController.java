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
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("ethereumDemoAccountController")
@RequestScoped
public class EthereumDemoAccountController implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumDemoAccountController.class);

    @EJB
    private EthereumBean ethereumBean;

    private String selectedAccount;

    private BigInteger selectedAccountBalance;

    private String password;

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getAccounts() {
        return this.ethereumBean.getAccounts();
    }

    public String newAccount() {
        this.ethereumBean.newAccount(this.password);
        return "/accounts";
    }

    public String getSelectedAccount() {
        return this.selectedAccount;
    }

    public void setSelectedAccount(String selectedAccount) {
        this.selectedAccount = selectedAccount;
    }

    public BigInteger getSelectedAccountBalance() {
        return this.selectedAccountBalance;
    }

    public String getBalance(String account) {
        LOGGER.debug("getBalance: {}", account);
        this.selectedAccount = account;
        this.selectedAccountBalance = this.ethereumBean.getBalance(this.selectedAccount);
        return "/balance";
    }

    public String unlock(String account) {
        this.selectedAccount = account;
        return "/unlock";
    }

    public String doUnlock() {
        LOGGER.debug("unlock {} - password {}", this.selectedAccount, this.password);
        this.ethereumBean.unlockAccount(this.selectedAccount, this.password);
        return "/unlocked";
    }
}
