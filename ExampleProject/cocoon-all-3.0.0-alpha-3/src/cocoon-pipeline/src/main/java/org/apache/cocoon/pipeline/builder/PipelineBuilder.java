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
package org.apache.cocoon.pipeline.builder;

import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

import org.apache.cocoon.pipeline.AsyncCachePipeline;
import org.apache.cocoon.pipeline.CachingPipeline;
import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.pipeline.component.Finisher;
import org.apache.cocoon.pipeline.component.PipelineComponent;
import org.apache.cocoon.pipeline.component.Starter;

/**
 * 
 *
 * @param <PC> the {@link PipelineComponent} type.
 * @version $Id: PipelineBuilder.java 1087869 2011-04-01 19:53:25Z simonetripodi $
 */
public final class PipelineBuilder {

    /**
     * Hidden constructor, this class can't be instantiated.
     */
    private PipelineBuilder() {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public static <PC extends PipelineComponent> LinkedPipelineStarterBuilder<PC> newAsyncCachePipeline() {
        return newPipeline(new AsyncCachePipeline<PC>());
    }

    /**
     * {@inheritDoc}
     */
    public static <PC extends PipelineComponent> LinkedPipelineStarterBuilder<PC> newCachingPipeline() {
        return newPipeline(new CachingPipeline<PC>());
    }

    /**
     * {@inheritDoc}
     */
    public static <PC extends PipelineComponent> LinkedPipelineStarterBuilder<PC> newNonCachingPipeline() {
        return newPipeline(new NonCachingPipeline<PC>());
    }

    private static <PC extends PipelineComponent> LinkedPipelineStarterBuilder<PC> newPipeline(final Pipeline<PC> pipeline) {
        return new LinkedPipelineStarterBuilder<PC>() {

            public LinkedPipelineComponentBuilder<PC> setStarter(final PC starter) {
                if (starter == null) {
                    throw new IllegalArgumentException("Parameter 'starter' must be not null");
                }
                if (!(starter instanceof Starter)) {
                    throw new IllegalArgumentException("Parameter 'starter' must be org.apache.cocoon.pipeline.component.Starter instance");
                }
                pipeline.addComponent(starter);

                return new LinkedPipelineComponentBuilder<PC>() {

                    public LinkedPipelineComponentBuilder<PC> addComponent(final PC pipelineComponent) {
                        if (pipelineComponent == null) {
                            throw new IllegalArgumentException("Parameter 'pipelineComponent' must be not null");
                        }
                        pipeline.addComponent(pipelineComponent);
                        return this;
                    }

                    public LinkedPipelineConfigurationBuilder<PC> setFinisher(final PC finisher) {
                        if (finisher == null) {
                            throw new IllegalArgumentException("Parameter 'finisher' must be not null");
                        }
                        if (!(finisher instanceof Finisher)) {
                            throw new IllegalArgumentException("Parameter 'finisher' must be org.apache.cocoon.pipeline.component.Finisher instance");
                        }
                        pipeline.addComponent(finisher);

                        return new LinkedPipelineConfigurationBuilder<PC>() {

                            @SuppressWarnings("unchecked")
                            public LinkedPipelineSetupBuilder<PC> withEmptyConfiguration() {
                                return this.setConfiguration(Collections.EMPTY_MAP);
                            }

                            public LinkedPipelineSetupBuilder<PC> setConfiguration(final Map<String, ? extends Object> parameters) {
                                if (parameters == null) {
                                    throw new IllegalArgumentException("Parameter 'parameters' must be not null");
                                }
                                pipeline.setConfiguration(parameters);

                                return new LinkedPipelineSetupBuilder<PC>() {

                                    public Pipeline<PC> setup(final OutputStream outputStream) {
                                        if (outputStream == null) {
                                            throw new IllegalArgumentException("Parameter 'outputStream' must be not null");
                                        }
                                        pipeline.setup(outputStream);
                                        return pipeline;
                                    }

                                };
                            }

                        };
                    }

                };
            }

        };
    }

}
