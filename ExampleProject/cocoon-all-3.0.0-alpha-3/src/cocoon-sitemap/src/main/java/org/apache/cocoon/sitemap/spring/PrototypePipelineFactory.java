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

import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.pipeline.component.PipelineComponent;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class PrototypePipelineFactory implements PipelineFactory, ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.sitemap.spring.PipelineFactory#createPipeline(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Pipeline<PipelineComponent> createPipeline(String type) {
        return (Pipeline<PipelineComponent>) this.applicationContext.getBean(type, Pipeline.class);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
