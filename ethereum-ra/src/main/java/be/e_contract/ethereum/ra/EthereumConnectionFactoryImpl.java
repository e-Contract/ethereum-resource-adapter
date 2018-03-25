/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import be.e_contract.ethereum.ra.api.EthereumConnectionSpec;
import be.e_contract.ethereum.ra.api.EthereumConnectionFactory;
import be.e_contract.ethereum.ra.api.EthereumConnection;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumConnectionFactoryImpl implements EthereumConnectionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumConnectionFactoryImpl.class);

    private final ManagedConnectionFactory managedConnectionFactory;

    private final ConnectionManager connectionManager;

    private Reference reference;

    public EthereumConnectionFactoryImpl() {
        this(null, null);
    }

    public EthereumConnectionFactoryImpl(ManagedConnectionFactory managedConnectionFactory, ConnectionManager cxManager) {
        LOGGER.debug("constructor");
        this.managedConnectionFactory = managedConnectionFactory;
        this.connectionManager = cxManager;
    }

    @Override
    public EthereumConnection getConnection() throws ResourceException {
        return getConnection(null);
    }

    @Override
    public EthereumConnection getConnection(ConnectionSpec connectionSpec) throws ResourceException {
        LOGGER.debug("getConnection");
        EthereumConnectionRequestInfo ethereumConnectionRequestInfo;
        if (null != connectionSpec) {
            EthereumConnectionSpec ethereumConnectionSpec = (EthereumConnectionSpec) connectionSpec;
            ethereumConnectionRequestInfo = new EthereumConnectionRequestInfo(ethereumConnectionSpec.getNodeLocation());
        } else {
            ethereumConnectionRequestInfo = null;
        }
        return (EthereumConnection) this.connectionManager.allocateConnection(this.managedConnectionFactory, ethereumConnectionRequestInfo);
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

    @Override
    public RecordFactory getRecordFactory() throws ResourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResourceAdapterMetaData getMetaData() throws ResourceException {
        return new EthereumResourceAdapterMetaData();
    }
}
