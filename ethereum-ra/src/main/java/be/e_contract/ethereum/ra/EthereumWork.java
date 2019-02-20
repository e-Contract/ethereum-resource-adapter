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
import javax.resource.spi.work.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EthereumWork implements Work {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumWork.class);

    private final String nodeLocation;

    private boolean shutdown;

    private final List<EthereumActivationSpec> ethereumActivationSpecs;

    public EthereumWork(String nodeLocation) {
        this.nodeLocation = nodeLocation;
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
    }

    public boolean isShutdown() {
        return this.shutdown;
    }

    public List<EthereumActivationSpec> getEthereumActivationSpecs() {
        return this.ethereumActivationSpecs;
    }

    public String getNodeLocation() {
        return this.nodeLocation;
    }
}
