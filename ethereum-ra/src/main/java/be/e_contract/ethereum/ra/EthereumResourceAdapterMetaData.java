/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
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
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAdapterVendorName() {
        LOGGER.debug("getAdapterVendorName");
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAdapterName() {
        LOGGER.debug("getAdapterName");
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAdapterShortDescription() {
        LOGGER.debug("getAdapterShortDescription");
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSpecVersion() {
        LOGGER.debug("getSpecVersion");
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getInteractionSpecsSupported() {
        LOGGER.debug("getInteractionSpecsSupported");
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsExecuteWithInputAndOutputRecord() {
        LOGGER.debug("supportsExecuteWithInputAndOutputRecord");
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsExecuteWithInputRecordOnly() {
        LOGGER.debug("supportsExecuteWithInputRecordOnly");
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsLocalTransactionDemarcation() {
        LOGGER.debug("supportsLocalTransactionDemarcation");
        throw new UnsupportedOperationException();
    }
}
