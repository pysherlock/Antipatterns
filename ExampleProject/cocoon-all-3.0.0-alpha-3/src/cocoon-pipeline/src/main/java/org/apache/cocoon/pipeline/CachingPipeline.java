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
package org.apache.cocoon.pipeline;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

import org.apache.cocoon.pipeline.caching.Cache;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.caching.CacheValue;
import org.apache.cocoon.pipeline.caching.CachingOutputStream;
import org.apache.cocoon.pipeline.caching.CompleteCacheValue;
import org.apache.cocoon.pipeline.caching.CompoundCacheKey;
import org.apache.cocoon.pipeline.caching.ExpiresCacheKey;
import org.apache.cocoon.pipeline.caching.InvalidCacheKey;
import org.apache.cocoon.pipeline.component.CachingPipelineComponent;
import org.apache.cocoon.pipeline.component.PipelineComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * A {@link Pipeline} implementation that returns a cached result if, and only
 * if all its components support caching. A {@link PipelineComponent} is
 * cacheable if it implements the interface {@link CachingPipelineComponent}.
 * </p>
 */
public class CachingPipeline<T extends PipelineComponent> extends AbstractPipeline<T> {

    protected Cache cache;

    protected CacheKey cacheKey;

    protected CachingOutputStream cachingOutputStream;

    /** Expires time in seconds */
    private String expires;

    /**
     * Expires pipelines that have non-cacheable pipeline components require an
     * explicit cache key
     */
    private Serializable expiresCacheKey;

    private String jmxGroupName;

    private final Log logger = LogFactory.getLog(this.getClass());

    public CacheKey constructCacheKey() {
        CompoundCacheKey result = new CompoundCacheKey();
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Creating " + result + ": ");
        }

        for (PipelineComponent pipelineComponent : this.getComponents()) {
            if (pipelineComponent instanceof CachingPipelineComponent) {
                CachingPipelineComponent cachablePipelineComponent = (CachingPipelineComponent) pipelineComponent;

                CacheKey cacheKey = cachablePipelineComponent.constructCacheKey();
                if (cacheKey != null) {
                    result.addCacheKey(cacheKey);
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("  ~ adding " + cacheKey + " for component " + pipelineComponent);
                    }

                    continue;
                }
            }

            // support expires caching
            if (this.expires != null) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("  ~ adding " + ExpiresCacheKey.class.getSimpleName() + " for component: "
                            + pipelineComponent + " (the component doesn't support caching "
                            + "but expires caching is activated)");
                }

                return new ExpiresCacheKey(new InvalidCacheKey(this.expiresCacheKey), this.expires);
            }

            // component does not support caching
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("  ~ no caching: " + pipelineComponent);
                this.logger.debug("Aborting cache key construction");
            }

            return null;
        }

        // support expires caching
        if (this.expires != null) {
            CacheKey expiresCacheKey = new ExpiresCacheKey(result, this.expires);

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Creating  " + expiresCacheKey + " for pipeline " + this);
            }

            return expiresCacheKey;
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Creating  " + result + " for pipeline " + this);
        }
        return result;
    }

    @Override
    public void execute() throws Exception {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Used cache: " + this.cache);
        }

        // checked for a cached value first
        CacheValue cachedValue = this.getCachedValue(this.cacheKey);
        if (this.isCacheKeyValid(cachedValue)) {
            // cached value found
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Write cache value to output stream: " + cachedValue);
            }

            cachedValue.writeTo(this.cachingOutputStream.getOutputStream());
            return;
        }

        // execute the pipeline
        this.invokeStarter();

        // cache the result
        CompleteCacheValue cacheValue = new CompleteCacheValue(this.cachingOutputStream.getContent(), this.cacheKey);
        this.setCachedValue(this.cacheKey, cacheValue);
    }

    public CacheKey getCacheKey() {
        return this.cacheKey;
    }

    public String getExpires() {
        return this.expires;
    }

    @Override
    public long getLastModified() {
        if (this.cacheKey == null) {
            return -1;
        }

        return this.cacheKey.getLastModified();
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.AbstractPipeline#setConfiguration(java.util.Map)
     */
    @Override
    public void setConfiguration(Map<String, ? extends Object> parameters) {
        this.expires = (String) parameters.get("expires");
        this.expiresCacheKey = (Serializable) parameters.get("expires-cache-key");
        this.jmxGroupName = (String) parameters.get("jmx-group-name");

        super.setConfiguration(parameters);
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public void setExpiresCacheKey(Serializable expiresCacheKey) {
        this.expiresCacheKey = expiresCacheKey;
    }

    @Override
    public void setup(OutputStream outputStream, Map<String, Object> parameters) {
        // create a caching output stream to intercept the result
        this.cachingOutputStream = new CachingOutputStream(outputStream);

        super.setup(this.cachingOutputStream, parameters);

        this.cacheKey = this.constructCacheKey();
    }

    protected CacheValue getCachedValue(CacheKey cacheKey) {
        if (cacheKey == null) {
            return null;
        }

        if (this.cache == null) {
            this.logger.warn("Caching pipeline has no cache configured. Falling back to non-caching behavior.");
            return null;
        }

        CacheValue cacheValue = this.cache.get(cacheKey, true);
        if (this.logger.isDebugEnabled()) {
            if (cacheValue != null) {
                this.logger.debug("Retrieved content from cache: " + cacheValue);
            } else {
                this.logger.debug("No cache value available for " + cacheKey);
            }
        }
        return cacheValue;
    }

    protected boolean isCacheKeyValid(CacheValue cachedValue) {
        return cachedValue != null && cachedValue.isValid(this.cacheKey);
    }

    protected void setCachedValue(CacheKey cacheKey, CacheValue cacheValue) {
        if (cacheKey == null) {
            return;
        }

        if (this.cache == null) {
            this.logger.warn("Caching pipeline has no cache configured. Falling back to non-caching behavior.");
            return;
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Putting result into pipeline cache: " + cacheValue + ")");
        }
        cacheKey.setJmxGroupName(this.jmxGroupName);
        this.cache.put(cacheKey, cacheValue);
    }
}
