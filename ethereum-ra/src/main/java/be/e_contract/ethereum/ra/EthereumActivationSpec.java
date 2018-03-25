/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import be.e_contract.ethereum.ra.api.EthereumMessageListener;
import javax.resource.ResourceException;
import javax.resource.spi.Activation;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Activation(messageListeners = EthereumMessageListener.class)
public class EthereumActivationSpec implements ActivationSpec {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumActivationSpec.class);

    private ResourceAdapter resourceAdapter;

    @ConfigProperty
    private String nodeLocation;

    @ConfigProperty(type = Boolean.class)
    private Boolean fullBlock;

    @ConfigProperty(type = Boolean.class)
    private Boolean deliverPending;

    @ConfigProperty(type = Boolean.class)
    private Boolean deliverBlock;

    public EthereumActivationSpec() {
        LOGGER.debug("constructor");
    }

    public String getNodeLocation() {
        return this.nodeLocation;
    }

    public void setNodeLocation(String nodeLocation) {
        this.nodeLocation = nodeLocation;
    }

    public Boolean getFullBlock() {
        return this.fullBlock;
    }

    public void setFullBlock(Boolean fullBlock) {
        this.fullBlock = fullBlock;
    }

    public Boolean getDeliverPending() {
        return this.deliverPending;
    }

    public void setDeliverPending(Boolean deliverPending) {
        this.deliverPending = deliverPending;
    }

    public Boolean getDeliverBlock() {
        return this.deliverBlock;
    }

    public void setDeliverBlock(Boolean deliverBlock) {
        this.deliverBlock = deliverBlock;
    }

    @Override
    public void validate() throws InvalidPropertyException {
        LOGGER.debug("validate");
        throw new UnsupportedOperationException();
    }

    @Override
    public ResourceAdapter getResourceAdapter() {
        LOGGER.debug("getResourceAdapter");
        return this.resourceAdapter;
    }

    @Override
    public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
        LOGGER.debug("setResourceAdapter");
        this.resourceAdapter = ra;
    }
}
