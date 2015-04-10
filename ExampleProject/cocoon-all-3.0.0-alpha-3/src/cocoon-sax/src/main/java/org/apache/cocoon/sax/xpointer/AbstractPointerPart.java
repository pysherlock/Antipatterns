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

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * $Id: AbstractPointerPart.java 892809 2009-12-21 13:14:59Z reinhard $
 */
abstract class AbstractPointerPart implements PointerPart {

    public void characters(char[] ch, int start, int length) throws SAXException {
        // do nothing as default behavior
    }

    public void endDocument() throws SAXException {
        // do nothing as default behavior
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        // do nothing as default behavior
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        // do nothing as default behavior
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        // do nothing as default behavior
    }

    public void processingInstruction(String target, String data) throws SAXException {
        // do nothing as default behavior
    }

    public void setDocumentLocator(Locator locator) {
        // do nothing as default behavior
    }

    public void skippedEntity(String name) throws SAXException {
        // do nothing as default behavior
    }

    public void startDocument() throws SAXException {
        // do nothing as default behavior
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        // do nothing as default behavior
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // do nothing as default behavior
    }
}
