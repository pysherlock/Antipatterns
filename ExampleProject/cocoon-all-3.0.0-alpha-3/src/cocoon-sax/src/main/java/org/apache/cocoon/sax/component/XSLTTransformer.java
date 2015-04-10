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
package org.apache.cocoon.sax.component;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.pipeline.util.StringRepresentation;
import org.apache.cocoon.sax.AbstractSAXTransformer;
import org.apache.cocoon.sax.SAXConsumer;
import org.apache.cocoon.sax.util.InMemoryLRUResourceCache;
import org.apache.cocoon.sax.util.SAXConsumerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XSLTTransformer extends AbstractSAXTransformer {

    /**
     * The memory based LRU cache for already loaded XSLTs.
     */
    private static final InMemoryLRUResourceCache<Templates> XSLT_CACHE = new InMemoryLRUResourceCache<Templates>();

    /**
     * A generic transformer factory to parse XSLTs.
     */
    private static final SAXTransformerFactory TRAX_FACTORY = createNewSAXTransformerFactory();

    /**
     * The XSLT parameters name pattern.
     */
    private static final Pattern XSLT_PARAMETER_NAME_PATTERN = Pattern.compile("[a-zA-Z_][\\w\\-\\.]*");

    /**
     * This class log.
     */
    private final Log log = LogFactory.getLog(XSLTTransformer.class);

    /**
     * The XSLT parameters reference.
     */
    private Map<String, Object> parameters;

    /**
     * The XSLT URL source.
     */
    private URL source;

    /**
     * The XSLT Template reference.
     */
    private Templates templates;

    /**
     * Empty constructor, used in sitemap.
     */
    public XSLTTransformer() {
        super();
    }

    /**
     * Creates a new transformer reading the XSLT from the URL source.
     *
     * @param source the XSLT URL source
     */
    public XSLTTransformer(final URL source) {
        this(source, null);
    }

    /**
     * Creates a new transformer reading the XSLT from the URL source and setting the Transformer
     * Factory attributes.
     *
     * This constructor is useful when users want to perform XSLT transformation using <a
     * href="http://xml.apache.org/xalan-j/xsltc_usage.html">xsltc</a>.
     *
     * @param source the XSLT URL source
     * @param attributes the Transformer Factory attributes
     */
    public XSLTTransformer(final URL source, final Map<String, Object> attributes) {
        super();
        this.loadXSLT(source, attributes);
    }

    /**
     * Method useful to create a new transformer reading the XSLT from the URL source and setting
     * the Transformer Factory attributes.
     *
     * This method is useful when users want to perform XSLT transformation using <a
     * href="http://xml.apache.org/xalan-j/xsltc_usage.html">xsltc</a>.
     *
     * @param source the XSLT URL source
     * @param attributes the Transformer Factory attributes
     */
    private void loadXSLT(final URL source, final Map<String, Object> attributes) {
        if (source == null) {
            throw new IllegalArgumentException("The parameter 'source' mustn't be null.");
        }

        this.source = source;

        // check the XSLT is in the cache first
        if (XSLT_CACHE.containsKey(source)) {
            // get the XSLT directly from the cache
            this.templates = XSLT_CACHE.get(this.source);
        } else {
            // XSLT has to be parsed
            Source urlSource = new StreamSource(this.source.toExternalForm());

            SAXTransformerFactory transformerFactory;
            if (attributes != null && !attributes.isEmpty()) {
                transformerFactory = createNewSAXTransformerFactory();
                for (Entry<String, Object> attribute : attributes.entrySet()) {
                    String name = attribute.getKey();
                    Object value = attribute.getValue();
                    transformerFactory.setAttribute(name, value);
                }
            } else {
                transformerFactory = TRAX_FACTORY;
            }

            try {
                this.templates = transformerFactory.newTemplates(urlSource);
                // store the XSLT into the cache for future reuse
                XSLT_CACHE.put(this.source, this.templates);
            } catch (TransformerConfigurationException e) {
                throw new SetupException("Impossible to read XSLT from '"
                        + this.source.toExternalForm()
                        + "', see nested exception", e);
            }
        }
    }

    /**
     * Sets the XSLT parameters to be applied to XSLT stylesheet.
     *
     * @param parameters the XSLT parameters to be applied to XSLT stylesheet
     */
    public void setParameters(final Map<String, ? extends Object> parameters) {
        if (parameters != null) {
            this.parameters = new HashMap<String, Object>(parameters);
        } else {
            this.parameters = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setConfiguration(final Map<String, ? extends Object> configuration) {
        try {
            this.source = (URL) configuration.get("source");
        } catch (ClassCastException cce) {
            throw new SetupException("The configuration value of 'source' can't be cast to java.net.URL.", cce);
        }

        if (this.source != null) {
            Object attributesObj = configuration.get("attributes");
            if (attributesObj != null && attributesObj instanceof Map) {
                this.loadXSLT(this.source, (Map) attributesObj);
            } else {
                this.loadXSLT(this.source, null);
            }
        } else {
            if (this.log.isDebugEnabled()) {
                this.log.debug("Impossible to load XSLT parameters from '" + this.source
                        + "' source, make sure it is NOT null and is a valid URL");
            }
        }

        this.setParameters(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setSAXConsumer(final SAXConsumer consumer) {
        TransformerHandler transformerHandler;
        try {
            transformerHandler = TRAX_FACTORY.newTransformerHandler(this.templates);
        } catch (Exception ex) {
            throw new SetupException("Could not initialize transformer handler.", ex);
        }

        if (this.parameters != null) {
            final Transformer transformer = transformerHandler.getTransformer();

            for (Entry<String, Object> entry : this.parameters.entrySet()) {
                String name = entry.getKey();

                // is valid XSLT parameter name
                if (XSLT_PARAMETER_NAME_PATTERN.matcher(name).matches()) {
                    transformer.setParameter(name, entry.getValue());
                }
            }
        }

        final SAXResult result = new SAXResult();
        result.setHandler(consumer);
        // According to TrAX specs, all TransformerHandlers are LexicalHandlers
        result.setLexicalHandler(consumer);
        transformerHandler.setResult(result);

        TraxErrorListener traxErrorListener = new TraxErrorListener(this.log, this.source.toExternalForm());
        transformerHandler.getTransformer().setErrorListener(traxErrorListener);

        SAXConsumerAdapter saxConsumerAdapter = new SAXConsumerAdapter();
        saxConsumerAdapter.setContentHandler(transformerHandler);
        super.setSAXConsumer(saxConsumerAdapter);
    }

    /**
     * Utility method to create a new transformer factory.
     *
     * @return a new transformer factory
     */
    private static SAXTransformerFactory createNewSAXTransformerFactory() {
        return (SAXTransformerFactory) TransformerFactory.newInstance();
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "src=" + this.source);
    }
}
