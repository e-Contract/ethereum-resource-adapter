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

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumDeliveryXAResource implements XAResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumDeliveryXAResource.class);

    private int transactionTimeout;

    public EthereumDeliveryXAResource() {
        LOGGER.debug("constructor");
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        LOGGER.debug("commit: {}", xid);
    }

    @Override
    public void end(Xid xid, int flags) throws XAException {
        LOGGER.debug("end: {}", xid);
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
        LOGGER.debug("isSameRM");
        return false;
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        LOGGER.debug("prepare: {}", xid);
        return XA_OK;
    }

    @Override
    public Xid[] recover(int flag) throws XAException {
        LOGGER.debug("recover");
        return new Xid[0];
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
        LOGGER.debug("start: {}", xid);
    }
}
