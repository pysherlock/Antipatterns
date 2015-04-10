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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * $Id: XPointer.java 892809 2009-12-21 13:14:59Z reinhard $
 */
public final class XPointer implements ContentHandler {

    private final List<PointerPart> pointerParts = new ArrayList<PointerPart>();

    private Log log;

    public void setLog(Log log) {
        this.log = log;
    }

    public void addPart(final PointerPart part) {
        this.pointerParts.add(part);
    }

    public void setUp(final XPointerContext xpointerContext) throws SAXException, IOException {
        for (PointerPart pointerPart : this.pointerParts) {
            pointerPart.setUp(xpointerContext);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        for (ContentHandler contentHandler : this.pointerParts) {
            try {
                contentHandler.characters(ch, start, length);
            } catch (SAXException e) {
                if (this.log.isWarnEnabled()) {
                    this.log.warn("An error occurred while spreading characters", e);
                }
            }
        }
    }

    public void endDocument() throws SAXException {
        for (ContentHandler contentHandler : this.pointerParts) {
            try {
                contentHandler.endDocument();
            } catch (SAXException e) {
                if (this.log.isWarnEnabled()) {
                    this.log.warn("An error occurred while spreading endDocument", e);
                }
            }
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        for (ContentHandler contentHandler : this.pointerParts) {
            try {
                contentHandler.endElement(uri, localName, qName);
            } catch (SAXException e) {
                if (this.log.isWarnEnabled()) {
                    this.log.warn("An error occurred while spreading endElement", e);
                }
            }
        }
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        for (ContentHandler contentHandler : this.pointerParts) {
            try {
                contentHandler.endPrefixMapping(prefix);
            } catch (SAXException e) {
                if (this.log.isWarnEnabled()) {
                    this.log.warn("An error occurred while spreading endPrefixMapping", e);
                }
            }
        }
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        for (ContentHandler contentHandler : this.pointerParts) {
            try {
                contentHandler.ignorableWhitespace(ch, start, length);
            } catch (SAXException e) {
                if (this.log.isWarnEnabled()) {
                    this.log.warn("An error occurred while spreading ignorableWhitespace", e);
                }
            }
        }
    }

    public void processingInstruction(String target, String data) throws SAXException {
        for (ContentHandler contentHandler : this.pointerParts) {
            try {
                contentHandler.processingInstruction(target, data);
            } catch (SAXException e) {
                if (this.log.isWarnEnabled()) {
                    this.log.warn("An error occurred while spreading processingInstruction", e);
                }
            }
        }
    }

    public void setDocumentLocator(Locator locator) {
        for (ContentHandler contentHandler : this.pointerParts) {
            contentHandler.setDocumentLocator(locator);
        }
    }

    public void skippedEntity(String name) throws SAXException {
        for (ContentHandler contentHandler : this.pointerParts) {
            try {
                contentHandler.skippedEntity(name);
            } catch (SAXException e) {
                if (this.log.isWarnEnabled()) {
                    this.log.warn("An error occurred while spreading skippedEntity", e);
                }
            }
        }
    }

    public void startDocument() throws SAXException {
        for (ContentHandler contentHandler : this.pointerParts) {
            try {
                contentHandler.startDocument();
            } catch (SAXException e) {
                if (this.log.isWarnEnabled()) {
                    this.log.warn("An error occurred while spreading skippedEntity", e);
                }
            }
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        for (ContentHandler contentHandler : this.pointerParts) {
            try {
                contentHandler.startElement(uri, localName, qName, atts);
            } catch (SAXException e) {
                if (this.log.isWarnEnabled()) {
                    this.log.warn("An error occurred while spreading skippedEntity", e);
                }
            }
        }
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        for (ContentHandler contentHandler : this.pointerParts) {
            try {
                contentHandler.startPrefixMapping(prefix, uri);
            } catch (SAXException e) {
                if (this.log.isWarnEnabled()) {
                    this.log.warn("An error occurred while spreading skippedEntity", e);
                }
            }
        }
    }
}
