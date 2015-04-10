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
package org.apache.cocoon.controller;

import java.io.OutputStream;
import java.util.Map;

import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.component.AbstractPipelineComponent;
import org.apache.cocoon.pipeline.component.CachingPipelineComponent;
import org.apache.cocoon.pipeline.component.Finisher;
import org.apache.cocoon.pipeline.component.Starter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringControllerComponent extends AbstractPipelineComponent implements Starter, Finisher,
        ApplicationContextAware, CachingPipelineComponent {

    private ApplicationContext applicationContext;
    private String beanName;
    private Map<String, ? extends Object> configuration;
    private Controller controller;
    private OutputStream outputStream;
    private Map<String, Object> parameters;
    private String selector;

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.component.CachingPipelineComponent#constructCacheKey()
     */
    public CacheKey constructCacheKey() {
        return this.controller.getCacheKey();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.component.Starter#execute()
     */
    public void execute() {
        this.controller.execute(this.outputStream);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.component.Finisher#getContentType()
     */
    public String getContentType() {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.component.PipelineComponent#setConfiguration(java.util.Map)
     */
    @Override
    public void setConfiguration(Map<String, ? extends Object> configuration) {
        this.configuration = configuration;
        this.beanName = (String) configuration.get("controller");
        this.selector = (String) configuration.get("select");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.component.Finisher#setOutputStream(java.io.OutputStream)
     */
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.component.PipelineComponent#setup(java.util.Map)
     */
    @Override
    public void setup(Map<String, Object> parameters) {
        this.parameters = parameters;

        this.controller = (Controller) this.applicationContext.getBean(
                Controller.class.getName() + "/" + this.beanName, Controller.class);
        this.controller.setup(this.selector, this.parameters, this.configuration);
    }
}
