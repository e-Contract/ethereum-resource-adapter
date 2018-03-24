/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.rar.demo;

import be.e_contract.ethereum.ra.EthereumConnection;
import be.e_contract.ethereum.ra.EthereumConnectionFactory;
import be.e_contract.ethereum.ra.EthereumConnectionRequestInfo;
import java.math.BigInteger;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.resource.ResourceException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class EthereumBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumBean.class);

    @Resource(mappedName = "java:/EthereumConnectionFactory")
    private EthereumConnectionFactory ethereumConnectionFactory;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public BigInteger getGasPrice(String nodeLocation) {
        EthereumConnectionRequestInfo ethereumConnectionRequestInfo;
        if (StringUtils.isEmpty(nodeLocation)) {
            ethereumConnectionRequestInfo = null;
        } else {
            ethereumConnectionRequestInfo = new EthereumConnectionRequestInfo(nodeLocation);
        }
        try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection(ethereumConnectionRequestInfo)) {
            return ethereumConnection.getGasPrice();
        } catch (ResourceException ex) {
            LOGGER.error("JCA error: " + ex.getMessage(), ex);
            return null;
        }
    }
}
