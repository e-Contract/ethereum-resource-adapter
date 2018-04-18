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
package be.e_contract.ethereum.ra.oracle;

import be.e_contract.ethereum.ra.oracle.spi.GasPriceOracleType;
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
import be.e_contract.ethereum.ra.oracle.spi.GasPriceOracleSpi;
import be.e_contract.ethereum.ra.oracle.api.GasPriceOracle;
import be.e_contract.ethereum.ra.oracle.api.UnknownGasPriceOracleException;

@Stateless
public class GasPriceOracleBean implements GasPriceOracle {

    private static final Logger LOGGER = LoggerFactory.getLogger(GasPriceOracleBean.class);

    @Inject
    @Any
    private Instance<GasPriceOracleSpi> gasPriceOracleTypes;

    public Map<String, GasPriceOracleSpi> getGasPriceOracles() {
        Map<String, GasPriceOracleSpi> result = new HashMap<>();
        Iterator<GasPriceOracleSpi> gasPriceOracleIterator = this.gasPriceOracleTypes.iterator();
        while (gasPriceOracleIterator.hasNext()) {
            GasPriceOracleSpi gasTypeOracle = gasPriceOracleIterator.next();
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

    @Override
    public List<String> getGasPriceOracleNames() {
        List<String> result = new LinkedList<>();
        Iterator<GasPriceOracleSpi> gasPriceOracleIterator = this.gasPriceOracleTypes.iterator();
        while (gasPriceOracleIterator.hasNext()) {
            GasPriceOracleSpi gasTypeOracle = gasPriceOracleIterator.next();
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

    @Override
    public Map<String, BigInteger> getGasPrices(Integer maxDuration) {
        LOGGER.debug("get gas prices for max duration: {}", maxDuration);
        Map<String, GasPriceOracleSpi> gasPriceOracles = getGasPriceOracles();
        Map<String, BigInteger> gasPrices = new HashMap<>();
        for (Map.Entry<String, GasPriceOracleSpi> gasPriceOracleEntry : gasPriceOracles.entrySet()) {
            BigInteger gasPrice = gasPriceOracleEntry.getValue().getGasPrice(maxDuration);
            gasPrices.put(gasPriceOracleEntry.getKey(), gasPrice);
        }
        return gasPrices;
    }

    @Override
    public BigInteger getGasPrice(String oracle, Integer maxDuration) throws UnknownGasPriceOracleException {
        Instance<GasPriceOracleSpi> gasPriceOracleInstance = this.gasPriceOracleTypes.select(new GasPriceOracleTypeQualifier(oracle));
        if (gasPriceOracleInstance.isUnsatisfied()) {
            throw new UnknownGasPriceOracleException();
        }
        if (gasPriceOracleInstance.isAmbiguous()) {
            throw new UnknownGasPriceOracleException();
        }
        GasPriceOracleSpi gasPriceOracle = gasPriceOracleInstance.get();
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
