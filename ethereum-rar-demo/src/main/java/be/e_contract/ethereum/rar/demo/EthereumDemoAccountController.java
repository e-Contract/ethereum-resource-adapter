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
import java.math.BigInteger;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
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

    private String to;
    private BigInteger value;
    private BigInteger gasPrice;
    private BigInteger nonce;

    public String getTo() {
        return this.to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BigInteger getValue() {
        return this.value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public BigInteger getGasPrice() {
        return this.gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    public BigInteger getNonce() {
        return this.nonce;
    }

    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
    }

    public String sendTransaction() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            String transactionHash = this.ethereumBean.sendAccountTransaction(this.selectedAccount,
                    this.to, this.value, this.gasPrice, this.nonce);
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "transaction hash: " + transactionHash, null));
        } catch (EthereumException ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), null));
        }
        return "/accounts/index";
    }

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
        return "/accounts/index";
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
        return "/accounts/balance";
    }

    public String unlock(String account) {
        this.selectedAccount = account;
        return "/accounts/unlock";
    }

    public String doUnlock() {
        LOGGER.debug("unlock {} - password {}", this.selectedAccount, this.password);
        try {
            this.ethereumBean.unlockAccount(this.selectedAccount, this.password);
        } catch (EthereumException ex) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), null));
        }
        return "/accounts/unlocked";
    }

    public String initTransaction(String account) {
        this.selectedAccount = account;
        try {
            this.gasPrice = this.ethereumBean.getGasPrice(null, false);
        } catch (RollbackException ex) {
            LOGGER.error("gas price error: " + ex.getMessage(), ex);
        }
        this.nonce = this.ethereumBean.getNonce(this.selectedAccount);
        return "/accounts/transaction";
    }
}
