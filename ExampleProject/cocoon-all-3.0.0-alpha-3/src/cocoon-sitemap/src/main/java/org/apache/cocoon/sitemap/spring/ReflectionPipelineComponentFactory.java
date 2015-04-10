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
package org.apache.cocoon.sitemap.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.cocoon.pipeline.component.PipelineComponent;

public class ReflectionPipelineComponentFactory implements PipelineComponentFactory {

    private Map<String, Class<? extends PipelineComponent>> types = new HashMap<String, Class<? extends PipelineComponent>>();

    /**
     * @param types
     */
    @SuppressWarnings("unchecked")
    public void setTypes(Map<String, String> types) {
        for (Entry<String, String> entry : types.entrySet()) {
            try {
                Class<? extends PipelineComponent> nodeClass = (Class<? extends PipelineComponent>) Class.forName(entry
                        .getValue());
                this.types.put(entry.getKey(), nodeClass);
            } catch (ClassCastException ccex) {
                throw new IllegalArgumentException("Could not register class " + entry.getValue()
                        + " as component type " + entry.getKey(), ccex);
            } catch (ClassNotFoundException cnfex) {
                throw new IllegalArgumentException("Could not register class " + entry.getValue()
                        + " as component type " + entry.getKey(), cnfex);
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.sitemap.spring.PipelineComponentFactory#createComponent(java.lang.String)
     */
    public PipelineComponent createComponent(String type) {
        Class<? extends PipelineComponent> componentClass = this.types.get(type);

        if (componentClass == null) {
            throw new IllegalArgumentException("Pipeline component type '" + type + "' is not supported.");
        }

        try {
            return componentClass.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("A component of type '" + type + "' could not be created.", e);
        }
    }
}
