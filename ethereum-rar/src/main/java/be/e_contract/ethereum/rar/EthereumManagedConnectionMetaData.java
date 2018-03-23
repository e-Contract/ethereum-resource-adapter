/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.rar;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionMetaData;

public class EthereumManagedConnectionMetaData implements ManagedConnectionMetaData {

    @Override
    public String getEISProductName() throws ResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getEISProductVersion() throws ResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxConnections() throws ResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getUserName() throws ResourceException {
        throw new UnsupportedOperationException();
    }
}
