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
package org.apache.cocoon.sitemap;

import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.pipeline.component.PipelineComponent;
import org.apache.cocoon.sitemap.action.Action;
import org.apache.cocoon.sitemap.expression.LanguageInterpreter;
import org.apache.cocoon.sitemap.expression.LanguageInterpreterFactory;
import org.apache.cocoon.sitemap.spring.ActionFactory;
import org.apache.cocoon.sitemap.spring.PipelineComponentFactory;
import org.apache.cocoon.sitemap.spring.PipelineFactory;

public class SpringComponentProvider implements ComponentProvider {

    private ActionFactory actionFactory;

    private PipelineComponentFactory pipelineComponentFactory;

    private PipelineFactory pipelineFactory;

    private LanguageInterpreterFactory languageInterpreterFactory;

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.sitemap.ComponentProvider#createAction(java.lang.String)
     */
    public Action createAction(String type) {
        return this.actionFactory.createAction(type);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.sitemap.ComponentProvider#createComponent(java.lang.String)
     */
    public PipelineComponent createComponent(String type) {
        return this.pipelineComponentFactory.createComponent(type);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.sitemap.ComponentProvider#createPipeline(java.lang.String)
     */
    public Pipeline<PipelineComponent> createPipeline(String type) {
        return this.pipelineFactory.createPipeline(type);
    }

    public LanguageInterpreter getLanguageInterpreter(String language) {
        return this.languageInterpreterFactory.getLanguageInterpreter(language);
    }

    public void setActionFactory(ActionFactory actionFactory) {
        this.actionFactory = actionFactory;
    }

    public void setLanguageInterpreterFactory(LanguageInterpreterFactory languageInterpreterFactory) {
        this.languageInterpreterFactory = languageInterpreterFactory;
    }

    public void setPipelineComponentFactory(PipelineComponentFactory pipelineComponentFactory) {
        this.pipelineComponentFactory = pipelineComponentFactory;
    }

    public void setPipelineFactory(PipelineFactory pipelineFactory) {
        this.pipelineFactory = pipelineFactory;
    }
}
