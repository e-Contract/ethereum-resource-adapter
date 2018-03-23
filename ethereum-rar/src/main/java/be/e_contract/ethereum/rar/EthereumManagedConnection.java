/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.rar;

import java.io.PrintWriter;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

/**
 *
 * @author Frank Cornelis
 */
public class EthereumManagedConnection implements ManagedConnection {

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroy() throws ResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanup() throws ResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        throw new UnsupportedOperationException();
    }
}
