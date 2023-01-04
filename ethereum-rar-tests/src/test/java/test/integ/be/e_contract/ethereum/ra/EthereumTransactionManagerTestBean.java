/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2023 e-Contract.be BV.
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
import be.e_contract.ethereum.utils.EthereumFinalState;
import be.e_contract.ethereum.utils.EthereumPublicationEvent;
import be.e_contract.ethereum.utils.EthereumTransactionManager;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.Security;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import static org.junit.Assert.assertTrue;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Convert;

@Singleton
@Lock(LockType.READ)
public class EthereumTransactionManagerTestBean {

    @Resource(lookup = "java:/EthereumConnectionFactory")
    private EthereumConnectionFactory ethereumConnectionFactory;

    @Inject
    private EthereumTransactionManager ethereumTransactionManager;

    private String transactionHash;

    public void run() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        try ( EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
            List<String> accounts = ethereumConnection.getAccounts();
            String account = accounts.get(0);
            String password = "";
            boolean unlockResult = ethereumConnection.unlockAccount(account, password);
            assertTrue(unlockResult);

            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            String address = "0x" + Keys.getAddress(ecKeyPair);

            BigInteger gasPrice = ethereumConnection.getGasPrice();
            BigInteger nonce = ethereumConnection.getTransactionCount(account);
            BigInteger value = Convert.toWei(BigDecimal.valueOf(10), Convert.Unit.ETHER).toBigInteger();
            this.transactionHash = ethereumConnection.sendAccountTransaction(account, address, value, gasPrice, nonce);
            this.ethereumTransactionManager.monitorTransaction(this.transactionHash);
        } finally {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
        synchronized (this.transactionHash) {
            this.transactionHash.wait();
        }
    }

    public void observeEthereumEvent(@Observes EthereumPublicationEvent event) {
        if (this.transactionHash.equals(event.getTransactionHash())) {
            if (event.getFinalState() == EthereumFinalState.SUCCEEDED) {
                synchronized (this.transactionHash) {
                    this.transactionHash.notify();
                }
            }
        }
    }
}
