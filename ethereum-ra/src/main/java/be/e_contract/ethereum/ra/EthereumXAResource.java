/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018 e-Contract.be BVBA.
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
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;

/**
 * Although the basics already work, this class still needs some work.
 *
 * @author Frank Cornelis
 */
public class EthereumXAResource implements XAResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumXAResource.class);

    private int transactionTimeout;

    private final EthereumManagedConnection ethereumManagedConnection;

    private final Map<Xid, List<String>> xidRawTransactions;

    private Xid currentXid;

    public EthereumXAResource(EthereumManagedConnection ethereumManagedConnection) {
        this.ethereumManagedConnection = ethereumManagedConnection;
        this.xidRawTransactions = new HashMap<>();
    }

    private List<String> getRawTransactions(Xid xid) {
        List<String> rawTransactions = this.xidRawTransactions.get(xid);
        if (null == rawTransactions) {
            rawTransactions = new LinkedList<>();
            this.xidRawTransactions.put(xid, rawTransactions);
        }
        return rawTransactions;
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        LOGGER.debug("commit: {} - one phase {}", xid, onePhase);
        this.currentXid = xid;
        List<String> rawTransactions = getRawTransactions(xid);
        for (String rawTransaction : rawTransactions) {
            LOGGER.debug("commit raw transaction: {}", rawTransaction);
            try {
                Web3j web3j = this.ethereumManagedConnection.getWeb3j();
                web3j.ethSendRawTransaction(rawTransaction);
            } catch (Exception ex) {
                LOGGER.error("web3j error: " + ex.getMessage(), ex);
                throw new XAException(XAException.XA_HEURRB);
            }
        }
        rawTransactions.clear();
    }

    @Override
    public void end(Xid xid, int flags) throws XAException {
        LOGGER.debug("end: {} - flags {}", xid, flags);
        this.currentXid = xid;
        List<String> rawTransactions = getRawTransactions(xid);
        if (isFlagSet(flags, TMFAIL)) {
            rawTransactions.clear();
        }
        if (!rawTransactions.isEmpty()) {
            try {
                Web3j web3j = this.ethereumManagedConnection.getWeb3j();
                // make sure the node is available
                web3j.ethProtocolVersion().send();
            } catch (Exception e) {
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
        this.currentXid = xid;
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        LOGGER.debug("getTransactionTimeout");
        return this.transactionTimeout;
    }

    @Override
    public boolean isSameRM(XAResource xaRes) throws XAException {
        boolean result = this == xaRes;
        if (result) {
            LOGGER.debug("isSameRM: {}", result);
        }
        return result;
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        LOGGER.debug("prepare: {}", xid);
        this.currentXid = xid;
        return XA_OK;
    }

    @Override
    public Xid[] recover(int flag) throws XAException {
        LOGGER.debug("recover: {}", flag);
        return new Xid[0];
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        LOGGER.debug("rollback: {}", xid);
        this.currentXid = xid;
        List<String> rawTransactions = getRawTransactions(xid);
        LOGGER.debug("number of raw transactions in queue: {}", rawTransactions.size());
        rawTransactions.clear();
    }

    @Override
    public boolean setTransactionTimeout(int seconds) throws XAException {
        LOGGER.debug("setTransactionTimeout: {}", seconds);
        if (seconds < 0) {
            throw new XAException();
        }
        this.transactionTimeout = seconds;
        return true;
    }

    @Override
    public void start(Xid xid, int flags) throws XAException {
        LOGGER.debug("start: {} - flags {}", xid, flags);
        this.currentXid = xid;
        List<String> rawTransactions = getRawTransactions(xid);
        if (!isFlagSet(flags, TMRESUME)) {
            rawTransactions.clear();
        }
    }

    public void scheduleRawTransaction(String rawTransaction) {
        LOGGER.debug("schedule raw transaction: {}", rawTransaction);
        List<String> rawTransactions = this.xidRawTransactions.get(this.currentXid);
        rawTransactions.add(rawTransaction);
    }
}
