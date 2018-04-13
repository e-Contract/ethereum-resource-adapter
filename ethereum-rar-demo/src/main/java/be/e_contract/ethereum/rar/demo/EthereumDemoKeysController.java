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
import be.e_contract.ethereum.rar.demo.model.MemoryKeysBean;
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
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

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

    private boolean rollback;

    private String to;
    private BigInteger value;
    private BigInteger gasPrice;
    private BigInteger nonce;
    private Integer chainId;

    private String contractAddress;

    public boolean isRollback() {
        return this.rollback;
    }

    public void setRollback(boolean rollback) {
        this.rollback = rollback;
    }

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

    public Integer getChainId() {
        return this.chainId;
    }

    public void setChainId(Integer chainId) {
        this.chainId = chainId;
    }

    public String getContractAddress() {
        return this.contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public List<String> getKeys() {
        return this.memoryKeysBean.getKeys();
    }

    public String newKey() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            String address = this.memoryKeysBean.newKey();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "new key address: " + address, null));
        } catch (Exception ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "new key error: " + ex.getMessage(), null));
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

    public void setSelectedAddress(String selectedAddress) {
        this.selectedAddress = selectedAddress;
    }

    public BigInteger getSelectedAddressBalance() {
        return this.selectedAddressBalance;
    }

    public String initTransaction(String address) {
        this.selectedAddress = address;
        try {
            this.gasPrice = this.ethereumBean.getGasPrice(null, false);
        } catch (RollbackException ex) {
            LOGGER.error("gas price error: " + ex.getMessage(), ex);
        }
        this.nonce = this.ethereumBean.getNonce(this.selectedAddress);
        return "/keys/transaction";
    }

    private String transaction;

    public String getTransaction() {
        return this.transaction;
    }

    public String signTransaction() {
        Byte chainIdByte;
        if (null != this.chainId) {
            chainIdByte = this.chainId.byteValue();
        } else {
            chainIdByte = null;
        }
        this.transaction = this.memoryKeysBean.signTransaction(this.nonce, this.gasPrice,
                this.selectedAddress, this.to, this.value, chainIdByte);
        return "/keys/transaction-result";
    }

    public String deployDemoContract(String address) {
        this.selectedAddress = address;
        Credentials credentials = this.memoryKeysBean.getCredentials(this.selectedAddress);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            TransactionReceipt transactionReceipt = this.ethereumBean.deployDemoContract(credentials);
            String contractAddress = transactionReceipt.getContractAddress();
            String transactionHash = transactionReceipt.getTransactionHash();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "contract address: " + contractAddress, null));
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "transaction hash: " + transactionHash, null));
        } catch (EthereumException ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "contract error: " + ex.getMessage(), null));
        }
        return "/keys/index";
    }

    public String initInvokeDemoContract(String address) {
        this.selectedAddress = address;
        return "/keys/contract";
    }

    public String invokeDemoContract() {
        Credentials credentials = this.memoryKeysBean.getCredentials(this.selectedAddress);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            String transactionHash = this.ethereumBean.invokeContract(this.contractAddress, credentials, this.value, this.rollback);
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "transaction hash: " + transactionHash, null));
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "contract invoked from: " + this.selectedAddress, null));
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "contract address: " + this.contractAddress, null));
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "contract invoked with value: " + this.value, null));
        } catch (RollbackException ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "transaction rollback", null));
        } catch (Exception ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "contract invoke error: "
                    + ex.getMessage(), null));
        }
        return "/keys/index";
    }

    public String initReadDemoContract(String address) {
        this.selectedAddress = address;
        return "/keys/read-contract";
    }

    public String readDemoContract() {
        Credentials credentials = this.memoryKeysBean.getCredentials(this.selectedAddress);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        BigInteger contractValue;
        try {
            contractValue = this.ethereumBean.readContract(this.contractAddress, credentials);
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "contract address: " + this.contractAddress, null));
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "contract value: " + contractValue, null));
        } catch (Exception ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "contract invoke error: "
                    + ex.getMessage(), null));
        }
        return "/keys/index";
    }
}
