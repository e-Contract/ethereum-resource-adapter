/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra.oracle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class GasPriceOracleTypeBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(GasPriceOracleTypeBean.class);

    @Inject
    @Any
    private Instance<GasPriceOracle> gasPriceOracleTypes;

    public Map<String, GasPriceOracle> getAccountTypes() {
        Map<String, GasPriceOracle> result = new HashMap<>();
        Iterator<GasPriceOracle> gasPriceOracleIterator = this.gasPriceOracleTypes.iterator();
        while (gasPriceOracleIterator.hasNext()) {
            GasPriceOracle gasTypeOracle = gasPriceOracleIterator.next();
            GasPriceOracleType gasPriceOracleTypeAnnotation = gasTypeOracle.getClass().getAnnotation(GasPriceOracleType.class);
            String gasPriceOracleType = gasPriceOracleTypeAnnotation.value();
            result.put(gasPriceOracleType, gasTypeOracle);
        }
        return result;
    }
}
