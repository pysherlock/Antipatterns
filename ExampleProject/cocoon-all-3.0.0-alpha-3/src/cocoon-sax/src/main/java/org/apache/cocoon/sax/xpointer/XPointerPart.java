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
package org.apache.cocoon.sax.xpointer;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * $Id: XPointerPart.java 892809 2009-12-21 13:14:59Z reinhard $
 */
public final class XPointerPart extends AbstractPointerPart {

    /**
     * A generic transformer factory to parse XSLTs.
     */
    private static final SAXTransformerFactory TRAX_FACTORY = (SAXTransformerFactory) TransformerFactory.newInstance();

    private static final String XMLNS_PATTERN = "xmlns:%s=\"%s\"";

    private static final String XSLT_PATTERN =
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" %s>"
        + "<xsl:template match=\"%s\">"
        + "<xsl:copy-of select=\".\"/>"
        + "</xsl:template></xsl:stylesheet>";

    private static final Map<String, Templates> TEMPLATES = new HashMap<String, Templates>();

    private final String expression;

    private TransformerHandler traxHandler;

    public XPointerPart(final String expression) {
        this.expression = expression;
    }

    public void setUp(final XPointerContext xpointerContext) throws SAXException, IOException {
        Templates templates;
        if (TEMPLATES.containsKey(this.expression)) {
            templates = TEMPLATES.get(this.expression);
        } else {
            StringBuilder builder = new StringBuilder();
            for (Entry<String, String> namespace : xpointerContext.getNamespaces().entrySet()) {
                builder.append('\n');
                builder.append(String.format(XMLNS_PATTERN, namespace.getKey(), namespace.getValue()));
            }
            String xmlNamespaces = builder.toString();

            String xslt = String.format(XSLT_PATTERN, xmlNamespaces, this.expression);
            Source source = new StreamSource(new StringReader(xslt));
            try {
                templates = TRAX_FACTORY.newTemplates(source);
            } catch (TransformerConfigurationException tce) {
                throw new SAXException("XPointer expression '"
                        + this.expression
                        + "' not valid as used in the fragment identifier '"
                        + xpointerContext.getXPointer()
                        + "'", tce);
            }
            TEMPLATES.put(this.expression, templates);
        }

        final SAXResult result = new SAXResult();
        result.setHandler(xpointerContext.getSaxConsumer());

        try {
            this.traxHandler = TRAX_FACTORY.newTransformerHandler(templates);
            this.traxHandler.setResult(result);
        } catch (TransformerConfigurationException tce) {
            throw new SAXException("Impossible to initialize transformer handler for XPointer expression '"
                    + this.expression
                    + "' as used in the fragment identifier '"
                    + xpointerContext.getXPointer()
                    + "'", tce);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        this.traxHandler.characters(ch, start, length);
    }

    @Override
    public void endDocument() throws SAXException {
        this.traxHandler.endDocument();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        this.traxHandler.endElement(uri, localName, qName);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        this.traxHandler.endPrefixMapping(prefix);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        this.traxHandler.ignorableWhitespace(ch, start, length);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        this.traxHandler.processingInstruction(target, data);
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.traxHandler.setDocumentLocator(locator);
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        this.traxHandler.skippedEntity(name);
    }

    @Override
    public void startDocument() throws SAXException {
        this.traxHandler.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        this.traxHandler.startElement(uri, localName, qName, atts);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        this.traxHandler.startPrefixMapping(prefix, uri);
    }
}
