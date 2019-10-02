/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2019 e-Contract.be BVBA.
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
package be.e_contract.ethereum.ra.web3j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.response.TransactionReceiptProcessor;

public class EthereumTransactionReceiptProcessor extends TransactionReceiptProcessor {

    private final Map<String, String> transactionHashToContractAddress;

    public EthereumTransactionReceiptProcessor() {
        super(null);
        this.transactionHashToContractAddress = new HashMap<>();
    }

    public void registerContractAddress(String transactionHash, String contractAddress) {
        this.transactionHashToContractAddress.put(transactionHash, contractAddress);
    }

    @Override
    public TransactionReceipt waitForTransactionReceipt(String transactionHash) throws IOException, TransactionException {
        String contractAddress = this.transactionHashToContractAddress.get(transactionHash);
        return new EthereumTransactionReceipt(transactionHash, contractAddress);
    }
}
