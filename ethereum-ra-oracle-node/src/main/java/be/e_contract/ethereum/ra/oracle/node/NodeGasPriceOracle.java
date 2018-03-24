/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra.oracle.node;

import be.e_contract.ethereum.ra.EthereumConnection;
import be.e_contract.ethereum.ra.EthereumConnectionFactory;
import be.e_contract.ethereum.ra.oracle.GasPriceOracle;
import be.e_contract.ethereum.ra.oracle.GasPriceOracleType;
import java.math.BigInteger;
import javax.annotation.Resource;
import javax.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Most simple Gas Price Oracle based on the gas price of the client node.
 *
 * @author Frank Cornelis
 */
@GasPriceOracleType("node")
public class NodeGasPriceOracle implements GasPriceOracle {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeGasPriceOracle.class);

    @Resource(mappedName = "java:/EthereumConnectionFactory")
    private EthereumConnectionFactory ethereumConnectionFactory;

    @Override
    public BigInteger getGasPrice(Integer maxDuration) {
        try (EthereumConnection ethereumConnection = (EthereumConnection) this.ethereumConnectionFactory.getConnection()) {
            return ethereumConnection.getGasPrice();
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }
}
