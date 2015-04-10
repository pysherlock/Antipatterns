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
package org.apache.cocoon.profiling.data;

import java.util.Comparator;

/**
 * Comparator that compares profiling data objects using their invocation start time.
 */
public class ProfilingDataComparator implements Comparator<ProfilingData> {

    /**
     * {@inheritDoc}
     */
    public int compare(ProfilingData arg0, ProfilingData arg1) {
        return (int) Math.signum(arg0.getInvocationStartTime() - arg1.getInvocationStartTime());
    }
}
