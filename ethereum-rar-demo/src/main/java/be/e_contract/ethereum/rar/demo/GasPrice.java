/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.rar.demo;

import java.io.Serializable;
import java.math.BigInteger;

public class GasPrice implements Serializable {

    private String oracle;
    private BigInteger gasPrice;

    public GasPrice(String oracle, BigInteger gasPrice) {
        this.oracle = oracle;
        this.gasPrice = gasPrice;
    }

    public String getOracle() {
        return this.oracle;
    }

    public BigInteger getGasPrice() {
        return this.gasPrice;
    }
}
