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
 * Unless required by applicable law or agreed to in writing,in ascending order
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.monitoring.statistics;

import java.util.Map;

public interface StatisticsSourceEnabled {

    /**
     * Returns sum for all hit count for given key. It will return <strong>0<strong> if given key does not
     * have any statistics data (that means, that <strong>0</strong> if given key does not exist in statistics
     * source).
     *
     * @param key
     * @return hit count for passed arguments
     */
    public double getHitCount(String key);

    /**
     * Returns all hit count for this statistics source (sum of all hit counts for all contained key's).
     *
     * @return
     */
    double getHitCountSum();

    /**
     * Returns unordered {@link Map} of summed value of all hit
     *
     * @return
     */
    Map<String, Double> getHits();

    /**
     * Returns unordered {@link Map}, where <code>key</code> is source name and <code>value</code> is
     * sum of hit count, limited only to entry's that are younger then <code>time</code> parameter
     *
     * @param time
     * @return
     */
    Map<String, Long> getHits(long time);

    /**
     * Returns hit count for particular <code>key</code> in particular <code>time</code>
     *
     * @param key
     * @param time
     * @return
     */
    long getRequestCount(String key, long time);

}
