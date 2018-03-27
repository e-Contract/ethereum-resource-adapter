/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018 e-Contract.be BVBA.
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

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class TimingEntry implements Comparable<TimingEntry> {

    private final DateTime created;
    private final Duration duration;

    public TimingEntry(DateTime created, Duration duration) {
        this.created = created;
        this.duration = duration;
    }

    public DateTime getCreated() {
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
