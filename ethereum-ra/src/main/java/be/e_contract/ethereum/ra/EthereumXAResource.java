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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.resource.ResourceException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;

public class EthereumXAResource implements XAResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumXAResource.class);

    private int transactionTimeout;

    private final EthereumManagedConnection ethereumManagedConnection;

    private final Map<Xid, EthereumTransactionCommit> xidRawTransactions;

    private Xid currentXid;

    public EthereumXAResource(EthereumManagedConnection ethereumManagedConnection) {
        this.ethereumManagedConnection = ethereumManagedConnection;
        this.xidRawTransactions = new HashMap<>();
    }

    private EthereumTransactionCommit getEthereumTransactionCommit(Xid xid) {
        EthereumTransactionCommit ethereumTransactionCommit = this.xidRawTransactions.get(xid);
        if (null == ethereumTransactionCommit) {
            ethereumTransactionCommit = new EthereumTransactionCommit(this.ethereumManagedConnection, xid);
            this.xidRawTransactions.put(xid, ethereumTransactionCommit);
        }
        return ethereumTransactionCommit;
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        LOGGER.debug("commit: {} - one phase {}", xid, onePhase);
        if (!this.currentXid.equals(xid)) {
            throw new XAException("invalid xid");
        }
        EthereumTransactionCommit ethereumTransactionCommit = this.xidRawTransactions.get(xid);
        if (null == ethereumTransactionCommit) {
            throw new XAException("commit unknown transaction: " + xid);
        }
        try {
            ethereumTransactionCommit.commit();
        } catch (ResourceException ex) {
            LOGGER.error("could not commit transaction: " + ex.getMessage(), ex);
            throw new XAException(XAException.XA_HEURMIX);
        }
        this.xidRawTransactions.remove(xid);
    }

    @Override
    public void end(Xid xid, int flags) throws XAException {
        LOGGER.debug("end: {} - flags {}", xid, flags);
        if (!this.currentXid.equals(xid)) {
            LOGGER.error("invalid xid: {}", xid);
            throw new XAException("invalid xid");
        }
        if (isFlagSet(flags, TMFAIL)) {
            LOGGER.debug("end: TMFAIL");
            return;
        }
        EthereumTransactionCommit ethereumTransactionCommit = getEthereumTransactionCommit(xid);
        if (ethereumTransactionCommit.getTransactionCount() > 0) {
            try {
                Web3j web3j = this.ethereumManagedConnection.getWeb3j();
                // make sure the node is available
                web3j.ethProtocolVersion().send();
            } catch (Exception e) {
                LOGGER.error("error during end: " + e.getMessage(), e);
                throw new XAException(XAException.XA_RBROLLBACK);
            }
        }
    }

    private boolean isFlagSet(int flags, int mask) {
        return mask == (flags & mask);
    }

    @Override
    public void forget(Xid xid) throws XAException {
        LOGGER.debug("forget: {}", xid);
        if (xid == null) {
            return;
        }
        if (xid.equals(this.currentXid)) {
            this.currentXid = null;
        }
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        LOGGER.debug("getTransactionTimeout");
        return this.transactionTimeout;
    }

    @Override
    public boolean isSameRM(XAResource xaRes) throws XAException {
        if (this == xaRes) {
            return true;
        }
        if (xaRes instanceof EthereumXAResource) {
            LOGGER.warn("Another EthereumXAResource was checked");
            return false;
        }
        return false;
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        LOGGER.debug("prepare: {}", xid);
        return XA_OK;
    }

    @Override
    public Xid[] recover(int flag) throws XAException {
        LOGGER.debug("recover instance: {}", this);
        LOGGER.debug("recover: {}", flag);
        List<Xid> xids = new LinkedList<>();
        for (EthereumTransactionCommit ethereumTransactionCommit : this.xidRawTransactions.values()) {
            if (ethereumTransactionCommit.isPrepared()) {
                xids.add(ethereumTransactionCommit.getXid());
            }
        }
        LOGGER.debug("recover: {}", xids);
        return xids.toArray(new Xid[0]);
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        LOGGER.debug("rollback: {}", xid);
        if (!this.currentXid.equals(xid)) {
            LOGGER.error("invalid xid: {}", xid);
            throw new XAException("invalid xid");
        }
        EthereumTransactionCommit ethereumTransactionCommit = this.xidRawTransactions.get(xid);
        if (null == ethereumTransactionCommit) {
            LOGGER.error("rollback unknown transaction: {}", xid);
            throw new XAException("rollback unknown transaction: " + xid);
        }
        LOGGER.debug("number of raw transactions in queue: {}", ethereumTransactionCommit.getTransactionCount());
        ethereumTransactionCommit.rollback();
        this.xidRawTransactions.remove(xid);
    }

    @Override
    public boolean setTransactionTimeout(int seconds) throws XAException {
        LOGGER.debug("setTransactionTimeout: {}", seconds);
        if (seconds < 0) {
            throw new XAException();
        }
        this.transactionTimeout = seconds;
        for (EthereumTransactionCommit ethereumTransactionCommit : this.xidRawTransactions.values()) {
            ethereumTransactionCommit.setTransactionTimeout(seconds);
        }
        return true;
    }

    @Override
    public void start(Xid xid, int flags) throws XAException {
        LOGGER.debug("start: {} - flags {}", xid, flags);
        if (isFlagSet(flags, TMNOFLAGS)) {
            if (this.xidRawTransactions.containsKey(xid)) {
                throw new XAException("duplicate xid: " + xid);
            }
            this.currentXid = xid;
        }

        EthereumTransactionCommit ethereumTransactionCommit = getEthereumTransactionCommit(xid);
        if (!isFlagSet(flags, TMRESUME)) {
            ethereumTransactionCommit.clear();
        }
        if (isFlagSet(flags, TMRESUME)) {
            if (this.currentXid.equals(xid)) {
                throw new XAException("attempting to resume in different transaction: expected " + this.currentXid + " but received " + xid);
            }
        }
    }

    public void scheduleRawTransaction(String rawTransaction) throws ResourceException {
        LOGGER.debug("schedule raw transaction: {}", rawTransaction);
        EthereumTransactionCommit ethereumTransactionCommit = this.xidRawTransactions.get(this.currentXid);
        ethereumTransactionCommit.addRawTransaction(rawTransaction);
    }
}
