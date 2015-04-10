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
package org.apache.cocoon.optional.pipeline.components.sax.neko;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.pipeline.util.StringRepresentation;
import org.apache.cocoon.sax.AbstractSAXGenerator;
import org.cyberneko.html.parsers.SAXParser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public class NekoGenerator extends AbstractSAXGenerator {

    private static final String BALANCE_TAGS_URI = "http://cyberneko.org/html/features/balance-tags";

    private static final String ELEMS_URI = "http://cyberneko.org/html/properties/names/elems";

    private static final String OVERRIDE_NAMESPACES_URI = "http://cyberneko.org/html/features/override-namespaces";

    private static final String INSERT_NAMESPACES_URI = "http://cyberneko.org/html/features/insert-namespaces";

    private static final String NAMESPACES_URI = "http://cyberneko.org/html/properties/namespaces-uri";

    private static final String XHTML_URL = "http://www.w3.org/1999/xhtml";

    private URL htmlSource;

    private SAXParser saxParser;

    public NekoGenerator() {
        super();
    }

    public NekoGenerator(URL htmlSource) {
        super();

        this.setHtmlSource(htmlSource);
    }

    public void execute() {
        this.saxParser.setContentHandler(this.getSAXConsumer());

        try {
            this.saxParser.parse(this.htmlSource.toExternalForm());
        } catch (SAXException e) {
            throw new ProcessingException("Fatal XML error", e);
        } catch (IOException e) {
            throw new ProcessingException("Fatal protocol violation", e);
        }
    }

    @Override
    public void setConfiguration(Map<String, ? extends Object> configuration) {
        super.setConfiguration(configuration);

        this.setHtmlSource((URL) configuration.get("source"));
    }

    public void setHtmlSource(URL htmlSource) {
        this.htmlSource = htmlSource;
    }

    @Override
    public void setup(Map<String, Object> parameters) {
        super.setup(parameters);

        this.saxParser = new SAXParser();
        try {
            this.saxParser.setFeature(BALANCE_TAGS_URI, true);
            this.saxParser.setProperty(ELEMS_URI, "lower");
            this.saxParser.setFeature(OVERRIDE_NAMESPACES_URI, true);
            this.saxParser.setFeature(INSERT_NAMESPACES_URI, true);
            this.saxParser.setProperty(NAMESPACES_URI, XHTML_URL);
        } catch (SAXNotRecognizedException e) {
            throw new SetupException("Impossible to set property to HTML Parser", e);
        } catch (SAXNotSupportedException e) {
            throw new SetupException("Property not supported by the HTML Parser", e);
        }
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "htmlSource=" + this.htmlSource);
    }
}
