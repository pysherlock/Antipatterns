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
package org.apache.cocoon.sax.builder;

import org.apache.cocoon.pipeline.AsyncCachePipeline;
import org.apache.cocoon.pipeline.CachingPipeline;
import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.sax.SAXPipelineComponent;

/**
 * 
 *
 * @version $Id: SAXPipelineBuilder.java 1087865 2011-04-01 19:31:04Z simonetripodi $
 */
public final class SAXPipelineBuilder {

    /**
     * {@inheritDoc}
     */
    public static LinkedSAXPipelineStarterBuilder newAsyncCachePipeline() {
        return newPipeline(new AsyncCachePipeline<SAXPipelineComponent>());
    }

    /**
     * {@inheritDoc}
     */
    public static LinkedSAXPipelineStarterBuilder newCachingPipeline() {
        return newPipeline(new CachingPipeline<SAXPipelineComponent>());
    }

    /**
     * {@inheritDoc}
     */
    public static LinkedSAXPipelineStarterBuilder newNonCachingPipeline() {
        return newPipeline(new NonCachingPipeline<SAXPipelineComponent>());
    }

    /**
     * {@inheritDoc}
     */
    private static LinkedSAXPipelineStarterBuilder newPipeline(final Pipeline<SAXPipelineComponent> pipeline) {
        return new LinkedSAXPipelineStarterBuilderImpl(pipeline);
    }

    /**
     * Hidden constructor, this class can't be instantiated.
     */
    private SAXPipelineBuilder() {
        // do nothing
    }

}
