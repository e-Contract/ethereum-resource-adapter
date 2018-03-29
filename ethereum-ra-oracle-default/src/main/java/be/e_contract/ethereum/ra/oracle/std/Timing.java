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

import java.math.BigInteger;
import java.util.PriorityQueue;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

public class Timing {

    public static final int MOVING_WINDOW_SIZE_MINUTES = 5;

    private final BigInteger gasPrice;
    private Duration totalTime;
    private int count;

    private final PriorityQueue<TimingEntry> timingEntries;

    public Timing(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
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
            if (created.plusMinutes(MOVING_WINDOW_SIZE_MINUTES).isAfter(now)) {
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

    public BigInteger getGasPrice() {
        return this.gasPrice;
    }
}
