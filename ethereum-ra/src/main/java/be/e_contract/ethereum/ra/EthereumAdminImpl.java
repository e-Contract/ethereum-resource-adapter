/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2023 e-Contract.be BV.
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

import be.e_contract.ethereum.ra.api.EthereumAdmin;
import java.io.Serializable;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.AdministeredObject;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AdministeredObject(
        adminObjectInterfaces = EthereumAdmin.class
)
public class EthereumAdminImpl implements EthereumAdmin, ResourceAdapterAssociation, Serializable, Referenceable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumAdminImpl.class);

    public EthereumAdminImpl() {
        LOGGER.debug("default constructor");
    }

    private EthereumResourceAdapter resourceAdapter;

    private Reference reference;

    @Override
    public ResourceAdapter getResourceAdapter() {
        return this.resourceAdapter;
    }

    @Override
    public void setResourceAdapter(ResourceAdapter resourceAdapter) throws ResourceException {
        this.resourceAdapter = (EthereumResourceAdapter) resourceAdapter;
    }

    @Override
    public String getImplementationVersion() {
        return Version.getImplementationVersion();
    }

    @Override
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    @Override
    public Reference getReference() throws NamingException {
        return this.reference;
    }

    @Override
    public String getDefaultNodeLocation() {
        return this.resourceAdapter.getNodeLocation();
    }

    @Override
    public String getDefaultWebSocketNodeLocation() {
        return this.resourceAdapter.getWebSocketNodeLocation();
    }

    @Override
    public void clearNouncesCache() {
        this.resourceAdapter.getNonces().clear();
    }

    @Override
    public void clearNounceCache(String address) {
        this.resourceAdapter.getNonces().remove(address);
    }
}
