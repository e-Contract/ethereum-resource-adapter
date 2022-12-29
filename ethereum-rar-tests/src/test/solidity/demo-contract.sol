/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018-2022 e-Contract.be BV.
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
 
// SPDX-License-Identifier: LGPL-3.0
pragma solidity >=0.8.13;

contract DemoContract {
    event DemoEvent(address _from, int _value);
    int value;
    address payable owner;

    constructor() {
        owner = payable(msg.sender);
    }

    function getValue() public view returns (int) {
        return value;
    }

    function setValue(int _value) public {
        value = _value;
        emit DemoEvent(msg.sender, _value);
    }

    function kill() public {
        if (msg.sender == owner) {
            selfdestruct(owner);
        }
    }
}

