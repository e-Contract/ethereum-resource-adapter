/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra.oracle;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class GasPriceOracleBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(GasPriceOracleBean.class);

    @Inject
    @Any
    private Instance<GasPriceOracle> gasPriceOracleTypes;

    public Map<String, GasPriceOracle> getGasPriceOracles() {
        Map<String, GasPriceOracle> result = new HashMap<>();
        Iterator<GasPriceOracle> gasPriceOracleIterator = this.gasPriceOracleTypes.iterator();
        while (gasPriceOracleIterator.hasNext()) {
            GasPriceOracle gasTypeOracle = gasPriceOracleIterator.next();
            LOGGER.debug("gas type oracle: {}", gasTypeOracle.getClass().getName());
            GasPriceOracleType gasPriceOracleTypeAnnotation = findGasPriceOracleTypeAnnotation(gasTypeOracle.getClass());
            if (null == gasPriceOracleTypeAnnotation) {
                // seems like this can happen
                continue;
            }
            String gasPriceOracleType = gasPriceOracleTypeAnnotation.value();
            result.put(gasPriceOracleType, gasTypeOracle);
        }
        return result;
    }

    public List<String> getGasPriceOracleNames() {
        List<String> result = new LinkedList<>();
        Iterator<GasPriceOracle> gasPriceOracleIterator = this.gasPriceOracleTypes.iterator();
        while (gasPriceOracleIterator.hasNext()) {
            GasPriceOracle gasTypeOracle = gasPriceOracleIterator.next();
            GasPriceOracleType gasPriceOracleTypeAnnotation = findGasPriceOracleTypeAnnotation(gasTypeOracle.getClass());
            if (null == gasPriceOracleTypeAnnotation) {
                // seems like this can happen
                continue;
            }
            String gasPriceOracleType = gasPriceOracleTypeAnnotation.value();
            result.add(gasPriceOracleType);
        }
        return result;
    }

    public Map<String, BigInteger> getGasPrices(Integer maxDuration) {
        Map<String, GasPriceOracle> gasPriceOracles = getGasPriceOracles();
        Map<String, BigInteger> gasPrices = new HashMap<>();
        for (Map.Entry<String, GasPriceOracle> gasPriceOracleEntry : gasPriceOracles.entrySet()) {
            BigInteger gasPrice = gasPriceOracleEntry.getValue().getGasPrice(maxDuration);
            gasPrices.put(gasPriceOracleEntry.getKey(), gasPrice);
        }
        return gasPrices;
    }

    public BigInteger getGasPrice(String oracle, Integer maxDuration) {
        Instance<GasPriceOracle> gasPriceOracleInstance = this.gasPriceOracleTypes.select(new GasPriceOracleTypeQualifier(oracle));
        if (gasPriceOracleInstance.isUnsatisfied()) {
            return null;
        }
        if (gasPriceOracleInstance.isAmbiguous()) {
            return null;
        }
        GasPriceOracle gasPriceOracle = gasPriceOracleInstance.get();
        BigInteger gasPrice = gasPriceOracle.getGasPrice(maxDuration);
        return gasPrice;
    }

    /**
     * Work-around for Weld Proxy stuff on @ApplicationScoped CDI beans
     *
     * @param clazz
     * @return
     */
    private GasPriceOracleType findGasPriceOracleTypeAnnotation(Class<?> clazz) {
        GasPriceOracleType gasPriceOracleTypeAnnotation = clazz.getAnnotation(GasPriceOracleType.class);
        while (null == gasPriceOracleTypeAnnotation) {
            if (clazz.equals(Object.class)) {
                return null;
            }
            clazz = clazz.getSuperclass();
            gasPriceOracleTypeAnnotation = clazz.getAnnotation(GasPriceOracleType.class);
        }
        return gasPriceOracleTypeAnnotation;
    }
}
