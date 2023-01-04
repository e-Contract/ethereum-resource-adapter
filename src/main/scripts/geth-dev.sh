#!/bin/bash
geth --dev --http --http.api personal,eth,net,web3 --dev.period 1 --ws --allow-insecure-unlock
