/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import javax.resource.ResourceException;
import javax.resource.spi.Activation;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Activation(messageListeners = null)
public class EthereumActivationSpec implements ActivationSpec {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumActivationSpec.class);

    public EthereumActivationSpec() {
        LOGGER.debug("constructor");
    }

    @Override
    public void validate() throws InvalidPropertyException {
        LOGGER.debug("validate");
        throw new UnsupportedOperationException();
    }

    @Override
    public ResourceAdapter getResourceAdapter() {
        LOGGER.debug("getResourceAdapter");
        throw new UnsupportedOperationException();
    }

    @Override
    public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
        LOGGER.debug("setResourceAdapter");
        throw new UnsupportedOperationException();
    }
}
