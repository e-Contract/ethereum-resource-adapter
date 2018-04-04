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

public class EthereumException extends Exception {

    private final int code;

    public EthereumException(int code, String message) {
        super(message);
        this.code = code;
    }

    public EthereumException(String message) {
        this(0, message);
    }

    public int getCode() {
        return this.code;
    }
}
