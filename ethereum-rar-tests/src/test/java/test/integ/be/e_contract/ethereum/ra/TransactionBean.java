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
package test.integ.be.e_contract.ethereum.ra;

import be.e_contract.ethereum.ra.api.EthereumConnection;
import be.e_contract.ethereum.ra.api.EthereumConnectionFactory;
import be.e_contract.ethereum.ra.api.TransactionConfirmation;
import java.math.BigInteger;
import java.security.Security;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

@Stateless
public class TransactionBean {

    @Resource(mappedName = "java:/EthereumConnectionFactory")
    private EthereumConnectionFactory ethereumConnectionFactory;

    public void performTransaction() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        try (EthereumConnection ethereumConnection = (EthereumConnection) this.ethereumConnectionFactory.getConnection()) {
            List<String> accounts = ethereumConnection.getAccounts();
            String account = accounts.get(0);
            String password = "";
            boolean unlockResult = ethereumConnection.unlockAccount(account, password);
            assertTrue(unlockResult);

            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            String address = "0x" + Keys.getAddress(ecKeyPair);

            BigInteger gasPrice = ethereumConnection.getGasPrice();
            BigInteger nonce = ethereumConnection.getTransactionCount(account);
            BigInteger value = BigInteger.valueOf(10);
            String transactionHash = ethereumConnection.sendAccountTransaction(account, address, value, gasPrice, nonce);

            TransactionConfirmation transactionConfirmation = ethereumConnection.getTransactionConfirmation(transactionHash);
            while (transactionConfirmation.getConfirmingBlocks() == 0) {
                Thread.sleep(1000);
                transactionConfirmation = ethereumConnection.getTransactionConfirmation(transactionHash);
            }
            BigInteger balance = ethereumConnection.getBalance(address);
            assertEquals(value, balance);
        } finally {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }
}
