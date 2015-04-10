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
package org.apache.cocoon.optional.servlet.components.sax.serializers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.cocoon.optional.servlet.components.sax.serializers.util.ConfigurationUtils;
import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.caching.ParameterCacheKey;
import org.apache.cocoon.pipeline.component.CachingPipelineComponent;
import org.apache.cocoon.pipeline.component.Finisher;
import org.apache.cocoon.pipeline.util.StringRepresentation;
import org.apache.cocoon.sax.SAXConsumer;
import org.apache.cocoon.sax.SAXPipelineComponent;
import org.apache.cocoon.servlet.util.HttpContextHelper;

/**
 * <p>
 * A fancy XML serializer not relying on the JAXP API.
 * </p>
 *
 * @version $Id: EncodingXMLSerializer.java 825425 2009-10-15 07:51:28Z reinhard $
 */
public class EncodingXMLSerializer extends org.apache.cocoon.components.serializers.util.XMLSerializer implements
        SAXPipelineComponent, Finisher, SAXConsumer, CachingPipelineComponent {

    private String encoding;
    private int indent = 0;

    public CacheKey constructCacheKey() {
        ParameterCacheKey parameterCacheKey = new ParameterCacheKey();
        parameterCacheKey.addParameter("encoding", this.encoding);
        parameterCacheKey.addParameter("indent", this.indent);
        return parameterCacheKey;
    }

    public void finish() {
    }

    public String getContentType() {
        return this.getMimeType();
    }

    public void setConfiguration(Map<String, ? extends Object> configuration) {
        try {
            this.encoding = ConfigurationUtils.getEncoding(configuration);
            this.setEncoding(this.encoding);
        } catch (UnsupportedEncodingException e) {
            throw new SetupException(e);
        }

        try {
            this.indent = ConfigurationUtils.getIndent(configuration);
            this.setIndentPerLevel(this.indent);
        } catch (NumberFormatException nfe) {
            throw new SetupException(nfe);
        }

    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        try {
            super.setOutputStream(outputStream);
        } catch (IOException e) {
            throw new SetupException(e);
        }
    }

    public void setup(Map<String, Object> parameters) {
        this.setup(HttpContextHelper.getRequest(parameters));
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this);
    }
}
