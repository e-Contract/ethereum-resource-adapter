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
package be.e_contract.ethereum.utils;

/**
 * Representation of the final state of an Ethereum transaction.
 *
 * @author Frank Cornelis
 */
public enum EthereumFinalState {

    /**
     * Marks that the Ethereum transaction succeeded.
     */
    SUCCEEDED,
    /**
     * Marks that the Ethereum transaction has failed.
     */
    FAILED,
    /**
     * Marks that the Ethereum transaction has magically disappeared.
     */
    DISAPPEARED
}
