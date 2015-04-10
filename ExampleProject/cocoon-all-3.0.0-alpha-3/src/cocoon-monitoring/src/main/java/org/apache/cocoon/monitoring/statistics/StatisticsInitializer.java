/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.monitoring.statistics;

import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jmx.export.MBeanExporter;

public class StatisticsInitializer {

    private final Log logger = LogFactory.getLog(this.getClass());

    public StatisticsInitializer(Map<String, StatisticsEnabled> statsSources, MBeanExporter exporter) {
        for (StatisticsEnabled stat : statsSources.values()) {
            String stringName = "org.apache.cocoon:group=Statistics,name=" + stat.statisticsSourceName();

            ObjectName name;
            try {
                name = new ObjectName(stringName);
            } catch (MalformedObjectNameException e) {
                this.logger.error("Invalid name of manager resource: " + stringName, e);
                continue;
            } catch (NullPointerException e) {
                this.logger.error("Should never happened. Value of name parameter always is different than null.", e);
                continue;
            }

            exporter.registerManagedResource(new Statistics(stat), name);
        }
    }
}
