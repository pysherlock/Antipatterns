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

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.apache.cocoon.stax.AbstractStAXTransformer;
import org.apache.cocoon.stax.navigation.InSubtreeNavigator;
import org.apache.cocoon.stax.navigation.Navigator;

/**
 * A transformer that allows to delete subtrees of elements.
 * <p>
 * It allows to delete subtrees of elements beginning with specified elements containing specified
 * attributes.
 * </p>
 */
public final class DeletionTransformer extends AbstractStAXTransformer {

    private Navigator navigator;

    /**
     * Creates a new {@link DeletionTransformer} that allows to delete subtrees of elements
     * beginning with specified elements containing specified attributes.
     * 
     * @param name The name of the element which will be deleted including all subelements.
     * @param attributes The attributes an element has to contain in order to get deleted.
     */
    public DeletionTransformer(String name, List<Attribute> attributes) {
        this.navigator = new InSubtreeNavigator(name, attributes);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.AbstractStAXTransformer#nextEvent()
     */
    @Override
    public void produceEvents() throws XMLStreamException {
        if (!this.getParent().hasNext()) {
            return;
        }

        XMLEvent event = this.getParent().nextEvent();
        while (this.navigator.fulfillsCriteria(event) && this.getParent().hasNext()) {
            event = this.getParent().nextEvent();
        }
        this.addEventToQueue(event);
    }
}
