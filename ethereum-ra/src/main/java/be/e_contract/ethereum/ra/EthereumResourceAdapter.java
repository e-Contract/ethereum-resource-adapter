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
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
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
import javax.resource.spi.work.WorkManager;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Connector(
        reauthenticationSupport = false,
        transactionSupport = TransactionSupport.TransactionSupportLevel.XATransaction,
        displayName = "Ethereum Resource Adapter",
        vendorName = "e-Contract.be BVBA",
        description = "JCA Resource Adapter to connect to Ethereum blockchain networks.")
public class EthereumResourceAdapter implements ResourceAdapter, Serializable, Referenceable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumResourceAdapter.class);

    @ConfigProperty(defaultValue = "http://localhost:8545", type = String.class,
            description = "The location of the client node.")
    @NotNull
    private String nodeLocation;

    private BootstrapContext bootstrapContext;

    private Reference reference;

    private final Map<String, EthereumWork> nodeLocationEthereumWork;

    private final Map<String, BigInteger> nonces;

    public EthereumResourceAdapter() {
        LOGGER.debug("constructor");
        this.nodeLocationEthereumWork = new HashMap<>();
        this.nonces = new HashMap<>();
    }

    public Map<String, BigInteger> getNonces() {
        return this.nonces;
    }

    public String getNodeLocation() {
        return this.nodeLocation;
    }

    public void setNodeLocation(String nodeLocation) {
        this.nodeLocation = nodeLocation;
    }

    @Override
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
        LOGGER.info("Starting Ethereum JCA Resource Adapter version " + Version.getImplementationVersion());
        LOGGER.info("If you like this project, please consider a donation at: " + Version.DONATION);
        this.bootstrapContext = ctx;
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping Ethereum JCA Resource Adapter");
        for (EthereumWork ethereumWork : this.nodeLocationEthereumWork.values()) {
            ethereumWork.shutdown();
        }
        this.nodeLocationEthereumWork.clear();
    }

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException {
        LOGGER.debug("endpointActivation");
        LOGGER.debug("message endpoint factory: {}", endpointFactory);
        LOGGER.debug("activation spec: {}", spec);

        if (!equals(spec.getResourceAdapter())) {
            throw new ResourceException("Activation spec not initialized with this resource adapter");
        }

        if (!(spec instanceof EthereumActivationSpec)) {
            throw new ResourceException("Unsupported activation spec type: {}", spec.getClass().getName());
        }

        boolean supportTransactedDelivery = false;
        for (Method method : EthereumMessageListener.class.getMethods()) {
            boolean transactedDelivery;
            try {
                transactedDelivery = endpointFactory.isDeliveryTransacted(method);
            } catch (NoSuchMethodException ex) {
                LOGGER.error("method not found: " + method.getName(), ex);
                throw new ResourceException();
            }
            if (transactedDelivery) {
                LOGGER.warn("not supporting transacted delivery for the moment: {}", method.getName());
            }
            supportTransactedDelivery |= transactedDelivery;
        }

        XAResource deliveryXAResource;
        if (supportTransactedDelivery) {
            deliveryXAResource = new EthereumDeliveryXAResource();
        } else {
            deliveryXAResource = null;
        }
        EthereumActivationSpec ethereumActivationSpec = (EthereumActivationSpec) spec;
        EthereumMessageListener ethereumMessageListener = (EthereumMessageListener) endpointFactory.createEndpoint(deliveryXAResource);
        ethereumActivationSpec.setEthereumMessageListener(ethereumMessageListener);
        String nodeLocation = ethereumActivationSpec.getNodeLocation();
        LOGGER.debug("node location: {}", nodeLocation);

        synchronized (this.nodeLocationEthereumWork) {
            // if not synchronized, we might end up with multiple workers for the same node
            EthereumWork ethereumWork = this.nodeLocationEthereumWork.get(nodeLocation);
            if (null == ethereumWork) {
                WorkManager workManager = this.bootstrapContext.getWorkManager();
                ethereumWork = new EthereumWork(nodeLocation, workManager);
                this.nodeLocationEthereumWork.put(nodeLocation, ethereumWork);
                workManager.scheduleWork(ethereumWork);
            }
            ethereumWork.addEthereumActivationSpec(ethereumActivationSpec);
        }
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
        LOGGER.debug("endpointDeactivation");
        EthereumActivationSpec ethereumActivationSpec = (EthereumActivationSpec) spec;
        String nodeLocation = ethereumActivationSpec.getNodeLocation();
        synchronized (this.nodeLocationEthereumWork) {
            EthereumWork ethereumWork = this.nodeLocationEthereumWork.get(nodeLocation);
            if (null == ethereumWork) {
                return;
            }
            if (ethereumWork.removeEthereumActivationSpec(ethereumActivationSpec)) {
                ethereumWork.shutdown();
                this.nodeLocationEthereumWork.remove(nodeLocation);
            }
        }
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
        return this.nodeLocation.equals(rhs.nodeLocation);
    }

    @Override
    public int hashCode() {
        return this.nodeLocation.hashCode();
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
