/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra.oracle.std;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

public class Timing {

    private Duration totalTime;
    private int count;

    public Timing(DateTime created) {
        DateTime now = new DateTime();
        Interval interval = new Interval(created, now);
        Duration duration = interval.toDuration();
        this.totalTime = duration;
        this.count++;
    }

    public synchronized void addTiming(DateTime created, DateTime blockTimestamp) {
        // seems like blockTimestamp can be before created in the beginning...
        // so we still have to use now here
        DateTime now = new DateTime();
        Interval interval = new Interval(created, now);
        Duration duration = interval.toDuration();
        this.totalTime = this.totalTime.plus(duration);
        this.count++;
    }

    /**
     * Gives back the average time in seconds.
     *
     * @return
     */
    public long getAverageTime() {
        return this.totalTime.dividedBy(this.count).getStandardSeconds();
    }

    public int getCount() {
        return this.count;
    }
}
