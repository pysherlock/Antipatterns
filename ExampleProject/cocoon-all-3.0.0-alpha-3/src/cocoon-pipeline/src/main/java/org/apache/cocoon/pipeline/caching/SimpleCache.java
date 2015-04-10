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

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.cocoon.pipeline.util.StringRepresentation;

/**
 * A very simple implementation of the {@link Cache} interface.<br>
 * <br>
 * It uses a {@link WeakHashMap} as internal data store.
 */
public class SimpleCache extends AbstractCache {

    private final Map<CacheKey, CacheValue> map = new WeakHashMap<CacheKey, CacheValue>();

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public synchronized String toString() {
        return StringRepresentation.buildString(this);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.caching.AbstractCache#retrieve(org.apache.cocoon.pipeline.caching.CacheKey)
     */
    @Override
    protected synchronized CacheValue retrieve(CacheKey cacheKey) {
        return this.map.get(cacheKey);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.caching.AbstractCache#store(org.apache.cocoon.pipeline.caching.CacheKey,
     *      org.apache.cocoon.pipeline.caching.CacheValue)
     */
    @Override
    protected synchronized void store(CacheKey cacheKey, CacheValue cacheValue) {
        this.map.put(cacheKey, cacheValue);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.caching.AbstractCach#doClear()
     */
    @Override
    protected synchronized void doClear() {
        this.map.clear();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.caching.AbstractCach#doRemove(org.apache.cocoon.pipeline.caching.CacheKey)
     */
    @Override
    protected synchronized boolean doRemove(CacheKey cacheKey) {
        Object pattern = this.map.get(cacheKey);
        Object removed = this.map.remove(cacheKey);
        return pattern == null && removed == null ? true : pattern == null && removed != null ? false : pattern
                .equals(removed);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.caching.AbstractCach#retrieve()
     */
    @Override
    protected synchronized Set<CacheKey> retrieveKeySet() {
        return this.map.keySet();
    }
}
