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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.cocoon.pipeline.util.MurmurHashCodeBuilder;
import org.apache.cocoon.pipeline.util.StringRepresentation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CompoundCacheKey extends AbstractCacheKey {

    private static final long serialVersionUID = 1L;

    private final Log logger = LogFactory.getLog(this.getClass());

    private final List<CacheKey> cacheKeys = new LinkedList<CacheKey>();

    public void addCacheKey(CacheKey cacheKey) {
        this.cacheKeys.add(cacheKey);
    }

    @Override
    public boolean equals(Object obj) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Comparing two cache keys: ");
            this.logger.debug("  this=" + this);
            this.logger.debug("  other=" + obj);
        }

        if (!(obj instanceof CompoundCacheKey)) {
            return false;
        }

        CompoundCacheKey other = (CompoundCacheKey) obj;
        if (this.cacheKeys.size() != other.cacheKeys.size()) {
            return false;
        }

        Iterator<CacheKey> myIterator = this.cacheKeys.iterator();
        Iterator<CacheKey> otherIterator = other.cacheKeys.iterator();

        while (myIterator.hasNext()) {
            CacheKey myCacheKey = myIterator.next();
            CacheKey otherCacheKey = otherIterator.next();

            if (myCacheKey == null || !myCacheKey.equals(otherCacheKey)) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Cache keys are not equal: ");
                    this.logger.debug("  myCacheKey=" + myCacheKey);
                    this.logger.debug("  otherCacheKey=" + otherCacheKey);
                }
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        MurmurHashCodeBuilder murmurHashCodeBuilder = new MurmurHashCodeBuilder();

        for (CacheKey cacheKey : this.cacheKeys) {
            murmurHashCodeBuilder.append(cacheKey.hashCode());
        }

        return murmurHashCodeBuilder.toHashCode();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.caching.CacheKey#isValid(org.apache.cocoon.pipeline.caching.CacheKey)
     */
    public boolean isValid(CacheKey cacheKey) {
        if (!this.equals(cacheKey)) {
            return false;
        }

        CompoundCacheKey other = (CompoundCacheKey) cacheKey;
        Iterator<CacheKey> myIterator = this.cacheKeys.iterator();
        Iterator<CacheKey> otherIterator = other.cacheKeys.iterator();

        while (myIterator.hasNext()) {
            CacheKey myCacheKey = myIterator.next();
            CacheKey otherCacheKey = otherIterator.next();

            if (!myCacheKey.isValid(otherCacheKey)) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Cache key is not valid: ");
                    this.logger.debug("  myCacheKey=" + myCacheKey);
                    this.logger.debug("  otherCacheKey=" + otherCacheKey);
                }

                return false;
            }
        }

        return true;
    }

    public long getLastModified() {
        long lastModified = 0;
        for (CacheKey eachKey : this.cacheKeys) {
            long eachLastModified = eachKey.getLastModified();
            if (eachLastModified == -1) {
                return -1;
            }
            if (eachLastModified > lastModified) {
                lastModified = eachLastModified;
                continue;
            }
        }
        return lastModified;
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "key=" + this.cacheKeys);
    }
}
