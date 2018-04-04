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
package be.e_contract.ethereum.rar.demo.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

@Singleton
@Startup
public class MemoryKeysBean {

    private Map<String, ECKeyPair> keys;

    @PostConstruct
    public void postConstruct() {
        this.keys = new HashMap<>();
    }

    public List<String> getKeys() {
        return new LinkedList(this.keys.keySet());
    }

    public String newKey() throws Exception {
        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        String address = "0x" + Keys.getAddress(ecKeyPair);
        this.keys.put(address, ecKeyPair);
        return address;
    }
}
