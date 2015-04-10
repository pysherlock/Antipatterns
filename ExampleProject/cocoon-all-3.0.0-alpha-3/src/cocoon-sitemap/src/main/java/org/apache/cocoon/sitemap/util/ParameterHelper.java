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
package org.apache.cocoon.sitemap.util;

import java.util.Map;

public class ParameterHelper {

    private static final String THROWABLE_KEY = java.lang.Throwable.class.getName();

    public static Throwable getThrowable(Map<String, ? extends Object> parameters) {
        return (Throwable) parameters.get(THROWABLE_KEY);
    }

    public static void setThrowable(Map<String, Object> parameters, Throwable throwable) {
        parameters.put(THROWABLE_KEY, throwable);
    }
}
