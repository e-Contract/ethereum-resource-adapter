#!/bin/bash
openethereum --config dev --base-path dev --jsonrpc-apis=personal,eth,net,web3,parity --ws-origins=http://localhost
echo "You might want to cleanup the dev directory..."
