/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import java.util.LinkedList;
import java.util.List;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;

/**
 * This class still needs some work.
 *
 * @author Frank Cornelis
 */
public class EthereumXAResource implements XAResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumXAResource.class);

    private int transactionTimeout;

    private final EthereumManagedConnection ethereumManagedConnection;

    private final List<String> rawTransactions;

    public EthereumXAResource(EthereumManagedConnection ethereumManagedConnection) {
        this.ethereumManagedConnection = ethereumManagedConnection;
        this.rawTransactions = new LinkedList<>();
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        LOGGER.debug("commit: {} - one phase {}", xid, onePhase);
        for (String rawTransaction : this.rawTransactions) {
            LOGGER.debug("commit raw transaction: {}", rawTransaction);
            try {
                Web3j web3j = this.ethereumManagedConnection.getWeb3j();
                web3j.ethSendRawTransaction(rawTransaction);
            } catch (Exception ex) {
                LOGGER.error("web3j error: " + ex.getMessage(), ex);
                throw new XAException(XAException.XA_HEURRB);
            }
        }
        this.rawTransactions.clear();
    }

    @Override
    public void end(Xid xid, int flags) throws XAException {
        LOGGER.debug("end: {} - flags {}", xid, flags);
        if (isFlagSet(flags, TMFAIL)) {
            this.rawTransactions.clear();
        }
        if (!this.rawTransactions.isEmpty()) {
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
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        LOGGER.debug("getTransactionTimeout");
        return this.transactionTimeout;
    }

    @Override
    public boolean isSameRM(XAResource xaRes) throws XAException {
        LOGGER.debug("isSameRM: {}", xaRes);
        return this == xaRes;
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        LOGGER.debug("prepare: {}", xid);
        return XA_OK;
    }

    @Override
    public Xid[] recover(int flag) throws XAException {
        LOGGER.debug("recover: {}", flag);
        return new Xid[]{};
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        LOGGER.debug("rollback: {}", xid);
        LOGGER.debug("number of raw transactions in queue: {}", this.rawTransactions.size());
        this.rawTransactions.clear();
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
        if (!isFlagSet(flags, TMRESUME)) {
            this.rawTransactions.clear();
        }
    }

    void scheduleRawTransaction(String rawTransaction) {
        LOGGER.debug("schedule raw transaction: {}", rawTransaction);
        this.rawTransactions.add(rawTransaction);
    }
}
