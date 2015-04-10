/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.cocoon.stax.component;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.cocoon.stax.AbstractStAXTransformer;

/**
 * Transformer which is used to clean a xml document from all whitespaces, comments and namespace
 * start prefixes and end prefixes. Works similar to the
 * {@link org.apache.cocoon.sax.component.CleaningTransformer} for SAX pipelines.
 */
public class CleaningTransformer extends AbstractStAXTransformer {

    private static XMLEventFactory eventFactory = XMLEventFactory.newInstance();

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.stax.AbstractStAXTransformer#produceEvents()
     */
    @Override
    public void produceEvents() throws XMLStreamException {
        while (this.getParent().hasNext()) {
            XMLEvent event = this.getParent().nextEvent();
            if (event.isCharacters()) {
                // remove char-events if only whitespaces
                String allChars = event.asCharacters().getData();
                for (int charCounter = 0; charCounter < allChars.length(); charCounter++) {
                    char eachChar = allChars.charAt(charCounter);
                    if (!Character.isWhitespace(eachChar) || eachChar == '\u00A0') {
                        this.addEventToQueue(event);
                        return;
                    }
                }
                continue;
            } else if (event.getEventType() == XMLStreamConstants.COMMENT) {
                // remove comments
                continue;
            } else if (event.isStartElement()) {
                // handle namespaces
                StartElement startElement = event.asStartElement();
                this.addEventToQueue(eventFactory.createStartElement("", startElement.getNamespaceURI(""), startElement
                        .getName().getLocalPart(), startElement.getAttributes(), null, startElement
                        .getNamespaceContext()));
                return;
            }
            this.addEventToQueue(event);
            return;
        }
    }
}
