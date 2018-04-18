Ethereum Java EE JCA Resource Adapter
=====================================

This project delivers a standard Java EE JCA Resource Adapter to connect to Ethereum networks.

Tested Java EE application servers:

* JBoss EAP 6.4.19
* JBoss WildFly 12.0.0
* Oracle WebLogic 12.2.1.3.0

We should be able to support every Java EE 6+ application server.

We target the Java EE JCA version 1.6 specification and Java 8+.

If you like this project, please consider a donation at:
```
0x0c56073db91c2Ba57FF362301eb32262BBeE6147
```


# Features

Why would you want to use such a thing as a JCA Resource Adapter to connect to an Ethereum network?

What does it offer more compared to direct usage of the web3j library?

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

The JCA Resource Adapter even supports JCA transactions on smart contracts written in Solidity.

Furthermore, fast Ethereum transactions are also supported via internal nonce caching.
On a JCA rollback, the corresponding nonce cache gets nicely reset as well.


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
Hence your client node can go down for maintenance without the need to restart your Java EE application server.


## Gas Price Oracle

We provide a modular gas price oracle.

Of course we provide a default gas price oracle, next to the client node gas price oracle, that allows you to ask for a gas price given a maximum duration you're willing to wait for the transaction to be mined within the Ethereum network.

Example code:
```
@EJB
private GasPriceOracle gasPriceOracle;

Integer maxDurationInSeconds = 60;
String oracle = "default";
BigInteger gasPrice = this.gasPriceOracle.getGasPrice(oracle, maxDurationInSeconds);
```

Depending on your max duration, this gas price oracle gives you a gas price that is on average 30-50% lower than the gas price set by the client node.


## JSF Tag Library

The project delivers a JSF Tag Library for validation and conversion of Ethereum types.

Example input validation of an Ethereum address:
```
xmlns:eth="urn:be:e-contract:ethereum:jsf"

<h:inputText value="...">
    <eth:addressValidator/>
</h:inputText>
```

Example conversion from wei to ether:
```
xmlns:eth="urn:be:e-contract:ethereum:jsf"

<h:outputText value="1234">
    <eth:fromWei unit="ETHER"/>
</h:outputText>
```

# Usage

This section provides generic usage information and usage information per Java EE application server.

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


## JBoss EAP/WildFly

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
        <ejb-name>YourBeanNameHere</ejb-name>
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
        <mdb:resource-adapter-name>your-ear-file-name-here.ear#ethereum-ra.rar</mdb:resource-adapter-name>
    </mdb:resource-adapter-binding>
</assembly-descriptor>
```

## Oracle WebLogic

The `EthereumConnectionFactory` will be available within JNDI under:
```
EthereumConnectionFactory
```
So you can refer to it via:
```
@Resource(name = "EthereumConnectionFactory")
private EthereumConnectionFactory ethereumConnectionFactory;
```
where you provide within your `META-INF/weblogic-ejb-jar.xml` to following mapping:
```
<weblogic-enterprise-bean>
    <ejb-name>YourBeanNameHere</ejb-name>
    <resource-description>
        <res-ref-name>EthereumConnectionFactory</res-ref-name>
        <jndi-name>EthereumConnectionFactory</jndi-name>
    </resource-description>
</weblogic-enterprise-bean>
```

For usage of the `EthereumMessageListener` you need to refer to the resource adapter explicitly. Do this by adding the following to your `META-INF/weblogic-ejb-jar.xml`:
```
<weblogic-enterprise-bean>
    <ejb-name>YourEthereumMDB</ejb-name>
    <message-driven-descriptor>
        <resource-adapter-jndi-name>EthereumResourceAdapter</resource-adapter-jndi-name>
    </message-driven-descriptor>
</weblogic-enterprise-bean>
```


# Gas Price Oracle Usage

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

And per gas price oracle implemention, you include the corresponding EJB JAR within the EAR. For the default oracle you include:
```
<dependency>
    <groupId>be.e-contract.ethereum-resource-adapter</groupId>
    <artifactId>ethereum-ra-oracle-default</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <type>ejb</type>
</dependency>
```
and as `maven-ear-plugin` configuration under `modules` you put:
```
<ejbModule>
    <groupId>be.e-contract.ethereum-resource-adapter</groupId>
    <artifactId>ethereum-ra-oracle-default</artifactId>
</ejbModule>
```


# Development

Build the project via Maven:
```
mvn clean install
```

## Demo Web Application

Start a `geth` Ethereum client node in development mode via:
```
geth --dev --rpc --rpcapi personal,eth,net --dev.period 0
```

Start WildFly via:
```
cd wildfly-12.0.0.Final/bin
./standalone.sh --server-config=standalone-full.xml
```

Compile and deploy the demo web application via:
```
mvn clean install
cd ethereum-rar-demo-deploy/
mvn wildfly:deploy
```

The demo web application is now available at:
http://localhost:8080/ethereum-demo/

If you want debug logging, add to `standalone/configuration/standalone-full.xml` under `subsystem xmlns="urn:jboss:domain:logging:4.0"` the following configuration:
```
<periodic-rotating-file-handler name="ETHEREUM" autoflush="true">
    <level name="DEBUG"/>
    <formatter>
        <named-formatter name="PATTERN"/>
    </formatter>
    <file relative-to="jboss.server.log.dir" path="ethereum.log"/>
    <suffix value=".yyyy-MM-dd"/>
    <append value="true"/>
</periodic-rotating-file-handler>
<logger category="be.e_contract.ethereum" use-parent-handlers="false">
    <level name="DEBUG"/>
    <handlers>
        <handler name="ETHEREUM"/>
    </handlers>
</logger>
```

Following the logging via:
```
tail -F standalone/log/ethereum.log
```


## Integration Tests

The project comes with Arquillian based integration tests.
To run the integration tests, you need a local running WildFly and Ethereum client node.

Start WildFly via:
```
cd wildfly-12.0.0.Final/bin
./standalone.sh --server-config=standalone-full.xml
```

Start a `geth` Ethereum client node in development mode via:
```
geth --dev --rpc --rpcapi personal,eth,net --dev.period 0
```

Run the integration tests via:
```
mvn clean install -Pintegration-tests-wildfly
```


## Contributions

We use Netbeans as IDE.
If you send pull requests, please keep the code clean to ease the review process.
