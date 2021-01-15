/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018-2021 e-Contract.be BV.
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
package be.e_contract.ethereum.ra.oracle.std;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimingEntry implements Comparable<TimingEntry> {

    private final LocalDateTime created;
    private final Duration duration;

    public TimingEntry(LocalDateTime created, Duration duration) {
        this.created = created;
        this.duration = duration;
    }

    public LocalDateTime getCreated() {
        return this.created;
    }

    public Duration getDuration() {
        return this.duration;
    }

    @Override
    public int compareTo(TimingEntry t) {
        return this.created.compareTo(t.getCreated());
    }
}
