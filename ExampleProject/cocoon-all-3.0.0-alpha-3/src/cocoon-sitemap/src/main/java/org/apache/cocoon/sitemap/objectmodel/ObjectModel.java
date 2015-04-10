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
package org.apache.cocoon.sitemap.objectmodel;

import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.sitemap.SitemapParametersStack;

/**
 * Object model for Cocoon 3 that can is used by expression languages.
 */
public class ObjectModel {

    private static final String OM_KEY_COCOON = "cocoon";

    private SitemapParametersStack sitemapParameters = new SitemapParametersStack();

    private final Map<String, Object> parameters;

    public ObjectModel(Map<String, Object> parameters) {
        super();

        this.parameters = parameters;
        this.parameters.put(OM_KEY_COCOON, new HashMap<String, Object>());
    }

    public void put(String key, Object object) {
        if ("cocoon".equals(key)) {
            throw new IllegalArgumentException("The cocoon object can't be set.");
        }
        this.parameters.put(key, object);
    }

    public Object get(String key) {
        return this.parameters.get(key);
    }

    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getCocoonObject() {
        return (Map<String, Object>) this.parameters.get(OM_KEY_COCOON);
    }

    public SitemapParametersStack getSitemapParameters() {
        return this.sitemapParameters;
    }
}
