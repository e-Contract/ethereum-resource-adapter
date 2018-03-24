/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import java.io.Serializable;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.Connector;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import javax.resource.spi.TransactionSupport;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Connector(
        reauthenticationSupport = false,
        transactionSupport = TransactionSupport.TransactionSupportLevel.NoTransaction)
public class EthereumResourceAdapter implements ResourceAdapter, Serializable, Referenceable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumResourceAdapter.class);

    @ConfigProperty(defaultValue = "http://localhost:8545")
    private String nodeLocation;

    private BootstrapContext bootstrapContext;

    private Reference reference;

    public EthereumResourceAdapter() {
        LOGGER.debug("constructor");
    }

    public String getNodeLocation() {
        return this.nodeLocation;
    }

    public void setNodeLocation(String nodeLocation) {
        this.nodeLocation = nodeLocation;
    }

    @Override
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
        LOGGER.debug("start");
        this.bootstrapContext = ctx;
    }

    @Override
    public void stop() {
        LOGGER.debug("stop");
    }

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException {
        LOGGER.debug("endpointActivation");
        throw new UnsupportedOperationException();
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
        LOGGER.debug("endpointDeactivation");
        throw new UnsupportedOperationException();
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        LOGGER.debug("getXAResources");
        // This method is called by the application server during crash recovery.
        return new XAResource[0];
    }

    // hashCode and equals have to be implemented
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
        EthereumResourceAdapter rhs = (EthereumResourceAdapter) obj;
        return new EqualsBuilder()
                .append(this.nodeLocation, rhs.nodeLocation)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.nodeLocation).toHashCode();
    }

    @Override
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    @Override
    public Reference getReference() throws NamingException {
        return this.reference;
    }
}
