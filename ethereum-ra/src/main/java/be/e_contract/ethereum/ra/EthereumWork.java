/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra;

import java.util.LinkedList;
import java.util.List;
import javax.resource.ResourceException;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumWork implements Work {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumWork.class);

    private final WorkManager workManager;

    private final List<EthereumActivationSpec> ethereumActivationSpecs;

    private EthereumPendingTransactionWork ethereumPendingTransactionWork;

    private EthereumBlockWork ethereumBlockWork;

    private final String nodeLocation;

    public EthereumWork(String nodeLocation, WorkManager workManager) throws ResourceException {
        this.nodeLocation = nodeLocation;
        this.workManager = workManager;
        this.ethereumActivationSpecs = new LinkedList<>();
    }

    public void addEthereumActivationSpec(EthereumActivationSpec ethereumActivationSpec) {
        this.ethereumActivationSpecs.add(ethereumActivationSpec);
    }

    public boolean removeEthereumActivationSpec(EthereumActivationSpec ethereumActivationSpec) {
        this.ethereumActivationSpecs.remove(ethereumActivationSpec);
        return this.ethereumActivationSpecs.isEmpty();
    }

    @Override
    public void release() {
        LOGGER.debug("release");
    }

    public void shutdown() {
        LOGGER.debug("shutdown");
        this.ethereumPendingTransactionWork.shutdown();
        this.ethereumBlockWork.shutdown();
    }

    public WorkManager getWorkManager() {
        return this.workManager;
    }

    public List<EthereumActivationSpec> getEthereumActivationSpecs() {
        return this.ethereumActivationSpecs;
    }

    public String getNodeLocation() {
        return this.nodeLocation;
    }

    @Override
    public void run() {
        LOGGER.debug("run");

        this.ethereumPendingTransactionWork = new EthereumPendingTransactionWork(this);
        this.ethereumBlockWork = new EthereumBlockWork(this);

        try {
            this.workManager.scheduleWork(this.ethereumPendingTransactionWork);
            this.workManager.scheduleWork(this.ethereumBlockWork);
        } catch (WorkException ex) {
            LOGGER.error("could not start work");
        }
    }
}
