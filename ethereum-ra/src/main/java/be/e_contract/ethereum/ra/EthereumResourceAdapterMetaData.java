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

import javax.resource.cci.ResourceAdapterMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumResourceAdapterMetaData implements ResourceAdapterMetaData {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumResourceAdapterMetaData.class);

    @Override
    public String getAdapterVersion() {
        LOGGER.debug("getAdapterVersion");
        return Version.getImplementationVersion();
    }

    @Override
    public String getAdapterVendorName() {
        LOGGER.debug("getAdapterVendorName");
        return "e-Contract.be BVBA";
    }

    @Override
    public String getAdapterName() {
        LOGGER.debug("getAdapterName");
        return "Ethereum JCA Connector";
    }

    @Override
    public String getAdapterShortDescription() {
        LOGGER.debug("getAdapterShortDescription");
        return "Ethereum JCA Connector";
    }

    @Override
    public String getSpecVersion() {
        LOGGER.debug("getSpecVersion");
        return "1.6";
    }

    @Override
    public String[] getInteractionSpecsSupported() {
        LOGGER.debug("getInteractionSpecsSupported");
        return new String[0];
    }

    @Override
    public boolean supportsExecuteWithInputAndOutputRecord() {
        LOGGER.debug("supportsExecuteWithInputAndOutputRecord");
        return false;
    }

    @Override
    public boolean supportsExecuteWithInputRecordOnly() {
        LOGGER.debug("supportsExecuteWithInputRecordOnly");
        return false;
    }

    @Override
    public boolean supportsLocalTransactionDemarcation() {
        LOGGER.debug("supportsLocalTransactionDemarcation");
        return true;
    }
}
