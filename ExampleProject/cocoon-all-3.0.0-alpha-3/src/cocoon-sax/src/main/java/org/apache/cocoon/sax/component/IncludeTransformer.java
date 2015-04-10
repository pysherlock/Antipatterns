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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.util.StringRepresentation;
import org.apache.cocoon.sax.AbstractSAXTransformer;
import org.apache.cocoon.xml.sax.EmbeddedSAXPipe;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class IncludeTransformer extends AbstractSAXTransformer {

    private final Log logger = LogFactory.getLog(this.getClass());

    private static final String INCLUDE_NS = "http://apache.org/cocoon/3.0/include";

    private static final String INCLUDE_EL = "include";

    private static final String SRC_ATTR = "src";

    private URL baseUrl;

    @Override
    public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
        if (!INCLUDE_NS.equals(uri) || !INCLUDE_EL.equals(localName)) {
            super.startElement(uri, localName, name, atts);
            return;
        }

        String sourceAtt = atts.getValue(SRC_ATTR);
        if (null == sourceAtt || "".equals(sourceAtt)) {
            throw new ProcessingException("The <include> element must contain a 'src' attribute that contains a URL.");
        }

        try {
            URL source = this.createSource(sourceAtt);

            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            EmbeddedSAXPipe embeddedSAXPipe = new EmbeddedSAXPipe(this.getSAXConsumer());
            xmlReader.setContentHandler(embeddedSAXPipe);
            xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", embeddedSAXPipe);

            BufferedInputStream inputStream = new BufferedInputStream(source.openStream());
            xmlReader.parse(new InputSource(inputStream));

            return;
        } catch (IOException e) {
            throw new ProcessingException(("Can't read from URL " + sourceAtt), e);
        }
    }

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
            throw new ProcessingException(("Can't parse URL " + sourceAtt), e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if (INCLUDE_NS.equals(uri) && INCLUDE_EL.equals(localName)) {
            return;
        }
        super.endElement(uri, localName, name);
    }

    @Override
    public void setConfiguration(Map<String, ? extends Object> configuration) {
        this.setBaseUrl((URL) configuration.get("baseUrl"));
    }

    public void setBaseUrl(URL baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "baseUrl=" + this.baseUrl);
    }
}
