/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.rar.demo;

import java.io.Serializable;
import java.math.BigInteger;
import javax.inject.Inject;
import javax.inject.Named;

@Named("ethereumController")
public class EthereumController implements Serializable {

    @Inject
    private EthereumBean ethereumBean;

    public BigInteger getGasPrice() {
        return this.ethereumBean.getGasPrice();
    }
}
