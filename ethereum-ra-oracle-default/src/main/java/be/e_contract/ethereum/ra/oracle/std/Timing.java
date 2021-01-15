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

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.PriorityQueue;

public class Timing {

    public static final int MOVING_WINDOW_SIZE_MINUTES = 5;

    private final BigInteger gasPrice;
    private Duration totalDuration;
    private int count;

    private final PriorityQueue<TimingEntry> timingEntries;

    public Timing(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
        this.totalDuration = Duration.ZERO;
        this.count = 0;
        this.timingEntries = new PriorityQueue<>();
    }

    public synchronized void addTiming(LocalDateTime transactionReceived, LocalDateTime blockReceived) {
        Duration duration = Duration.between(transactionReceived, blockReceived);
        this.totalDuration = this.totalDuration.plus(duration);
        this.count++;
        this.timingEntries.add(new TimingEntry(blockReceived, duration));
    }

    public synchronized boolean cleanMovingWindow(LocalDateTime now) {
        TimingEntry timingEntry = this.timingEntries.peek();
        while (null != timingEntry) {
            LocalDateTime created = timingEntry.getCreated();
            if (created.plusMinutes(MOVING_WINDOW_SIZE_MINUTES).isAfter(now)) {
                break;
            }
            // remove old entry
            timingEntry = this.timingEntries.poll();
            this.count--;
            this.totalDuration = this.totalDuration.minus(timingEntry.getDuration());
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
        return this.totalDuration.dividedBy(this.count).getSeconds();
    }

    public int getCount() {
        return this.count;
    }

    public BigInteger getGasPrice() {
        return this.gasPrice;
    }
}
