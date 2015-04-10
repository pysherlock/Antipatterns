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
package org.apache.cocoon.monitoring.statistics.aspects;

import org.apache.cocoon.monitoring.statistics.StatisticsCollector;
import org.apache.cocoon.monitoring.statistics.StatisticsEnabled;
import org.apache.cocoon.monitoring.statistics.StatisticsSourceEnabled;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class ServletHitCountStatisticsAspect implements StatisticsEnabled {

    private final StatisticsCollector collector;

    public ServletHitCountStatisticsAspect() {
        this(StatisticsCollector.DEFAULT_MAX_KEEP_TIME, StatisticsCollector.DEFAULT_REFRESH_DELAY);
    }

    public ServletHitCountStatisticsAspect(long maxKeepTime, long refreshDelaty) {
        this.collector = new StatisticsCollector(maxKeepTime, refreshDelaty);
    }

    @Around("execution(* service(..)) && target(javax.servlet.Servlet))")
    public Object handleServletRequest(ProceedingJoinPoint pjp) throws Throwable {
        String className = pjp.getTarget().getClass().getName();
        if (!className.startsWith("$Proxy")) {
            this.collector.incerementCounter(className);
        }
        return pjp.proceed(pjp.getArgs());
    }

    public StatisticsSourceEnabled getStatistics() {
        return this.collector;
    }

    public String statisticsSourceName() {
        return "ServletHitCount";
    }
}