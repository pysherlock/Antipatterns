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
package org.apache.cocoon.stax.stress.src;

import java.util.List;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.apache.cocoon.stax.AbstractStAXTransformer;
import org.apache.cocoon.stax.StAXConsumer;
import org.apache.cocoon.stax.navigation.FindStartElementNavigator;
import org.apache.cocoon.stax.navigation.Navigator;

/**
 * A transformer that emits new elements under a configurable parent.
 */
public class EmitElementTransformer extends AbstractStAXTransformer implements StAXConsumer {

    private static final int BATCH_SIZE = 1000;
    private final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private Navigator navigator;
    private String childElementName;
    private int elementCount;
    private boolean emitingElements;
    private int count;

    /**
     * Creates a new {@link EmitElementTransformer} that emits new elements under a configurable
     * parent.
     * 
     * @param parentElementName The name of the element that is the parent of the new emitted
     *            elements.
     * @param parentElementAttributes The attributes an element must contain in order to be the
     *            parent of the new emitted elements.
     * @param childElement The name the created children shall have.
     * @param elementCount The number of identical children that shall be emitted.
     */
    public EmitElementTransformer(String parentElementName, List<Attribute> parentElementAttributes,
            String childElement, int elementCount) {
        this.navigator = new FindStartElementNavigator(parentElementName, parentElementAttributes);
        this.childElementName = childElement;
        this.elementCount = elementCount;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.AbstractStAXTransformer#produceEvents()
     */
    @Override
    protected void produceEvents() throws XMLStreamException {
        if (!this.emitingElements && this.getParent().hasNext()) {
            XMLEvent event = this.getParent().nextEvent();
            this.addEventToQueue(event);

            if (this.navigator.fulfillsCriteria(event)) {
                this.emitingElements = true;
                this.count = 0;
            }
        } else {
            for (; this.count < this.elementCount;) {
                this.count++;
                this.addEventToQueue(this.eventFactory.createStartElement("", "", this.childElementName));
                this.addEventToQueue(this.eventFactory.createEndElement("", "", this.childElementName));
                if (this.count % BATCH_SIZE == 0) {
                    break;
                }
            }

            if (this.count >= this.elementCount) {
                this.emitingElements = false;
            }
        }
    }
}
