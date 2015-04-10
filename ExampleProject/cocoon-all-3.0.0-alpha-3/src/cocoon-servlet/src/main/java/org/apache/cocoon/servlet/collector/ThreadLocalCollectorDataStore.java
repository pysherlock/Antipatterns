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
package org.apache.cocoon.servlet.collector;

import java.util.HashMap;
import java.util.Map;

public class ThreadLocalCollectorDataStore implements CollectorDataStore {

    private static ThreadLocal<Map<String, Object>> threadStore = new ThreadLocal<Map<String, Object>>();

    public ThreadLocalCollectorDataStore() {
        threadStore.set(new HashMap<String, Object>());
    }

    public Object get(String key) {
        return threadStore.get().get(key);
    }

    public void set(String key, Object value) {
        threadStore.get().put(key, value);
    }
}