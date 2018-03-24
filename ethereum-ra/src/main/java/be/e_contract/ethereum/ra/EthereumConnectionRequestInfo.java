/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import javax.resource.spi.ConnectionRequestInfo;

public class EthereumConnectionRequestInfo implements ConnectionRequestInfo {

    private final String nodeLocation;

    public EthereumConnectionRequestInfo(String nodeLocation) {
        this.nodeLocation = nodeLocation;
    }

    public String getNodeLocation() {
        return this.nodeLocation;
    }
}
