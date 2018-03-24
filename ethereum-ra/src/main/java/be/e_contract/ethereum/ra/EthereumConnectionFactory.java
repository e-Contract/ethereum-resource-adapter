/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import java.io.Serializable;
import javax.resource.Referenceable;
import javax.resource.ResourceException;

public interface EthereumConnectionFactory extends Serializable, Referenceable {

    public EthereumConnection getConnection() throws ResourceException;
}
