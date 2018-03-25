/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra.oracle.std;

import be.e_contract.ethereum.ra.api.EthereumConnection;
import be.e_contract.ethereum.ra.api.EthereumConnectionFactory;
import be.e_contract.ethereum.ra.oracle.GasPriceOracle;
import be.e_contract.ethereum.ra.oracle.GasPriceOracleType;
import java.math.BigInteger;
import javax.annotation.Resource;
import javax.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default Gas Price Oracle.
 *
 * @author Frank Cornelis
 */
@GasPriceOracleType("default")
public class DefaultGasPriceOracle implements GasPriceOracle {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGasPriceOracle.class);

    @Resource(mappedName = "java:/EthereumConnectionFactory")
    private EthereumConnectionFactory ethereumConnectionFactory;

    @Override
    public BigInteger getGasPrice(Integer maxDuration) {
        // TODO: add oracle logic here
        try (EthereumConnection ethereumConnection = (EthereumConnection) this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.getGasPrice();
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }
}
