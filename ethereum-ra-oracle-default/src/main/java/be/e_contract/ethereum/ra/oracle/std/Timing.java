/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra.oracle.std;

import java.util.PriorityQueue;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

public class Timing {

    private Duration totalTime;
    private int count;

    private final PriorityQueue<TimingEntry> timingEntries;

    public Timing() {
        this.totalTime = new Duration(0);
        this.count = 0;
        this.timingEntries = new PriorityQueue<>();
    }

    public synchronized void addTiming(DateTime transactionReceived, DateTime blockReceived) {
        Interval interval = new Interval(transactionReceived, blockReceived);
        Duration duration = interval.toDuration();
        this.totalTime = this.totalTime.plus(duration);
        this.count++;
        this.timingEntries.add(new TimingEntry(blockReceived, duration));
    }

    public synchronized boolean cleanMovingWindow(DateTime now) {
        TimingEntry timingEntry = this.timingEntries.peek();
        while (null != timingEntry) {
            DateTime created = timingEntry.getCreated();
            if (created.plusMinutes(5).isAfter(now)) {
                break;
            }
            // remove old entry
            timingEntry = this.timingEntries.poll();
            this.count--;
            this.totalTime = this.totalTime.minus(timingEntry.getDuration());
            timingEntry = this.timingEntries.peek();
        }
        return this.count == 0;
    }

    /**
     * Gives back the average time in seconds.
     *
     * @return
     */
    public long getAverageTime() {
        if (this.count == 0) {
            return Long.MAX_VALUE;
        }
        return this.totalTime.dividedBy(this.count).getStandardSeconds();
    }

    public int getCount() {
        return this.count;
    }
}
