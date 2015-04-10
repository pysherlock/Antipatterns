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

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class StatisticsCollectorTest {

    private static final String testKey1 = "test1";
    private static final String testKey2 = "test2";
    private static final String testKey3 = "test3";

    @Test
    public void testGetHitCount() {
        StatisticsCollector collector = new StatisticsCollector();
        collector.incerementCounter(testKey1);
        assertEquals(1, collector.getHitCount(testKey1), 0);

        collector.incerementCounter(testKey1);
        assertEquals(2, collector.getHitCount(testKey1), 0);

        collector.incerementCounter(testKey2);
        assertEquals(1, collector.getHitCount(testKey2), 0);

        collector.incerementCounter(testKey2);
        assertEquals(2, collector.getHitCount(testKey2), 0);

        collector.incerementCounter(testKey1);
        assertEquals(3, collector.getHitCount(testKey1), 0);
    }

    @Test
    public void testGetAllHitCount() {
        StatisticsCollector collector = new StatisticsCollector();
        collector.incerementCounter(testKey1);
        assertEquals(1, collector.getHitCountSum(), 0);

        collector.incerementCounter(testKey1);
        assertEquals(2, collector.getHitCountSum(), 0);

        collector.incerementCounter(testKey2);
        assertEquals(3, collector.getHitCountSum(), 0);

        collector.incerementCounter(testKey2);
        assertEquals(4, collector.getHitCountSum(), 0);

        collector.incerementCounter(testKey1);
        assertEquals(5, collector.getHitCountSum(), 0);
    }

    @Test
    public void testGetHitCountList() {
        StatisticsCollector collector = new StatisticsCollector(100000, 100000);

        collector.incerementCounter(testKey1);
        collector.incerementCounter(testKey1);
        collector.incerementCounter(testKey1);

        collector.incerementCounter(testKey2);

        this.sleep(60);

        collector.incerementCounter(testKey3);
        collector.incerementCounter(testKey3);
        collector.incerementCounter(testKey3);
        collector.incerementCounter(testKey3);

        collector.incerementCounter(testKey2);

        Map<String, Long> timeOut50ms = collector.getHits(50);
        Map<String, Long> timeOut70ms = collector.getHits(70);

        assertEquals(3, timeOut50ms.size());

        assertTrue(timeOut50ms.containsKey(testKey3));
        assertEquals(4, timeOut50ms.get(testKey3).longValue());

        assertTrue(timeOut50ms.containsKey(testKey2));
        assertEquals(1, timeOut50ms.get(testKey2).longValue());

        assertTrue(timeOut50ms.containsKey(testKey1));
        assertEquals(0, timeOut50ms.get(testKey1).longValue());

        assertEquals(3, timeOut50ms.size());

        assertTrue(timeOut70ms.containsKey(testKey3));
        assertEquals(4, timeOut70ms.get(testKey3).longValue());

        assertTrue(timeOut70ms.containsKey(testKey1));
        assertEquals(3, timeOut70ms.get(testKey1).longValue());

        assertTrue(timeOut70ms.containsKey(testKey2));
        assertEquals(2, timeOut70ms.get(testKey2).longValue());

    }

    @Test
    public void testGetRequestCount() {
        StatisticsCollector collector = new StatisticsCollector(1000, 1000);

        collector.incerementCounter(testKey1);

        this.sleep(10);
        assertEquals(1, collector.getRequestCount(testKey1, 50), 0);

        this.sleep(300);
        collector.incerementCounter(testKey1);
        assertEquals(1, collector.getRequestCount(testKey1, 50), 0);
        assertEquals(2, collector.getRequestCount(testKey1, 500), 0);

        this.sleep(900);
        collector.incerementCounter(testKey1);
        assertEquals(1, collector.getRequestCount(testKey1, 50), 0);
        assertEquals(1, collector.getRequestCount(testKey1, 500), 0);
        assertEquals(2, collector.getRequestCount(testKey1, 1000), 0);

        this.sleep(100);
        assertEquals(1, collector.getRequestCount(testKey1, 1000), 0);

        // wait for cleaning action
        this.sleep(1000);

        // check that everything was cleaned
        assertEquals(0, collector.getRequestCount(testKey1, 50), 0);
        assertEquals(0, collector.getRequestCount(testKey1, 500), 0);
        assertEquals(0, collector.getRequestCount(testKey1, 1000), 0);

    }

    @Test
    public void testIncerementCounter() {
        StatisticsCollector collector = new StatisticsCollector();
        collector.incerementCounter(testKey1);
        assertEquals(1, collector.getHitCountSum(), 0);

        Map<String, Long> map = collector.getHits(100);

        assertEquals(1, map.size());
        assertTrue(map.containsKey(testKey1));
        assertEquals(1, map.get(testKey1).longValue());
    }

    @Test
    public void testGetAllHitCountMap() {
        StatisticsCollector collector = new StatisticsCollector();

        collector.incerementCounter(testKey1);
        collector.incerementCounter(testKey2);
        collector.incerementCounter(testKey3);
        collector.incerementCounter(testKey1);
        collector.incerementCounter(testKey2);
        collector.incerementCounter(testKey1);

        Map<String, Double> allHitMap = collector.getHits();

        assertEquals(3, allHitMap.size());

        assertTrue(allHitMap.containsKey(testKey1));
        assertTrue(allHitMap.containsKey(testKey2));
        assertTrue(allHitMap.containsKey(testKey3));

        assertEquals(3, allHitMap.get(testKey1), 0);
        assertEquals(2, allHitMap.get(testKey2), 0);
        assertEquals(1, allHitMap.get(testKey3), 0);

        allHitMap.put(testKey1, 40d);
        assertNotSame(40, collector.getHits().get(testKey1));
    }

    @Test
    public void testPutKey() {
        StatisticsCollector collector = new StatisticsCollector();
        collector.putKey(testKey1);

        assertEquals(0, collector.getHitCountSum(), 0);
        assertEquals(1, collector.getHits(100).size(), 0);

        assertEquals(0, collector.getRequestCount(testKey1, 100));
        assertEquals(0, collector.getHits(100).get(testKey1).floatValue(), 0);
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (final InterruptedException e) {
            throw new RuntimeException("Should never happens!");
        }
    }

}
