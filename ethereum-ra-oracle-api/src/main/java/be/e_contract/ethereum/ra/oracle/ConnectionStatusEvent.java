/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra.oracle;

public class ConnectionStatusEvent {

    private final boolean connected;

    public ConnectionStatusEvent(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return this.connected;
    }
}
