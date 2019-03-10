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
package be.e_contract.ethereum.ra.inflow;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.resource.ResourceException;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.work.HintsContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkContext;
import javax.resource.spi.work.WorkContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EthereumWork implements Work, WorkContextProvider, ResourceAdapterAssociation {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumWork.class);

    private final String nodeLocation;

    private final String wsOrigin;

    private boolean shutdown;

    private CountDownLatch doneSignal;

    private final List<EthereumActivationSpec> ethereumActivationSpecs;

    private ResourceAdapter resourceAdapter;

    public EthereumWork(String nodeLocation, String wsOrigin) {
        this.nodeLocation = nodeLocation;
        this.wsOrigin = wsOrigin;
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
        // nothing to do here
        // especially do not release the message endpoints here or shutdown the work
        // as the EthereumWorkListener needs to be able to reschedule this work later on
    }

    @Override
    public void run() {
        this.doneSignal = new CountDownLatch(1);
        try {
            doWork();
        } catch (Exception e) {
            LOGGER.error("work error: " + e.getMessage(), e);
        } finally {
            this.doneSignal.countDown();
        }
    }

    protected abstract void doWork() throws Exception;

    @Override
    public List<WorkContext> getWorkContexts() {
        List<WorkContext> workContexts = new LinkedList<>();
        HintsContext hintsContext = new HintsContext();
        hintsContext.setName(getClass().getCanonicalName());
        hintsContext.setDescription("Node location: " + this.nodeLocation);
        hintsContext.setHint(HintsContext.NAME_HINT, getClass().getCanonicalName());
        hintsContext.setHint(HintsContext.LONGRUNNING_HINT, Boolean.TRUE);
        workContexts.add(hintsContext);
        return workContexts;
    }

    @Override
    public ResourceAdapter getResourceAdapter() {
        return this.resourceAdapter;
    }

    @Override
    public void setResourceAdapter(ResourceAdapter resourceAdapter) throws ResourceException {
        this.resourceAdapter = resourceAdapter;
    }

    public void shutdown() {
        LOGGER.debug("shutdown");
        this.shutdown = true;
        try {
            this.doneSignal.await();
        } catch (InterruptedException ex) {
            LOGGER.error("done signal interrupted: " + ex.getMessage(), ex);
        }
    }

    public boolean isShutdown() {
        return this.shutdown;
    }

    public List<EthereumActivationSpec> getEthereumActivationSpecs() {
        return this.ethereumActivationSpecs;
    }

    protected String getNodeLocation() {
        return this.nodeLocation;
    }

    protected String getWsOrigin() {
        if (null == this.wsOrigin) {
            return "http://localhost";
        }
        return this.wsOrigin;
    }
}
