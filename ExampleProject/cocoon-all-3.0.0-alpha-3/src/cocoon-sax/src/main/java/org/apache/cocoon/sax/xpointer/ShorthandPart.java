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

import org.apache.cocoon.sax.SAXConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Implements support for shorthand XPointers (= id-based lookup). We treat them here as if they
 * were a pointerpart too.
 *
 * <p>Note that although this is implemented here, this feature depends on the presence of a DTD,
 * and a validating parser. Currently, this means its unuseable within Cocoon.
 * 
 * $Id: ShorthandPart.java 892809 2009-12-21 13:14:59Z reinhard $
 */
public final class ShorthandPart extends AbstractPointerPart {

    private final static String XMLNS_NAMESPACE_98 = "http://www.w3.org/XML/1998/namespace";

    private final static String XMLNS_NAMESPACE_00 = "http://www.w3.org/2000/xmlns/";

    private final static String ID = "id";

    private final String shorthand;

    private SAXConsumer saxConsumer;

    private boolean matching = false;

    private int matchingLevel = 0;

    public ShorthandPart(final String shorthand) {
        this.shorthand = shorthand;
    }

    public void setUp(final XPointerContext xpointerContext) throws SAXException, IOException {
        this.saxConsumer = xpointerContext.getSaxConsumer();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.matching) {
            this.saxConsumer.characters(ch, start, length);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        this.saxConsumer.endDocument();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (this.matching) {
            this.saxConsumer.endElement(uri, localName, qName);
            this.matchingLevel--;

            if (this.matchingLevel == 0) {
                this.matching = false;
            }
        }
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        if (this.matching) {
            this.saxConsumer.endPrefixMapping(prefix);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        if (this.matching) {
            this.saxConsumer.ignorableWhitespace(ch, start, length);
        }
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        if (this.matching) {
            this.saxConsumer.processingInstruction(target, data);
        }
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        // ignored, already set on the sax consumer
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        if (this.matching) {
            this.saxConsumer.skippedEntity(name);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        this.saxConsumer.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (!this.matching) {
            dance: for (int i = 0; i < atts.getLength(); i++) {
                String attributeURI = atts.getURI(i);
                String attributeName = atts.getLocalName(i);
                String attributeValue = atts.getValue(i);

                if ((attributeURI == null
                        || attributeURI.length() == 0
                        || XMLNS_NAMESPACE_98.equals(attributeURI)
                        || XMLNS_NAMESPACE_00.equals(attributeURI))
                        && ID.equalsIgnoreCase(attributeName)
                        && this.shorthand.equals(attributeValue)) {
                    this.matching = true;
                    this.matchingLevel = 0;

                    break dance;
                }
            }
        }

        if (this.matching) {
            this.saxConsumer.startElement(uri, localName, qName, atts);
            this.matchingLevel++;
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (this.matching) {
            this.saxConsumer.startPrefixMapping(prefix, uri);
        }
    }
}
