/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.rar.demo;

import java.io.Serializable;
import java.math.BigInteger;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("ethereumController")
@RequestScoped
public class EthereumController implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumController.class);

    @Inject
    private EthereumBean ethereumBean;

    private String nodeLocation;

    public String getNodeLocation() {
        return this.nodeLocation;
    }

    public void setNodeLocation(String nodeLocation) {
        this.nodeLocation = nodeLocation;
    }

    public BigInteger getGasPrice() {
        LOGGER.debug("node location: {}", this.nodeLocation);
        return this.ethereumBean.getGasPrice(this.nodeLocation);
    }
}
