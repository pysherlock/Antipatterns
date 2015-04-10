/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.monitoring.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class StatisticsCollector implements StatisticsSourceEnabled {

    /**
    * Default refresh time: 10s;
    */
    public static final long DEFAULT_REFRESH_DELAY = 1000 * 10;

    /**
    * Default value of maxKeepTime: 24h.
    */
    public static final long DEFAULT_MAX_KEEP_TIME = 1000 * 60 * 60 * 24;

    private final Map<String, Double> allHitCount;
    private final Map<String, List<Long>> coutners;
    private final long maxKeepTime;

    /**
     * This constructor uses default values of {@link StatisticsCollector#DEFAULT_MAX_KEEP_TIME DEFAULT_MAX_KEEP_TIME},
     * and {@link StatisticsCollector#DEFAULT_REFRESH_DELAY DEFAULT_REFRESH_DELAY} to pass into
     * {@link StatisticsCollector#StatisticsCollector(long, long) StatisticsCollector(long, long)}
     */
    public StatisticsCollector() {
        this(DEFAULT_MAX_KEEP_TIME, DEFAULT_REFRESH_DELAY);
    }

    /**
     *
     * @param maxKeepTime how long (in milliseconds) should be statistics data kept in collector
     * @param refreshDelay delay time (in milliseconds) between run of cleaning thread, that will remove entry's
     *          are older than value in <code>maxKeepTime</code>
     */
    public StatisticsCollector(long maxKeepTime, long refreshDelay) {
        this.maxKeepTime = maxKeepTime;
        this.allHitCount = Collections.synchronizedMap(new HashMap<String, Double>());
        this.coutners = Collections.synchronizedMap(new HashMap<String, List<Long>>());

        this.initCleaningThread(refreshDelay);
    }

    /** @{inheritDoc} */
    public Map<String, Double> getHits() {
        return new HashMap<String, Double>(this.allHitCount); // defense copy
    }

    /** @{inheritDoc} */
    public Map<String, Long> getHits(long time) {
        final Map<String, Long> result = new HashMap<String, Long>();
        long timeBorder = new Date().getTime() - time;

        for (String key : this.coutners.keySet()) {
            long sum = 0;
            for (long item : this.coutners.get(key)) {
                if (item > timeBorder) {
                    sum++;
                }
            }
            result.put(key, sum);
        }

        return result;
    }

    /** @{inheritDoc} */
    public double getHitCount(String key) {
        return this.allHitCount.containsKey(key) ? this.allHitCount.get(key) : 0;
    }

    /** @{inheritDoc} */
    public double getHitCountSum() {
        double result = 0;
        for (Double count : this.allHitCount.values()) {
            result += count;
        }
        return result;
    }

    /** @{inheritDoc} */
    public long getRequestCount(String key, long time) {
        if (!this.coutners.containsKey(key)) {
            return 0;
        }

        List<Long> counter = this.coutners.get(key);

        long hitCount = 0;
        long currentTimestamp = new Date().getTime() - time;

        for (Long timestamp : counter) {
            if (timestamp > 0 && currentTimestamp < timestamp) {
                hitCount++;
            }
        }

        return hitCount;
    }

    /**
     * Only adds key into counter but don't increment hit count for this
     * <code>key</code>. It is useful if you want to have this <code>key</code> in a list
     * of all used key's with value <strong>0</strong> (i.e. for registered but never used
     * cache entry's).
     *
     * @param key
     */
    public void putKey(String key) {
        this.insertDataIntoCounter(key, -1l);
    }

    /**
     * Increment value of counter for particular <code>key</code>.
     *
     * <p>In fact this method adds actual time (in milliseconds) into list that is connected with
     * this <code>key</code>.
     *
     * @param key
     */
    public void incerementCounter(String key) {
        this.insertDataIntoCounter(key, new Date().getTime());
    }

    private void insertDataIntoCounter(String key, long data) {
        if (!this.coutners.containsKey(key)) {
            List<Long> list = new ArrayList<Long>();
            list.add(data);
            this.coutners.put(key, list);
        } else {
            this.coutners.get(key).add(data);
        }

        if (this.allHitCount.containsKey(key) && data > 0) {
            this.allHitCount.put(key, this.allHitCount.get(key) + 1);
        } else if (data > 0) {
            this.allHitCount.put(key, 1d);
        } else {
            this.allHitCount.put(key, 0d);
        }
    }

    private void initCleaningThread(long refreshDelay) {
        Timer cleaningTimer = new Timer("RequestCounterCleaningTask", true);
        cleaningTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                List<Long> toRemove = new ArrayList<Long>();
                long currentTimestamp = new Date().getTime();

                for (List<Long> counter : StatisticsCollector.this.coutners.values()) {
                    for (Long timestamp : counter) {
                        if (timestamp > 0 && currentTimestamp - timestamp > StatisticsCollector.this.maxKeepTime) {
                            toRemove.add(timestamp);
                        }
                    }
                    counter.removeAll(toRemove);
                    toRemove.clear();
                }

            }
        }, refreshDelay, refreshDelay);
    }
}
