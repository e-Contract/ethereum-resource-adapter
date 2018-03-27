/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
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
