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

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.management.ManagementService;

public class EhCacheProfilingDataHolder implements ProfilingDataHolder {

    private Cache profilingCache;
    private CacheManager cacheManager;
    private String cacheName;

    /**
     * Get the cache from ehcache cache manager and register MBeans.
     */
    public void setupCache() {
        this.profilingCache = this.cacheManager.getCache(this.cacheName);
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        boolean registerCacheManager = false;
        boolean registerCaches = true;
        boolean registerCacheConfigurations = true;
        boolean registerCacheStatistics = true;
        ManagementService.registerMBeans(this.cacheManager, mBeanServer, registerCacheManager, registerCaches,
                registerCacheConfigurations, registerCacheStatistics);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.profiling.data.ProfilingDataHolder#get(java.lang.String)
     */
    public ProfilingData get(String id) {
        Element e = this.profilingCache.get(id);
        return e == null ? null : (ProfilingData) e.getObjectValue();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.profiling.data.ProfilingDataHolder#store(java.lang.String,
     *      org.apache.cocoon.profiling.data.ProfilingData)
     */
    public void store(String id, ProfilingData data) {
        this.profilingCache.put(new Element(id, data));
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }
}
