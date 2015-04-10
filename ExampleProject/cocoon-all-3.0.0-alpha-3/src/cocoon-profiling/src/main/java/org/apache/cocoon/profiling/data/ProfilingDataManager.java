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

/**
 * The {@link ProfilingDataManager} is responsible for building the data structure by linking the
 * {@link ProfilingData} elements together.
 * 
 * <p>
 * The creation of the data structure is solely in the responsibility of the
 * {@link ProfilingDataManager} and has to be accomplished using the information stored in the
 * {@link ProfilingData} objects. Furthermore it has to group the incoming {@link ProfilingData}
 * objects and to move the whole data structure to the {@link ProfilingDataHolder} after one group
 * was completely processed.
 * </p>
 */
public interface ProfilingDataManager {

    /**
     * Add and process the given profiling data.
     * 
     * @param data
     */
    public void add(ProfilingData data);

    public void setProfilingDataHolder(ProfilingDataHolder profilingDataHolder);
}
