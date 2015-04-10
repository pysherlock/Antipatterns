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

import java.io.ByteArrayOutputStream;

import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.caching.CacheRefreshJob;
import org.apache.cocoon.pipeline.caching.CacheRefreshManager;
import org.apache.cocoon.pipeline.caching.CacheValue;
import org.apache.cocoon.pipeline.caching.CompleteCacheValue;
import org.apache.cocoon.pipeline.component.PipelineComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * This {@link Pipeline} basically works like the {@link CachingPipeline}. The only difference is that when the cached
 * result isn't valid anymore, the refresh is done in a separate thread. This means that the re-production of the result
 * doesn't block the initial request. The disadvantage of this approach is that until the result is being reproduced, an
 * out-dated result is returned. If this is out of question for a use case, the {@link CachingPipeline} has to be used.
 * </p>
 */
public class AsyncCachePipeline<T extends PipelineComponent> extends CachingPipeline<T> implements CacheRefreshJob {

    private final Log logger = LogFactory.getLog(this.getClass());

    /**
     * The component that does the refresh in a separate thread.
     */
    private CacheRefreshManager cacheRefreshManager;

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.CachingPipeline#execute()
     */
    @Override
    public void execute() throws Exception {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Used cache: " + this.cache);
        }

        // construct the current cache key
        this.cacheKey = this.constructCacheKey();

        // check for a cached value first
        CacheValue cachedValue = this.getCachedValue(this.cacheKey);
        if (cachedValue != null) {
            // cached value found -> write it
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Write cache value to output stream: " + cachedValue);
            }
            cachedValue.writeTo(this.cachingOutputStream.getOutputStream());

            if (!this.isCacheKeyValid(cachedValue)) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Cached value is not up to date. Delegating to " + this.cacheRefreshManager);
                }
                // the cached value is not valid -> refresh the value
                this.cacheRefreshManager.refreshCacheValue(this.cacheKey, this);
            }
            // stop here
            return;
        }

        // no cached value (not even an invalid one) was present -> execute the pipeline
        this.invokeStarter();
        // cache the result
        CompleteCacheValue cacheValue = new CompleteCacheValue(this.cachingOutputStream.getContent(), this.cacheKey);
        this.setCachedValue(this.cacheKey, cacheValue);
    }

    public CacheRefreshManager getCacheRefreshManager() {
        return this.cacheRefreshManager;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.caching.CacheRefreshJob#refresh(org.apache.cocoon.pipeline.caching.CacheKey)
     */
    public void refresh(CacheKey cacheKey) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.getFinisher().setOutputStream(baos);

        // execute the pipeline
        this.invokeStarter();

        this.setCachedValue(cacheKey, new CompleteCacheValue(baos.toByteArray(), cacheKey));
    }

    public void setCacheRefreshManager(CacheRefreshManager cacheRefreshManager) {
        this.cacheRefreshManager = cacheRefreshManager;
    }
}
