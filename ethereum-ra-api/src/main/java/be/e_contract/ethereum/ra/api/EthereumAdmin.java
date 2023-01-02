/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2023 e-Contract.be BV.
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

/**
 * Administrator interface for Ethereum Resource Adapter.
 *
 * @author Frank Cornelis
 */
public interface EthereumAdmin {

    /**
     * Gives back the implementation version of the Ethereum resource adapter.
     *
     * @return the version.
     */
    String getImplementationVersion();

    /**
     * Gives back the default location of the client node.
     *
     * @return the location.
     */
    String getDefaultNodeLocation();

    /**
     * Gives back the default web socket client node location.
     *
     * @return the web socket location.
     */
    String getDefaultWebSocketNodeLocation();

    /**
     * Clears the nounces cache.
     */
    void clearNouncesCache();

    /**
     * Clears the nounce cache for a specific Ethereum address.
     *
     * @param address the Ethereum address for which to clear the nounce cache
     * entry.
     */
    void clearNounceCache(String address);
}
