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
package org.apache.cocoon.pipeline.caching;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cocoon.pipeline.util.StringRepresentation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CacheRefreshManagerImpl implements CacheRefreshManager {

    private final Log logger = LogFactory.getLog(this.getClass());

    private static final int threadPoolSize = 50;

    private final ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

    private final List<CacheKey> pendingCacheKeys = Collections.synchronizedList(new LinkedList<CacheKey>());

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.caching.CacheRefreshManager#refreshCacheValue(org.apache.cocoon.pipeline.caching.CacheKey,
     *      org.apache.cocoon.pipeline.caching.CacheRefreshJob)
     */
    public void refreshCacheValue(CacheKey cacheKey, CacheRefreshJob cacheRefreshJob) {
        if (this.pendingCacheKeys.contains(cacheKey)) {
            // the refresh of this cache key is already scheduled
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Refreshing of this cache key is already scheduled: " + cacheKey);
            }

            return;
        }

        this.pendingCacheKeys.add(cacheKey);
        this.executorService.execute(new RefreshWorker(cacheKey, cacheRefreshJob));
    }

    protected void executeCacheRefreshJob(CacheRefreshJob cacheRefreshJob, CacheKey cacheKey) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Execute cache refresh job for " + cacheRefreshJob);
        }

        cacheRefreshJob.refresh(cacheKey);
        this.pendingCacheKeys.remove(cacheKey);
    }

    private class RefreshWorker implements Runnable {

        private final CacheKey cacheKey;
        private final CacheRefreshJob cacheRefreshJob;

        public RefreshWorker(CacheKey cacheKey, CacheRefreshJob cacheRefreshJob) {
            this.cacheKey = cacheKey;
            this.cacheRefreshJob = cacheRefreshJob;
        }

        public void run() {
            CacheRefreshManagerImpl.this.executeCacheRefreshJob(this.cacheRefreshJob, this.cacheKey);
        }
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "executerService=" + this.executorService, "threadPoolSize="
                + threadPoolSize);
    }
}
