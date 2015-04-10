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

import java.util.Set;

/**
 * An abstract implementation of the {@link Cache} interface.<br>
 * <br>
 * It handles the validity check for retrieving {@link CacheValue}s but relies on child classes for actually accessing the underlying
 * stores.
 */
public abstract class AbstractCache implements Cache {

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.caching.Cache#get(org.apache.cocoon.pipeline.caching.CacheKey)
     */
    public final CacheValue get(CacheKey cacheKey) {
        return this.get(cacheKey, false);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.caching.Cache#get(org.apache.cocoon.pipeline.caching.CacheKey, boolean)
     */
    public final CacheValue get(CacheKey cacheKey, boolean includeInvalid) {
        CacheValue cacheValue = this.retrieve(cacheKey);

        if (includeInvalid || this.isValid(cacheKey, cacheValue)) {
            return cacheValue;
        }

        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.caching.Cache#put(org.apache.cocoon.pipeline.caching.CacheKey,
     *      org.apache.cocoon.pipeline.caching.CacheValue)
     */
    public final void put(CacheKey cacheKey, CacheValue cacheValue) {
        this.store(cacheKey, cacheValue);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.caching.Cache#clear()
     */
    public void clear() {
        this.doClear();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.caching.Cache#remove(org.apache.cocoon.pipeline.caching.CacheKey)
     */
    public boolean remove(CacheKey cacheKey) {
        return this.doRemove(cacheKey);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.caching.Cache#keySet()
     */
    public Set<CacheKey> keySet() {
        return this.retrieveKeySet();
    }

    /**
     * Determines if the given <code>cacheValue</code> is valid according to the given <code>cacheKey</code>.<br>
     * <br>
     * This method returns <code>true</code> if and only if the given <code>cacheValue</code> is not <code>null</code> and calling
     * {@link CacheValue#isValid(CacheKey)} with the given <code>cacheKey</code> returns <code>true</code>.
     *
     * @param cacheKey The {@link CacheKey} to be used for checking the <code>cacheValue</code>'s validity.
     * @param cacheValue The {@link CacheValue} to check for validity.
     * @return <code>true</code> if the given <code>cacheValue</code> is not <code>null</code> and valid for the given
     *         <code>cacheKey</code>.
     */
    protected boolean isValid(CacheKey cacheKey, CacheValue cacheValue) {
        if (cacheValue == null) {
            return false;
        }

        return cacheValue.isValid(cacheKey);
    }

    /**
     * Actually retrieves the {@link CacheValue} from the underlying storage.<br>
     * This method must return the previously stored value - even if it is not valid anymore.
     * 
     * @param cacheKey The {@link CacheKey} to be used for retrieval.
     * @return The previously stored {@link CacheValue} or <code>null</code> if no {@link CacheValue} is stored at the given
     *         <code>cacheKey</code>.
     */
    protected abstract CacheValue retrieve(CacheKey cacheKey);

    /**
     * Actually stores the given <code>cacheValue</code> at the given <code>cacheKey</code> in the underlying storage.<br>
     * <br>
     * This method is to replace any previously stored {@link CacheValue} (if any).
     *
     * @param cacheKey The {@link CacheKey} to be used for storing.
     * @param cacheValue The {@link CacheValue} to be stored.
     */
    protected abstract void store(CacheKey cacheKey, CacheValue cacheValue);

    /**
     * Actually clears the underlying storage.
     */
    protected abstract void doClear();

    /**
     * Actually removes cached data from underlying storage.
     *
     * @param cacheKey The {@link CacheKey} to be removed.
     */
    protected abstract boolean doRemove(CacheKey cacheKey);

    /**
     * Actually retrieves the {@link Set} for {@link CacheKey} from underlying storage.
     *
     * @return The {@link Set} of {@link CacheKey} of containded data.
     */
    protected abstract Set<CacheKey> retrieveKeySet();

}
