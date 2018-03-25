/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import be.e_contract.ethereum.ra.api.EthereumMessageListener;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Connector(
        reauthenticationSupport = false,
        transactionSupport = TransactionSupport.TransactionSupportLevel.XATransaction)
public class EthereumResourceAdapter implements ResourceAdapter, Serializable, Referenceable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumResourceAdapter.class);

    @ConfigProperty(defaultValue = "http://localhost:8545")
    private String nodeLocation;

    private BootstrapContext bootstrapContext;

    private Reference reference;

    private final List<EthereumWork> ethereumWorkList;

    public EthereumResourceAdapter() {
        LOGGER.debug("constructor");
        this.ethereumWorkList = Collections.synchronizedList(new LinkedList<>());
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
        this.bootstrapContext = ctx;
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping Ethereum JCA Resource Adapter");
        for (EthereumWork ethereumWork : this.ethereumWorkList) {
            ethereumWork.shutdown();
        }
    }

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException {
        LOGGER.debug("endpointActivation");
        LOGGER.debug("message endpoint factory: {}", endpointFactory);
        LOGGER.debug("activation spec: {}", spec);
        EthereumActivationSpec ethereumActivationSpec = (EthereumActivationSpec) spec;
        LOGGER.debug("node location: {}", ethereumActivationSpec.getNodeLocation());
        WorkManager workManager = this.bootstrapContext.getWorkManager();
        EthereumMessageListener ethereumMessageListener = (EthereumMessageListener) endpointFactory.createEndpoint(null);
        Method[] methods = EthereumMessageListener.class.getDeclaredMethods();
        for (Method method : methods) {
            boolean deliveryTransacted;
            try {
                deliveryTransacted = endpointFactory.isDeliveryTransacted(method);
            } catch (NoSuchMethodException ex) {
                continue;
            }
            if (deliveryTransacted) {
                LOGGER.warn("not supporting transacted delivery for method: {} on MDB {}",
                        method.getName(), ethereumMessageListener.getClass().getName());
                // TODO: implement this
            }
        }
        EthereumWork ethereumWork = new EthereumWork(ethereumMessageListener, ethereumActivationSpec, workManager);
        this.ethereumWorkList.add(ethereumWork);
        ethereumActivationSpec.setEthereumWork(ethereumWork);
        workManager.scheduleWork(ethereumWork);
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
        LOGGER.debug("endpointDeactivation");
        EthereumActivationSpec ethereumActivationSpec = (EthereumActivationSpec) spec;
        EthereumWork ethereumWork = ethereumActivationSpec.getEthereumWork();
        ethereumWork.shutdown();
        this.ethereumWorkList.remove(ethereumWork);
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
