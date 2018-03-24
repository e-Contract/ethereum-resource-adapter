/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumXAResource implements XAResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumXAResource.class);

    private int transactionTimeout;

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        LOGGER.debug("commit: {} - one phase {}", xid, onePhase);
    }

    @Override
    public void end(Xid xid, int flags) throws XAException {
        LOGGER.debug("end: {} - flags {}", xid, false);
    }

    @Override
    public void forget(Xid xid) throws XAException {
        LOGGER.debug("forget: {}", xid);
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        LOGGER.debug("getTransactionTimeout");
        return this.transactionTimeout;
    }

    @Override
    public boolean isSameRM(XAResource xaRes) throws XAException {
        LOGGER.debug("isSameRM: {}", xaRes);
        return false;
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        LOGGER.debug("prepare: {}", xid);
        return 0;
    }

    @Override
    public Xid[] recover(int flag) throws XAException {
        LOGGER.debug("recover: {}", flag);
        return new Xid[]{};
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        LOGGER.debug("rollback: {}", xid);
    }

    @Override
    public boolean setTransactionTimeout(int seconds) throws XAException {
        LOGGER.debug("setTransactionTimeout: {}", seconds);
        this.transactionTimeout = seconds;
        return true;
    }

    @Override
    public void start(Xid xid, int flags) throws XAException {
        LOGGER.debug("start: {} - flags {}", xid, flags);
    }
}
