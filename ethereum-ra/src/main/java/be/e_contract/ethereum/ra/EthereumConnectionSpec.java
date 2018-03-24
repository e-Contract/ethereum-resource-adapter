/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import javax.resource.cci.ConnectionSpec;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class EthereumConnectionSpec implements ConnectionSpec {

    private final String nodeLocation;

    public EthereumConnectionSpec(String nodeLocation) {
        this.nodeLocation = nodeLocation;
    }

    public String getNodeLocation() {
        return this.nodeLocation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        EthereumConnectionSpec rhs = (EthereumConnectionSpec) obj;
        return new EqualsBuilder()
                .append(this.nodeLocation, rhs.nodeLocation)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.nodeLocation).toHashCode();
    }
}
