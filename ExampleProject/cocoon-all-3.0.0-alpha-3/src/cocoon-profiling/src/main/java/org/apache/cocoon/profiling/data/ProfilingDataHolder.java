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
 * Interface for the storage layer of the profiling module. Supports storage and retrieval of
 * {@link ProfilingData} objects based on a key.
 * <p>
 * The stored {@link ProfilingData} objects may be discarded at any point in time.
 * </p>
 */
public interface ProfilingDataHolder {

    /**
     * Store the given {@link ProfilingData} objects using the given id as key.
     * 
     * @param id
     * @param data
     */
    void store(String id, ProfilingData data);

    /**
     * Retrieve the {@link ProfilingData} objects stored with the given id as key.
     * 
     * @param id
     * @return the {@link ProfilingData} stored with the given id as key or null if there is no
     *         mapping for the given id.
     */
    ProfilingData get(String id);
}
