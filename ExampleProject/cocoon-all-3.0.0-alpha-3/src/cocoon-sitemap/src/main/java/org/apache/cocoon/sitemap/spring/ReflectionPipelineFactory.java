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

import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.pipeline.component.PipelineComponent;

public class ReflectionPipelineFactory implements PipelineFactory {

    private Map<String, Class<? extends Pipeline<PipelineComponent>>> types = new HashMap<String, Class<? extends Pipeline<PipelineComponent>>>();

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.sitemap.spring.PipelineFactory#createPipeline(java.lang.String)
     */
    public Pipeline<PipelineComponent> createPipeline(String type) {
        Class<? extends Pipeline<PipelineComponent>> pipelineClass = this.types.get(type);

        if (pipelineClass == null) {
            throw new IllegalArgumentException("Pipeline type '" + type + "' is not supported.");
        }

        try {
            Pipeline<PipelineComponent> pipeline = pipelineClass.newInstance();
            return pipeline;
        } catch (Exception e) {
            throw new IllegalArgumentException("A pipeline of type '" + type + "' could not be created.", e);
        }
    }

    /**
     * @param types
     */
    @SuppressWarnings("unchecked")
    public void setTypes(Map<String, String> types) {
        for (Entry<String, String> entry : types.entrySet()) {
            try {
                Class<? extends Pipeline<PipelineComponent>> nodeClass = (Class<? extends Pipeline<PipelineComponent>>) Class
                        .forName(entry.getValue());
                this.types.put(entry.getKey(), nodeClass);
            } catch (ClassCastException ccex) {
                throw new IllegalArgumentException("Could not register class " + entry.getValue() + " as type "
                        + entry.getKey(), ccex);
            } catch (ClassNotFoundException cnfex) {
                throw new IllegalArgumentException("Could not register class " + entry.getValue() + " as type "
                        + entry.getKey(), cnfex);
            }
        }
    }
}
