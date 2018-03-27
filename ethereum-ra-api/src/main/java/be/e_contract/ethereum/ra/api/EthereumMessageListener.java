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
package be.e_contract.ethereum.ra.api;

import java.util.Date;

/**
 * Interface for Ethereum network listeners.
 *
 * @author Frank Cornelis
 */
public interface EthereumMessageListener {

    /**
     * Gives back the hash of the latest block with a timestamp when the
     * resource adapter received the notification.
     *
     * @param blockHash
     * @param timestamp
     * @throws Exception
     */
    void block(String blockHash, Date timestamp) throws Exception;

    /**
     * Gives back the hash of a pending transaction with a timestamp when the
     * resource adapter received the notification.
     *
     * @param transactionHash
     * @param timestamp
     * @throws Exception
     */
    void pendingTransaction(String transactionHash, Date timestamp) throws Exception;

    /**
     * Gives back the client node connection status. If disconnected, the
     * resource adapter will automatically try to reconnect.
     *
     * @param connected
     * @throws Exception
     */
    void connectionStatus(boolean connected) throws Exception;
}
