/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.rar;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;

public class EthereumConnectionFactoryImpl implements EthereumConnectionFactory {

    @Override
    public EthereumConnection getConnection() throws ResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setReference(Reference reference) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reference getReference() throws NamingException {
        throw new UnsupportedOperationException();
    }
}
