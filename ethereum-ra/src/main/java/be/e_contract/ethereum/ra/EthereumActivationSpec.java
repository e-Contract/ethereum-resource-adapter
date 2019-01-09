/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018-2019 e-Contract.be BVBA.
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
package be.e_contract.ethereum.ra;

import be.e_contract.ethereum.ra.api.EthereumMessageListener;
import java.io.Serializable;
import javax.resource.ResourceException;
import javax.resource.spi.Activation;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Activation(messageListeners = EthereumMessageListener.class)
public class EthereumActivationSpec implements ActivationSpec, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumActivationSpec.class);

    private transient EthereumResourceAdapter resourceAdapter;

    @ConfigProperty
    private String nodeLocation;

    @ConfigProperty(type = Boolean.class)
    private Boolean deliverPending;

    @ConfigProperty(type = Boolean.class)
    private Boolean deliverBlock;

    @ConfigProperty(type = Boolean.class)
    private Boolean omitSyncing;

    private EthereumMessageListener ethereumMessageListener;

    public EthereumActivationSpec() {
        LOGGER.debug("constructor");
    }

    public String getNodeLocation() {
        if (null == this.nodeLocation) {
            if (null == this.resourceAdapter) {
                // can happen on GlassFish 5
                return null;
            }
            return this.resourceAdapter.getNodeLocation();
        }
        return this.nodeLocation;
    }

    public void setNodeLocation(String nodeLocation) {
        this.nodeLocation = nodeLocation;
    }

    public Boolean isDeliverPending() {
        return this.deliverPending;
    }

    public void setDeliverPending(Boolean deliverPending) {
        this.deliverPending = deliverPending;
    }

    public Boolean isDeliverBlock() {
        return this.deliverBlock;
    }

    public void setDeliverBlock(Boolean deliverBlock) {
        this.deliverBlock = deliverBlock;
    }

    public Boolean isOmitSyncing() {
        return this.omitSyncing;
    }

    public void setOmitSyncing(Boolean omitSyncing) {
        this.omitSyncing = omitSyncing;
    }

    @Override
    public void validate() throws InvalidPropertyException {
        LOGGER.debug("validate");
        if (null == this.deliverPending && null == this.deliverBlock) {
            // nothing to do here...
            throw new InvalidPropertyException();
        }
    }

    @Override
    public ResourceAdapter getResourceAdapter() {
        LOGGER.debug("getResourceAdapter");
        return this.resourceAdapter;
    }

    @Override
    public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
        LOGGER.debug("setResourceAdapter");
        this.resourceAdapter = (EthereumResourceAdapter) ra;
    }

    public EthereumMessageListener getEthereumMessageListener() {
        return this.ethereumMessageListener;
    }

    public void setEthereumMessageListener(EthereumMessageListener ethereumMessageListener) {
        this.ethereumMessageListener = ethereumMessageListener;
    }
}
