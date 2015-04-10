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
package org.apache.cocoon.sax.util;

import java.io.Serializable;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public final class InMemoryLRUResourceCache<V> implements Serializable {

    /**
     * This class serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The fixed cache size.
     */
    private static final int CACHE_SIZE = 255;

    /**
     * The fixed cache load facor.
     */
    private static final float LOAD_FACTOR = 0.75f;

    /**
     * The fixed cache capacity.
     */
    private static final int CACHE_CAPACITY = (int) Math.ceil(CACHE_SIZE / LOAD_FACTOR) + 1;

    /**
     * The map that implements the LRU cache.
     */
    private final Map<URL, V> data = new LinkedHashMap<URL, V>(CACHE_CAPACITY, LOAD_FACTOR) {
        /**
         * This class serialVersionUID.
         */
        private static final long serialVersionUID = 1L;

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean removeEldestEntry(Map.Entry<URL, V> eldest) {
            return size() > CACHE_SIZE;
        }
    };

    /**
     * Returns true if this cache contains a mapping for the specified key.
     *
     * @param key key whose presence in this map is to be tested.
     * @return true if this map contains a mapping for the specified key, false
     *         otherwise.
     */
    public boolean containsKey(URL key) {
        checkKey(key);
        return this.data.containsKey(key);
    }

    /**
     * Returns the value to which the specified key is cached, or null if this
     * cache contains no mapping for the key.
     *
     * Key parameter must not be null.
     *
     * @param key the key has to be checked it is present, it must not be null.
     * @return the value to which the specified key is cached, null if this
     *         cache contains no mapping for the key.
     */
    public V get(URL key) {
        checkKey(key);
        return this.data.get(key);
    }

    /**
     * Associates the specified value with the specified key in this cache.
     *
     * Key parameter must not be null.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     */
    public void put(URL key, V value) {
        checkKey(key);
        this.data.put(key, value);
    }

    /**
     * Verify that a key is not null.
     *
     * @param key the key object.
     */
    private static void checkKey(URL key) {
        if (key == null) {
            throw new IllegalArgumentException("null keys not supported");
        }
    }

}
