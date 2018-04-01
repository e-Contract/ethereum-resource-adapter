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

import be.e_contract.ethereum.ra.api.TransactionConfirmation;
import be.e_contract.ethereum.rar.demo.model.EthereumBean;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;

@Named("ethereumDemoConfirmController")
@RequestScoped
public class EthereumDemoConfirmController implements Serializable {

    @EJB
    private EthereumBean ethereumBean;

    private String transactionHash;

    public String getTransactionHash() {
        return this.transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public TransactionConfirmation getTransactionConfirmation() {
        if (StringUtils.isEmpty(this.transactionHash)) {
            return null;
        }
        return this.ethereumBean.getTransactionConfirmation(this.transactionHash);
    }

    public String confirm() {
        return "/confirm/transaction";
    }
}
