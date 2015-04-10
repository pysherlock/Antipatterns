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
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.stringtemplate.AttributeRenderer;
import org.antlr.stringtemplate.StringTemplate;
import org.apache.cocoon.pipeline.PipelineException;
import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.caching.CompoundCacheKey;
import org.apache.cocoon.pipeline.caching.ParameterCacheKey;
import org.apache.cocoon.pipeline.caching.TimestampCacheKey;
import org.apache.cocoon.pipeline.component.CachingPipelineComponent;
import org.apache.cocoon.pipeline.component.Starter;
import org.apache.cocoon.sax.AbstractSAXProducer;
import org.apache.cocoon.sax.util.XMLUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;

public class StringTemplateGenerator extends AbstractSAXProducer implements Starter, CachingPipelineComponent {

    private final Log logger = LogFactory.getLog(this.getClass());

    private URL source;
    protected Map<String, Object> parameters = new HashMap<String, Object>();

    public StringTemplateGenerator() {
        super();
    }

    public StringTemplateGenerator(URL source) {
        super();
        this.setSource(source);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.component.CachingPipelineComponent#constructCacheKey()
     */
    public CacheKey constructCacheKey() {
        CompoundCacheKey cacheKey = new CompoundCacheKey();

        try {
            cacheKey.addCacheKey(new TimestampCacheKey(this.source, this.source.openConnection().getLastModified()));
            cacheKey.addCacheKey(new ParameterCacheKey("contextParameters", this.parameters));
        } catch (IOException e) {
            throw new SetupException(e);
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
            throw new IllegalArgumentException("StringTemplateGenerator has no source.");
        }

        try {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Using template: " + this.source.toURI().toASCIIString());
            }

            InputStream inputStream = this.source.openStream();
            try {
                StringTemplate stringTemplate = new StringTemplate(IOUtils.toString(inputStream, "UTF-8"));
                stringTemplate.registerRenderer(String.class, new AttributeRenderer() {
                    public String toString(Object object) {
                        return StringEscapeUtils.escapeXml(object.toString());
                    }

                    public String toString(Object o, String string) {
                        return this.toString(o);
                    }
                });

                this.addTemplateAttributes(stringTemplate);

                XMLUtils.createXMLReader(this.getSAXConsumer()).parse(
                        new InputSource(new StringReader(stringTemplate.toString())));
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        } catch (PipelineException e) {
            throw e;
        } catch (Exception e) {
            throw new ProcessingException("Can't parse url connection " + this.source, e);
        }
    }

    public void setSource(URL source) {
        this.source = source;
    }

    @Override
    public void setup(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * Add attributes to the StringTemplate. Be careful to keep the constructCacheKey() method
     * align.
     *
     * @param stringTemplate The template where the attributes are added to.
     */
    protected void addTemplateAttributes(StringTemplate stringTemplate) {
        if(this.parameters == null) {
            this.logger.warn("There are not any parameters passed to the template.");
            return;
        }

        for (Entry<String, Object> eachEntry : this.parameters.entrySet()) {
            stringTemplate.setAttribute(eachEntry.getKey().replace(".", "_"), eachEntry.getValue());

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Passing pipeline parameter as attribute: key=" + eachEntry.getKey() + ", value="
                        + eachEntry.getValue());
            }
        }
    }
}
