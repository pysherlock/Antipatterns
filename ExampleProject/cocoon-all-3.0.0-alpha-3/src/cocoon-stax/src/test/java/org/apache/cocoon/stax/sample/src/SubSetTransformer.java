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
package org.apache.cocoon.stax.sample.src;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.apache.cocoon.stax.AbstractStAXTransformer;
import org.apache.cocoon.stax.navigation.InSubtreeNavigator;

/**
 * A transformer that deletes all elements in a xml document but a specified sub tree.
 */
public class SubSetTransformer extends AbstractStAXTransformer {

    private InSubtreeNavigator navigator;
    private XMLEvent lastEvent;
    private boolean lastState = false;
    private boolean started = false;

    /**
     * Creates a new {@link SubSetTransformer} that deletes all elements in a xml document but a
     * specified sub tree.
     * 
     * @param name The name of the element that shall be the root of the sub tree that is not
     *            deleted.
     * @param attribute The attributes an element must contain in order to be the root of the sub
     *            tree that is not deleted.
     */
    public SubSetTransformer(String name, Attribute... attribute) {
        this.navigator = new InSubtreeNavigator(name, attribute);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.AbstractStAXTransformer#produceEvents()
     */
    @Override
    protected void produceEvents() throws XMLStreamException {
        XMLEvent event = null;
        while (this.getParent().hasNext()) {
            event = this.getParent().nextEvent();
            if (event.isStartDocument() || event.isEndDocument() || event.getEventType() == XMLStreamConstants.COMMENT) {
                XMLEvent actualOld = this.lastEvent;
                this.lastEvent = event;
                if (!this.started) {
                    this.started = true;
                } else {
                    this.addEventToQueue(actualOld);
                    return;
                }
            }
            if (this.navigator.fulfillsCriteria(event)) {
                XMLEvent actualOld = this.lastEvent;
                this.lastEvent = event;
                this.lastState = true;
                if (!this.started) {
                    this.started = true;
                } else {
                    this.addEventToQueue(actualOld);

                    return;
                }
            } else {
                if (this.lastState == true) {
                    while (this.getParent().hasNext()) {
                        this.getParent().nextEvent();
                    }
                    this.addEventToQueue(this.lastEvent);

                    return;
                }

                continue;
            }
        }
    }
}
