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
 * A pedantinc XHTML serializer encoding all recognized entities with their proper HTML names.
 * </p>
 * 
 * <p>
 * For configuration options of this serializer, please look at the
 * {@link org.apache.cocoon.components.serializers.util.EncodingSerializer}, in addition to those,
 * this serializer also support the specification of a default doctype. This default will be used if
 * no document type is received in the SAX events, and can be configured in the following way:
 * </p>
 * 
 * <pre>
 * &lt;map:serialize type=&quot;exhtml&quot; doctype-default=&quot;mytype&quot; /&gt;
 * </pre>
 * 
 * <p>
 * The value <i>mytype</i> can be one of:
 * </p>
 * 
 * <dl>
 * <dt>"<code>none</code>"</dt>
 * <dd>Not to emit any dococument type declaration.</dd>
 * <dt>"<code>strict</code>"</dt>
 * <dd>The XHTML 1.0 Strict document type.</dd>
 * <dt>"<code>loose</code>"</dt>
 * <dd>The XHTML 1.0 Transitional document type.</dd>
 * <dt>"<code>frameset</code>"</dt>
 * <dd>The XHTML 1.0 Frameset document type.</dd>
 * </dl>
 * 
 *
 * @version $Id: EncodingXHTMLSerializer.java 825425 2009-10-15 07:51:28Z reinhard $
 */
public class EncodingXHTMLSerializer extends org.apache.cocoon.components.serializers.util.XHTMLSerializer implements
        SAXPipelineComponent, Finisher, SAXConsumer, CachingPipelineComponent {

    private String encoding;
    private int indent = 0;
    private String omitXmlDeclaration;
    private String docType;

    public CacheKey constructCacheKey() {
        ParameterCacheKey parameterCacheKey = new ParameterCacheKey();
        parameterCacheKey.addParameter("encoding", this.encoding);
        parameterCacheKey.addParameter("indent", this.indent);
        parameterCacheKey.addParameter("docType", this.docType);
        parameterCacheKey.addParameter("omitXmlDeclaration", this.omitXmlDeclaration);
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

        this.omitXmlDeclaration = (String) configuration.get("omit-xml-declaration");
        this.setOmitXmlDeclaration(this.omitXmlDeclaration);

        this.docType = (String) configuration.get("doctype-default");
        this.setDoctypeDefault(this.docType);
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
