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
package be.e_contract.ethereum.ra.api;

import javax.resource.cci.ConnectionSpec;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Via this Ethereum connection specification you can override the detail client
 * node location. The resource adapter supports both HTTP and IPC locations.
 *
 * @author Frank Cornelis
 */
public class EthereumConnectionSpec implements ConnectionSpec {

    private final String nodeLocation;

    /**
     * Main constructor.
     *
     * @param nodeLocation
     */
    public EthereumConnectionSpec(String nodeLocation) {
        this.nodeLocation = nodeLocation;
    }

    /**
     * Gives back the location of the Ethereum client node.
     *
     * @return
     */
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
