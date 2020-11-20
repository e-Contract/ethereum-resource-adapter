#!/bin/bash

if [ $# == 0 ]
then
	echo "Usage: $0 <address>"
	exit 1
fi

ADDRESS=$1

BESU_HOME=$HOME/besu-20.10.1
$BESU_HOME/bin/besu --network=dev --miner-enabled --miner-coinbase=$ADDRESS --rpc-http-cors-origins="all" --host-allowlist="*" --rpc-ws-enabled --rpc-http-enabled