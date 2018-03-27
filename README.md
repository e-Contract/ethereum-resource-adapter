Ethereum Java EE JCA Resource Adapter
=====================================

This project delivers a standard Java EE JCA Resource Adapter to connect to Ethereum networks.

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
@Resource(mappedName = "java:/EthereumConnectionFactory")
private EthereumConnectionFactory ethereumConnectionFactory;

try (EthereumConnection ethereumConnection = (EthereumConnection) this.ethereumConnectionFactory.getConnection()) {
    ethereumConnection.sendRawTransaction(rawTransaction);
}
```

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