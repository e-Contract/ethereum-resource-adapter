/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumConnectionFactoryImpl implements EthereumConnectionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumConnectionFactoryImpl.class);

    private Reference reference;

    public EthereumConnectionFactoryImpl() {
        LOGGER.debug("constructor");
    }

    @Override
    public EthereumConnection getConnection() throws ResourceException {
        LOGGER.debug("getConnection");
        return new EthereumConnectionImpl();
    }

    @Override
    public void setReference(Reference reference) {
        LOGGER.debug("setReference");
        this.reference = reference;
    }

    @Override
    public Reference getReference() throws NamingException {
        LOGGER.debug("getReference");
        return this.reference;
    }
}
