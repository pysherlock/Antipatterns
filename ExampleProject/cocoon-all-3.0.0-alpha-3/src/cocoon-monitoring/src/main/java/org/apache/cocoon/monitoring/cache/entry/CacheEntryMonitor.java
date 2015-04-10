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
package org.apache.cocoon.monitoring.cache.entry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.cocoon.monitoring.util.UnitSizeFormatter;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.caching.CacheValue;
import org.apache.cocoon.pipeline.caching.CompleteCacheValue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource
public class CacheEntryMonitor {

    private final CacheKey cacheKey;
    private final CacheValue cacheValue;
    private final Log logger = LogFactory.getLog(this.getClass());

    public CacheEntryMonitor(CacheKey cacheKey, CacheValue cacheValue) {
        this.cacheKey = cacheKey;
        this.cacheValue = cacheValue;
    }

    @ManagedAttribute(description = "Retuns tihs cache value key.")
    public final String getCacheKey() {
        return this.cacheKey.toString();
    }

    /**
     *
     * @return size of this cache entry
     */
    @ManagedAttribute(description = "Returns content of this cache entry.")
    public final String getCacheValue() {
        if (this.cacheValue instanceof CompleteCacheValue) {
            ByteArrayOutputStream test = new ByteArrayOutputStream();
            try {
                this.cacheValue.writeTo(test);
            } catch (IOException e) {
                this.logger.fatal(e.getMessage(), e);
                return "IOException occurs, please check logs.";
            }

            return new String(test.toByteArray());
        } else {
            return String.valueOf(this.cacheValue.getValue());
        }
    }

    /**
     * Sets value of this cache entry.
     *
     * @param value new value of cache
     */
    @ManagedOperation(description = "Sets value of this cache entry.")
    @ManagedOperationParameters( { @ManagedOperationParameter(name = "value", description = "New value of cache.") })
    public final boolean setCacheValue(String value) {
        this.cacheValue.setValue(value);
        return true;
    }

    /**
     *
     * @return size of this cache entry
     */
    @ManagedAttribute(description = "Returns size of this cache entry.")
    public final String getSize() {
        return UnitSizeFormatter.getHumanReadableSize(this.cacheValue.size());
    }
}
