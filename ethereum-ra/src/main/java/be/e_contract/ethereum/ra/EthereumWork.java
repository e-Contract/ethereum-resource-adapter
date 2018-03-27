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

    private boolean shutdown;

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
        this.shutdown = true;
        this.ethereumPendingTransactionWork.shutdown();
        this.ethereumBlockWork.shutdown();
    }

    public boolean isShutdown() {
        return this.shutdown;
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

    public EthereumPendingTransactionWork getEthereumPendingTransactionWork() {
        return this.ethereumPendingTransactionWork;
    }

    public void setEthereumPendingTransactionWork(EthereumPendingTransactionWork ethereumPendingTransactionWork) {
        this.ethereumPendingTransactionWork = ethereumPendingTransactionWork;
    }

    public EthereumBlockWork getEthereumBlockWork() {
        return this.ethereumBlockWork;
    }

    public void setEthereumBlockWork(EthereumBlockWork ethereumBlockWork) {
        this.ethereumBlockWork = ethereumBlockWork;
    }

    @Override
    public void run() {
        LOGGER.debug("run");

        this.ethereumPendingTransactionWork = new EthereumPendingTransactionWork(this);
        this.ethereumBlockWork = new EthereumBlockWork(this);

        try {
            this.workManager.scheduleWork(this.ethereumPendingTransactionWork, WorkManager.INDEFINITE,
                    null, new EthereumWorkListener(this.ethereumPendingTransactionWork, this));
            this.workManager.scheduleWork(this.ethereumBlockWork, WorkManager.INDEFINITE, null,
                    new EthereumWorkListener(this.ethereumBlockWork, this));
        } catch (WorkException ex) {
            LOGGER.error("could not start work");
        }
    }
}
