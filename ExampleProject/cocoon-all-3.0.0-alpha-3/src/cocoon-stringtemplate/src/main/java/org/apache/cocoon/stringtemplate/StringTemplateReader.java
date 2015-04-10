/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.stringtemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.stringtemplate.AttributeRenderer;
import org.antlr.stringtemplate.StringTemplate;
import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.caching.CompoundCacheKey;
import org.apache.cocoon.pipeline.caching.ParameterCacheKey;
import org.apache.cocoon.pipeline.caching.TimestampCacheKey;
import org.apache.cocoon.pipeline.component.AbstractPipelineComponent;
import org.apache.cocoon.pipeline.component.CachingPipelineComponent;
import org.apache.cocoon.pipeline.component.Finisher;
import org.apache.cocoon.pipeline.component.Starter;
import org.apache.cocoon.servlet.controller.ControllerContextHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;

public class StringTemplateReader extends AbstractPipelineComponent implements CachingPipelineComponent, Starter,
        Finisher {

    private String mimeType;
    private OutputStream outputStream;
    private Map<String, Object> parameters = new HashMap<String, Object>();
    private URL source;

    public StringTemplateReader() {
        this(null);
    }

    public StringTemplateReader(URL source) {
        super();

        this.setSource(source);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.component.CachingPipelineComponent#constructCacheKey()
     */
    public CacheKey constructCacheKey() {
        if (this.source == null) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " has no source.");
        }

        CompoundCacheKey cacheKey = new CompoundCacheKey();

        try {
            cacheKey.addCacheKey(new TimestampCacheKey(this.source, this.source.openConnection().getLastModified()));
            cacheKey.addCacheKey(new ParameterCacheKey("contextParameters", this.parameters));
        } catch (IOException e) {
            throw new SetupException("Could not create cache key.", e);
        }

        return cacheKey;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.component.Starter#execute()
     */
    public void execute() {
        if (this.source == null) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " has no source.");
        }

        try {
            InputStream inputStream = this.source.openStream();

            try {
                StringTemplate stringTemplate = new StringTemplate(IOUtils.toString(inputStream, "UTF-8"));
                stringTemplate.registerRenderer(String.class, new AttributeRenderer() {

                    public String toString(Object object) {
                        return StringEscapeUtils.escapeXml(object.toString());
                    }

                    public String toString(Object o, String string) {
                        return toString(o);
                    }
                });

                Map<String, Object> controllerContext = ControllerContextHelper.getContext(this.parameters);
                for (Entry<String, Object> eachEntry : controllerContext.entrySet()) {
                    stringTemplate.setAttribute(eachEntry.getKey(), eachEntry.getValue());
                }

                OutputStreamWriter writer = new OutputStreamWriter(this.outputStream);
                writer.write(stringTemplate.toString());
                writer.flush();
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        } catch (IOException e) {
            throw new ProcessingException("Failed to produce result.", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.component.Finisher#getContentType()
     */
    public String getContentType() {
        return this.mimeType;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.component.PipelineComponent#setConfiguration(java.util.Map)
     */
    @Override
    public void setConfiguration(Map<String, ? extends Object> configuration) {
        this.setSource((URL) configuration.get("source"));
        this.setMimeType((String) configuration.get("mime-type"));
    }

    /**
     * Set the mime-type directly which is useful when this component is used directly.
     *
     * @param mimeType
     *            The mime-type that belongs to the content that is produced by this component.
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
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
     * Set the source {@link URL} directly when this component is used directly.
     *
     * @param source
     *            A {@link URL} that will be used by this component.
     */
    public void setSource(URL source) {
        this.source = source;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.component.AbstractPipelineComponent#setup(java.util.Map)
     */
    @Override
    public void setup(Map<String, Object> parameters) {
        super.setup(parameters);

        this.parameters = parameters;
    }
}
