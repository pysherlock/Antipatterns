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
package org.apache.cocoon.optional.pipeline.components.sax.jaxb;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * @version $Id: InMemoryLRUMarshallerCache.java 1087886 2011-04-01 20:27:21Z simonetripodi $
 */
final class InMemoryLRUMarshallerCache implements Serializable {

    /**
     * This class serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The fixed cache size.
     */
    private static final int CACHE_SIZE = 255;

    /**
     * The fixed cache load factor.
     */
    private static final float LOAD_FACTOR = 0.75f;

    /**
     * The fixed cache capacity.
     */
    private static final int CACHE_CAPACITY = (int) Math.ceil(CACHE_SIZE / LOAD_FACTOR) + 1;

    private static final InMemoryLRUMarshallerCache INSTANCE = new InMemoryLRUMarshallerCache();

    public static InMemoryLRUMarshallerCache getInstance() {
        return INSTANCE;
    }

    /**
     * This class can't be instantiated.
     */
    private InMemoryLRUMarshallerCache() {
        // do nothing
    }

    /**
     * The map that implements the LRU cache.
     */
    private final Map<Class<?>, JAXBContext> data = new LinkedHashMap<Class<?>, JAXBContext>(CACHE_CAPACITY, LOAD_FACTOR) {
        /**
         * This class serialVersionUID.
         */
        private static final long serialVersionUID = 1L;

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean removeEldestEntry(Map.Entry<Class<?>, JAXBContext> eldest) {
            return size() > CACHE_SIZE;
        }
    };

    /**
     * Creates the JAXBContext and stores it if the class parameter has never
     * previously analyzed, otherwise gets it from a local cache; finally it uses
     * it to create the Marshaller.
     *
     * @param clazz the class for which the Marshaller has to be created.
     * @return the JAXB Marshaller.
     * @throws JAXBException if any error occurs.
     */
    public synchronized Marshaller getMarshaller(Class<?> clazz) throws JAXBException {
        if (clazz == null) {
            throw new IllegalArgumentException("Parameter 'clazz' must not be null");
        }

        JAXBContext jaxbContext = null;

        if (this.data.containsKey(clazz)) {
            jaxbContext = this.data.get(clazz);
        } else {
            jaxbContext = JAXBContext.newInstance(clazz);
            this.data.put(clazz, jaxbContext);
        }

        return jaxbContext.createMarshaller();
    }

}
