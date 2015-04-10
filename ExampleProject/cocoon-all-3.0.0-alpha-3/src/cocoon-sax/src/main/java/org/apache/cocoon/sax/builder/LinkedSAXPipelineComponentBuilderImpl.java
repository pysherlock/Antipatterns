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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;

import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.pipeline.builder.LinkedPipelineConfigurationBuilder;
import org.apache.cocoon.pipeline.builder.LinkedPipelineSetupBuilder;
import org.apache.cocoon.sax.SAXPipelineComponent;
import org.apache.cocoon.sax.component.CleaningTransformer;
import org.apache.cocoon.sax.component.IncludeTransformer;
import org.apache.cocoon.sax.component.LogAsXMLTransformer;
import org.apache.cocoon.sax.component.LogTransformer;
import org.apache.cocoon.sax.component.SchemaProcessorTransformer;
import org.apache.cocoon.sax.component.XIncludeTransformer;
import org.apache.cocoon.sax.component.XMLSerializer;
import org.apache.cocoon.sax.component.XSLTTransformer;

/**
 * 
 *
 * @version $Id: LinkedSAXPipelineComponentBuilderImpl.java 1044301 2010-12-10 11:34:38Z simonetripodi $
 */
public final class LinkedSAXPipelineComponentBuilderImpl implements LinkedSAXPipelineComponentBuilder {

    private final Pipeline<SAXPipelineComponent> pipeline;

    public LinkedSAXPipelineComponentBuilderImpl(final Pipeline<SAXPipelineComponent> pipeline) {
        this.pipeline = pipeline;
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder addCleaningTransformer() {
        return this.addComponent(new CleaningTransformer());
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder addIncludeTransformer() {
        return this.addComponent(new IncludeTransformer());
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder addLogAsXMLTransformer() {
        return this.addComponent(new LogAsXMLTransformer());
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder addLogAsXMLTransformer(File logFile) {
        if (logFile == null) {
            throw new IllegalArgumentException("Parameter 'logFile' must be not null");
        }
        return this.addComponent(new LogAsXMLTransformer(logFile));
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder addLogTransformer(File logFile) throws IOException {
        return this.addLogTransformer(logFile, false);
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder addLogTransformer(File logFile, boolean append) throws IOException {
        return this.addLogTransformer(logFile, append, "yyyy-MM-dd'T'hh:mm:ss");
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder addLogTransformer(File logFile, boolean append, String datePattern) throws IOException {
        if (datePattern == null) {
            throw new IllegalArgumentException("Parameter 'datePattern' must be not null");
        }
        return this.addLogTransformer(logFile, append, new SimpleDateFormat(datePattern));
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder addLogTransformer(File logFile, boolean append, SimpleDateFormat dateFormat) throws IOException {
        if (logFile == null) {
            throw new IllegalArgumentException("Parameter 'logFile' must be not null");
        }
        if (dateFormat == null) {
            throw new IllegalArgumentException("Parameter 'dateFormat' must be not null");
        }
        return this.addComponent(new LogTransformer(logFile, append, dateFormat));
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder addSchemaProcessorTransformer(URL source) {
        if (source == null) {
            throw new IllegalArgumentException("Parameter 'source' must be not null");
        }
        return this.addComponent(new SchemaProcessorTransformer(source));
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder addXIncludeTransformer() {
        return this.addXIncludeTransformer(null);
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder addXIncludeTransformer(URL baseUrl) {
        // baseUrl can be null, but only if documents don't have relative paths inside
        return this.addComponent(new XIncludeTransformer(baseUrl));
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder addXSLTTransformer(URL source) {
        return this.addXSLTTransformer(source, null);
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder addXSLTTransformer(URL source, Map<String, Object> attributes) {
        if (source == null) {
            throw new IllegalArgumentException("Parameter 'source' must be not null");
        }
        return this.addComponent(new XSLTTransformer(source, attributes));
    }

    /**
     * {@inheritDoc}
     */
    public <SPC extends SAXPipelineComponent> LinkedSAXPipelineComponentBuilder addComponent(SPC pipelineComponent) {
        if (pipelineComponent == null) {
            throw new IllegalArgumentException("Parameter 'pipelineComponent' must not be null");
        }
        this.pipeline.addComponent(pipelineComponent);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public LinkedPipelineConfigurationBuilder<SAXPipelineComponent> addSerializer() {
        this.pipeline.addComponent(new XMLSerializer());

        return new LinkedPipelineConfigurationBuilder<SAXPipelineComponent>() {

            @SuppressWarnings("unchecked")
            public LinkedPipelineSetupBuilder<SAXPipelineComponent> withEmptyConfiguration() {
                return this.setConfiguration(Collections.EMPTY_MAP);
            }

            public LinkedPipelineSetupBuilder<SAXPipelineComponent> setConfiguration(final Map<String, ? extends Object> parameters) {
                if (parameters == null) {
                    throw new IllegalArgumentException("Parameter 'parameters' must be not null");
                }
                pipeline.setConfiguration(parameters);

                return new LinkedPipelineSetupBuilder<SAXPipelineComponent>() {

                    public Pipeline<SAXPipelineComponent> setup(final OutputStream outputStream) {
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

}
