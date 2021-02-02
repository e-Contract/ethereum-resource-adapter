#!/bin/bash

if [ $# == 0 ]
then
	echo "Usage: $0 <your ethereum address on which will be mined>"
	exit 1
fi

ADDRESS=$1
echo "Miner address: $ADDRESS"

BESU_HOME=$HOME/besu-20.10.4
$BESU_HOME/bin/besu --network=dev --miner-enabled --miner-coinbase="$ADDRESS" --rpc-http-cors-origins="all" --host-allowlist="*" --rpc-ws-enabled --rpc-http-enabled
