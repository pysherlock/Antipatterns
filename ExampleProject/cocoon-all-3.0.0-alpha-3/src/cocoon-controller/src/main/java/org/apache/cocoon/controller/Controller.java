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
package org.apache.cocoon.controller;

import java.io.OutputStream;
import java.util.Map;

import org.apache.cocoon.pipeline.caching.CacheKey;

/**
 * The invocation of a controller is separated into two phases: The setup phase
 * {@link Controller#setup(String, Map, Map)} and the execution phase
 * {@link Controller#execute(OutputStream). After the setup phase a controller must already provide
 * all meta data that are necessary (status codes, mime-types). The execution phase only sends the
 * result as {@link OutputStream}.
 */
public interface Controller {

    /**
     * @param outputStream
     * @return The mime-type of the returned output stream.
     */
    void execute(OutputStream outputStream);

    /**
     * @param beanName
     * @param inputParameters
     * @param configuration
     */
    void setup(String beanName, Map<String, Object> inputParameters, Map<String, ? extends Object> configuration);

    /**
     * This method returns the cache key that might be available after the setup phase.
     * 
     * @return A {@link CacheKey} for the controller or <code>null</code> if no cache key can be
     *         calculated.
     */
    CacheKey getCacheKey();
}
