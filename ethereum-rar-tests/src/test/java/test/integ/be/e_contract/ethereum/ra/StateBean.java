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
package test.integ.be.e_contract.ethereum.ra;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class StateBean {

    private Set<String> pendingTransactions;

    private Set<String> blocks;

    @PostConstruct
    public void postConstruct() {
        this.pendingTransactions = new HashSet<>();
        this.blocks = new HashSet<>();
    }

    public void addPendingTransaction(String transactionHash) {
        this.pendingTransactions.add(transactionHash);
    }

    public void addBlock(String blockHash) {
        this.blocks.add(blockHash);
    }

    public boolean hasPendingTransactions() {
        return !this.pendingTransactions.isEmpty();
    }

    public boolean hasBlocks() {
        return !this.blocks.isEmpty();
    }
}
