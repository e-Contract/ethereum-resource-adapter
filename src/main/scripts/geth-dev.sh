#!/bin/bash
geth --dev --http --http.api personal,eth,net,web3 --dev.period 0 --ws --allow-insecure-unlock
