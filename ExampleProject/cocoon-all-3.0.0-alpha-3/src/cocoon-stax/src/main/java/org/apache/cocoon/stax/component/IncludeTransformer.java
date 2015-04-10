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
package org.apache.cocoon.stax.component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.component.AbstractPipelineComponent;
import org.apache.cocoon.pipeline.util.StringRepresentation;
import org.apache.cocoon.stax.AbstractStAXTransformer;
import org.apache.cocoon.stax.StAXProducer;
import org.apache.cocoon.stax.navigation.Navigator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A transformer that includes a xml document into another at the place where a include tag is found
 * pointing at the document to include.
 */
public class IncludeTransformer extends AbstractStAXTransformer {

    private final Log logger = LogFactory.getLog(this.getClass());

    private static final String INCLUDE_NS = "http://apache.org/cocoon/3.0/include";

    private static final String INCLUDE_EL = "include";

    private static final String SRC_ATTR = "src";

    private static final QName INCLUDE_QNAME = new QName(INCLUDE_NS, INCLUDE_EL);

    private URL baseUrl;

    private Navigator isIncludeEnd;

    private Navigator isIncludeStart;

    private StAXProducer includeDelegate;

    /**
     * Creates a new {@link IncludeTransformer} that includes a xml document into another at the
     * place where a include tag is found pointing at the document to include.
     */
    public IncludeTransformer() {
        this.isIncludeStart = new Navigator() {

            private boolean active;

            public boolean fulfillsCriteria(XMLEvent event) {
                if (event.isStartElement()) {
                    this.active = event.asStartElement().getName().equals(IncludeTransformer.INCLUDE_QNAME);
                    return this.active;
                }
                this.active = false;
                return this.active;
            }

            public boolean isActive() {
                return this.active;
            }
        };
        this.isIncludeEnd = new Navigator() {

            private boolean active;

            public boolean fulfillsCriteria(XMLEvent event) {
                if (event.isEndElement()) {
                    this.active = event.asEndElement().getName().equals(IncludeTransformer.INCLUDE_QNAME);
                    return this.active;
                }
                this.active = false;
                return this.active;
            }

            public boolean isActive() {
                return this.active;
            }
        };

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.AbstractStAXTransformer#produceEvents()
     */
    @Override
    public void produceEvents() throws XMLStreamException {
        while (this.getParent().hasNext()) {
            if (this.includeDelegate == null) {
                XMLEvent event = this.getParent().nextEvent();
                if (this.isIncludeStart.fulfillsCriteria(event)) {
                    Attribute sourceAttribute = event.asStartElement().getAttributeByName(new QName(SRC_ATTR));
                    URL source = this.createSource(sourceAttribute.getValue());
                    try {
                        this.includeDelegate = new XMLGenerator(source.openConnection().getInputStream());
                    } catch (IOException e) {
                        String message = "Can't read from URL " + sourceAttribute.getValue();
                        this.logger.error(message, e);
                        throw new ProcessingException(message, e);
                    }
                } else if (!this.isIncludeEnd.fulfillsCriteria(event)) {
                    this.addEventToQueue(event);
                    return;
                }

            } else {
                if (this.includeDelegate.hasNext()) {
                    XMLEvent event = this.includeDelegate.nextEvent();
                    if (!event.isStartDocument() && !event.isEndDocument()) {
                        this.addEventToQueue(event);
                        return;
                    }
                } else {
                    this.includeDelegate = null;
                }
            }
        }
    }

    /**
     * Creates a url from a string.
     * 
     * @param sourceAtt The url represented as a string.
     * @return The url of the specified string.
     */
    private URL createSource(String sourceAtt) {
        try {
            URL source = null;
            if (sourceAtt.contains(":")) {
                source = new URL(sourceAtt);
            } else {
                source = new URL(this.baseUrl, sourceAtt);
            }
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Including source: " + source);
            }

            return source;
        } catch (MalformedURLException e) {
            String message = "Can't parse URL " + sourceAtt;
            this.logger.error(message, e);
            throw new ProcessingException(message, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractPipelineComponent#setConfiguration(Map)
     */
    @Override
    public void setConfiguration(Map<String, ? extends Object> configuration) {
        this.setBaseUrl((URL) configuration.get("baseUrl"));
    }

    /**
     * Sets the base url for this transformer.
     * 
     * @param baseUrl The new base url
     */
    public void setBaseUrl(URL baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "baseUrl=" + this.baseUrl);
    }
}
