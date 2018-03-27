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

import java.io.IOException;
import java.util.Properties;

public class Version {

    private final static String IMPLEMENTATION_VERSION;

    public final static String DONATION = "0x0c56073db91c2ba57ff362301eb32262bbee6147";

    static {
        Properties properties = new Properties();
        try {
            properties.load(Version.class
                    .getResourceAsStream("/ethereum-resource-adapter-version.properties"));
        } catch (IOException e) {
            throw new RuntimeException("could not load ethereum-resource-adapter-version.properties");
        }
        IMPLEMENTATION_VERSION = properties.getProperty("implementation.version");
    }

    public static String getImplementationVersion() {
        return IMPLEMENTATION_VERSION;
    }
}
