/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018-2020 e-Contract.be BV.
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

import be.e_contract.ethereum.ra.api.EthereumConnection;
import be.e_contract.ethereum.ra.api.EthereumConnectionFactory;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.resource.ResourceException;

@Singleton
@Startup
public class StateBean {

    @Resource(mappedName = "java:/EthereumConnectionFactory")
    private EthereumConnectionFactory ethereumConnectionFactory;

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

    public boolean hasPendingTransactions() throws ResourceException {
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            String clientVersion = ethereumConnection.getClientVersion();
            if (null != clientVersion && (clientVersion.startsWith("Parity") || clientVersion.startsWith("OpenEthereum"))) {
                // Parity in dev mode does not yield pending transactions
                return true;
            }
        }
        return !this.pendingTransactions.isEmpty();
    }

    public boolean hasPendingTransaction(String transactionHash) {
        return this.pendingTransactions.contains(transactionHash);
    }

    public boolean hasBlocks() {
        return !this.blocks.isEmpty();
    }
}
