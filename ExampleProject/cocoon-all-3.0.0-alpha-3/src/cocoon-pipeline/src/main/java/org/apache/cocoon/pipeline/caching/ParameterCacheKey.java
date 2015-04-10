/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.pipeline.caching;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.cocoon.pipeline.util.MurmurHashCodeBuilder;

/**
 * A {@link CacheKey} that contains a {@link Map} of parameters.
 */
public class ParameterCacheKey extends AbstractCacheKey {

    private static final long serialVersionUID = 1L;
    private final Map<String, String> parameters;

    public ParameterCacheKey() {
        this(new HashMap<String, String>());
    }

    public ParameterCacheKey(Map<String, String> parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("A map of parameters has to be passed.");
        }

        this.parameters = parameters;
    }

    public ParameterCacheKey(String name, Map<?, ?> value) {
        this();
        this.addParameter(name, value);
    }

    public ParameterCacheKey(String name, String value) {
        this();
        this.addParameter(name, value);
    }

    public void addParameter(String name, boolean value) {
        this.parameters.put(name, Boolean.toString(value));
    }

    public void addParameter(String name, int value) {
        this.parameters.put(name, Integer.toString(value));
    }

    public void addParameter(String name, Map<?, ?> value) {
        for (Entry<?, ?> object : value.entrySet()) {
            this.parameters.put(name + "_" + object.getKey().toString(), object.getValue().toString());
        }
    }

    public void addParameter(String name, String value) {
        this.parameters.put(name, value);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ParameterCacheKey)) {
            return false;
        }

        ParameterCacheKey other = (ParameterCacheKey) obj;
        return this.parameters != null && this.parameters.equals(other.parameters);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.caching.CacheKey#getLastModified()
     */
    public long getLastModified() {
        return -1;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        MurmurHashCodeBuilder hashCodeBuilder = new MurmurHashCodeBuilder();
        for (Entry<String, String> parameterEntry : this.parameters.entrySet()) {
            hashCodeBuilder.append(parameterEntry.getKey()).append(parameterEntry.getValue());
        }
        return hashCodeBuilder.toHashCode();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.caching.CacheKey#isValid(org.apache.cocoon.pipeline.caching.CacheKey)
     */
    public boolean isValid(CacheKey cacheKey) {
        return this.equals(cacheKey);
    }
}
