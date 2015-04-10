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

import java.util.Map;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource
public class Statistics {

    private final StatisticsSourceEnabled stats;

    public Statistics(StatisticsEnabled stats) {
        this.stats = stats.getStatistics();
    }

    @ManagedAttribute(description = "Returns all hit count since system start.")
    public double getAllHitCount() {
        return this.stats.getHitCountSum();
    }

    @ManagedAttribute(description = "Returns a map of all hits and their counts.")
    public Map<String, Double> getHits() {
        return this.stats.getHits();
    }

    @ManagedOperation(description = "Returns a map of all hits and their counts limited by a time parameter.")
    @ManagedOperationParameters( { @ManagedOperationParameter(name = "time", description = "Time in miliseconds.") })
    public Map<String, Long> getHits(long time) {
        return this.stats.getHits(time);
    }
}
