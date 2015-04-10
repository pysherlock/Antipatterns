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
package org.apache.cocoon.monitoring.cache;

import java.util.Set;

import org.apache.cocoon.monitoring.util.UnitSizeFormatter;
import org.apache.cocoon.pipeline.caching.Cache;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.caching.CacheValue;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * JMX MBean to monitor caches.
 */
@ManagedResource
public class CacheMonitor {

    private final Cache cache;

    public CacheMonitor(Cache cache) {
        this.cache = cache;
    }

    /**
     * Removes all cached data.
     */
    @ManagedOperation(description = "Removes all cached data.")
    public final void clear() {
        this.cache.clear();
    }

    /**
     * List available cache keys
     *
     * @return
     */
    @ManagedOperation(description = "Removes all cached data.")
    public final String[] listKeys() { // TODO make cache names more readable and usable for user
        Set<CacheKey> keySet = this.cache.keySet();
        String[] result = new String[keySet.size()];

        int i = 0;
        for (CacheKey cacheKey : keySet) {
            result[i++] = cacheKey.toString();
        }

        return result;
    }

    /**
     * Remove specific key for cache
     *
     * @param cacheKeyName name of CacheKey with should be removed
     * @return
     */
    @ManagedOperation(description = "Remove specific key for cache")
    @ManagedOperationParameter(name = "cacheKeyName", description = "Name of CacheKey with should be removed")
    public final boolean removeKey(String cacheKeyName) {
        Set<CacheKey> keySet = this.cache.keySet();

        for (CacheKey cacheKey : keySet) {
            if (cacheKey.equals(cacheKeyName)) {
                this.cache.remove(cacheKey);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns size of this cache
     *
     * @return
     */
    @ManagedOperation(description = "Returns size of this cache")
    public final String size() {
        double result = 0;
        for (CacheKey key : this.cache.keySet()) {
            CacheValue cacheValue = this.cache.get(key);
            if (cacheValue != null) { // prevent from NullPointerException
                double size = cacheValue.size();
                if (size != -1) {
                    result += size;
                }
            }
        }

        return UnitSizeFormatter.getHumanReadableSize(result);
    }
}
