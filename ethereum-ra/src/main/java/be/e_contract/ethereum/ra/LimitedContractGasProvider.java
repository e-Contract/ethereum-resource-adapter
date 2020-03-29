/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018-2020 e-Contract.be BV.
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
import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * Contract gas provider that limits the gas limit according to the latest
 * block. This to avoid "exceeds block gas limit" transaction errors.
 *
 * @author Frank Cornelis
 */
public class LimitedContractGasProvider implements ContractGasProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(LimitedContractGasProvider.class);

    private final ContractGasProvider contractGasProvider;

    private final Web3j web3j;

    public LimitedContractGasProvider(ContractGasProvider contractGasProvider, Web3j web3j) {
        this.contractGasProvider = contractGasProvider;
        this.web3j = web3j;
    }

    @Override
    public BigInteger getGasPrice(String contractFunc) {
        return this.contractGasProvider.getGasPrice(contractFunc);
    }

    @Override
    public BigInteger getGasPrice() {
        return this.contractGasProvider.getGasPrice();
    }

    @Override
    public BigInteger getGasLimit(String contractFunc) {
        BigInteger gasLimit = this.contractGasProvider.getGasLimit(contractFunc);
        BigInteger limitedGasLimit = getLimitedGasLimit(gasLimit);
        return limitedGasLimit;
    }

    @Override
    public BigInteger getGasLimit() {
        BigInteger gasLimit = this.contractGasProvider.getGasLimit();
        BigInteger limitedGasLimit = getLimitedGasLimit(gasLimit);
        return limitedGasLimit;
    }

    private BigInteger getLimitedGasLimit(BigInteger gasLimit) {
        BigInteger blockNumber;
        try {
            blockNumber = this.web3j.ethBlockNumber().send().getBlockNumber();
        } catch (IOException ex) {
            LOGGER.error("get block number error: " + ex.getMessage(), ex);
            return gasLimit;
        }
        EthBlock.Block block;
        try {
            block = this.web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true).send().getBlock();
        } catch (IOException ex) {
            LOGGER.error("get block error: " + ex.getMessage(), ex);
            return gasLimit;
        }
        BigInteger blockGasLimit = block.getGasLimit();
        if (blockGasLimit.compareTo(gasLimit) == -1) {
            LOGGER.warn("limiting gas limit to {}", blockGasLimit);
            return blockGasLimit;
        }
        return gasLimit;
    }
}
