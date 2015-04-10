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
package org.apache.cocoon.optional.pipeline.components.sax.jaxb;

import org.apache.cocoon.pipeline.caching.AbstractCacheKey;
import org.apache.cocoon.pipeline.caching.CacheKey;

/**
 * @version $Id: ObjectCacheKey.java 957445 2010-06-24 07:59:21Z simonetripodi $
 */
final class ObjectCacheKey extends AbstractCacheKey {

    private static final long serialVersionUID = 1L;

    private final Object obj;

    public ObjectCacheKey(Object obj) {
        this.obj = obj;
    }

    public Object getObj() {
        return this.obj;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ObjectCacheKey)) {
            return false;
        }

        ObjectCacheKey otherCacheKey = (ObjectCacheKey) other;
        return this.obj.equals(otherCacheKey.getObj());
    }

    @Override
    public int hashCode() {
        return this.obj.hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.caching.CacheKey#isValid(org.apache.cocoon.pipeline.caching.CacheKey)
     */
    public long getLastModified() {
        return -1;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.caching.CacheKey#isValid(org.apache.cocoon.pipeline.caching.CacheKey)
     */
    public boolean isValid(CacheKey other) {
        return this.equals(other);
    }
}
