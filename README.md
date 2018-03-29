Ethereum Java EE JCA Resource Adapter
=====================================

This project delivers a standard Java EE JCA Resource Adapter to connect to Ethereum networks.

Tested Java EE application servers:

* JBoss EAP 6.4.19
* JBoss WildFly 12

We should be able to support every Java EE 6+ application server.

We target the Java EE JCA version 1.6 specification.

If you like this project, please consider a donation at:
```
0x0c56073db91c2ba57ff362301eb32262bbee6147
```


# Features

Why would you want to use such a thing as a JCA Resource Adapter to connect to an Ethereum network?

What does it offer more compared to a library such as web3j for instance?

## Transaction support

This JCA Resource Adapter gives you transaction support within your Java EE application when transmitting Ethereum transactions towards an Ethereum network.

Simply use the JCA Connector, and you will automatically have transaction support within your Java EE application.

Example code:
```
@Resource(name = "EthereumConnectionFactory")
private EthereumConnectionFactory ethereumConnectionFactory;

try (EthereumConnection ethereumConnection = (EthereumConnection) this.ethereumConnectionFactory.getConnection()) {
    ethereumConnection.sendRawTransaction(rawTransaction);
}
```
The transaction implementation also features automatic retry on commit just in case the client node is temporarily unavailable.


## Message Listener

We provide a robust message listener API that features automatic recovery from a failed client node.


Example code:
```
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "deliverPending", propertyValue = "true"),
    @ActivationConfigProperty(propertyName = "deliverBlock", propertyValue = "true")
})
public class EthereumMDB implements EthereumMessageListener {

    @Override
    public void pendingTransaction(String transactionHash, Date timestamp) throws Exception {
        
    }

    @Override
    public void block(String blockHash, Date timestamp) throws Exception {
        
    }

    @Override
    public void connectionStatus(boolean connected) throws Exception {
        
    }
}
```

The resource adapter will automatically attempt to reconnect when a client node goes down for a while.


## Gas Price Oracle

We provide a modular gas price oracle.

Of course we provide a default gas price oracle, next to the client node gas price oracle, that allows you to ask for a gas price given a maximum duration you're willing to wait for the transaction to be mined within the Ethereum network.

Example code:
```
@EJB
private GasPriceOracleBean gasPriceOracleBean;

Integer maxDurationInSeconds = 60;
String oracle = "default";
BigInteger gasPrice = this.gasPriceOracleBean.getGasPrice(oracle, maxDurationInSeconds);
```

Depending on your max duration, this gas price oracle gives you a gas price that is on average 30-50% lower than the gas price set by the client node.


# Usage

This section provides usage information per Java EE application server.

## JBoss EAP/WildFly

Within your EAR, you need to include the RAR.

If you use Maven, refer to the e-contract.be Maven repository via:
```
<repository>
    <id>e-contract</id>
    <url>https://www.e-contract.be/maven2/</url>
    <releases>
        <enabled>true</enabled>
    </releases>
</repository>
```

Include within your Java EE application EAR the resource adapter RAR via:
```
<dependency>
    <groupId>be.e-contract.ethereum-resource-adapter</groupId>
    <artifactId>ethereum-rar</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <type>rar</type>
</dependency>
```

and as `maven-ear-plugin` configuration under `modules` you put:
```
<rarModule>
    <groupId>be.e-contract.ethereum-resource-adapter</groupId>
    <artifactId>ethereum-rar</artifactId>
    <bundleFileName>ethereum-ra.rar</bundleFileName>
</rarModule>
```

The `EthereumConnectionFactory` will be available within JNDI under:
```
java:/EthereumConnectionFactory
```
Hence you can refer to it via:
```
@Resource(mappedName = "java:/EthereumConnectionFactory")
private EthereumConnectionFactory ethereumConnectionFactory;
```
or in an application server independent way via:
```
@Resource(name = "EthereumConnectionFactory")
private EthereumConnectionFactory ethereumConnectionFactory;
```
where you provide within your `META-INF/jboss-ejb3.xml` to following mapping:
```
<jboss:enterprise-beans>
    <session>
        <ejb-name>EthereumBean</ejb-name>
        <resource-ref>
            <res-ref-name>EthereumConnectionFactory</res-ref-name>
            <jndi-name>java:/EthereumConnectionFactory</jndi-name>
        </resource-ref>
    </session>
</jboss:enterprise-beans>
```

For usage of the `EthereumMessageListener` you need to refer to the resource adapter explicitly. Do this by adding the following to your `META-INF/jboss-ejb3.xml`:
```
<assembly-descriptor>
    <mdb:resource-adapter-binding>
        <ejb-name>YourEthereumMDB</ejb-name>
        <mdb:resource-adapter-name>your-ear-file.ear#ethereum-ra.rar</mdb:resource-adapter-name>
    </mdb:resource-adapter-binding>
</assembly-descriptor>
```

For usage of the gas price oracle, you need to include the corresponding EJB within the EAR via:
```
<dependency>
    <groupId>be.e-contract.ethereum-resource-adapter</groupId>
    <artifactId>ethereum-ra-oracle</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <type>ejb</type>
</dependency>
```

and as `maven-ear-plugin` configuration under `modules` you put:
```
<ejbModule>
    <groupId>be.e-contract.ethereum-resource-adapter</groupId>
    <artifactId>ethereum-ra-oracle</artifactId>
</ejbModule>
```

And per gas price oracle implemention, you include the corresponding CDI JAR within the EAR. For the default oracle you include:
```
<dependency>
    <groupId>be.e-contract.ethereum-resource-adapter</groupId>
    <artifactId>ethereum-ra-oracle-default</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

# Development

Build the project via Maven:
```
mvn clean install
```

We use Netbeans as IDE.
If you send pull requests, please keep the code clean to ease the review process.