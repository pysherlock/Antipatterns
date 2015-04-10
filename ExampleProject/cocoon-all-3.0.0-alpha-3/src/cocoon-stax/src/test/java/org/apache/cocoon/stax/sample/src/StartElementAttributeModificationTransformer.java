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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.cocoon.stax.AbstractStAXTransformer;
import org.apache.cocoon.stax.navigation.FindStartElementNavigator;

/**
 * A Transformer that allows to modify the attributes of a specific element.
 * <p>
 * It allows to specify a specific element by name and attributes and to change
 * its attributes.
 * </p>
 */
public final class StartElementAttributeModificationTransformer extends AbstractStAXTransformer {

    private FindStartElementNavigator navigator;
    private StartElement newStartElement;

    /**
     * Creates a new {@link StartElementAttributeModificationTransformer} that
     * allows to specify a specific element by name and attributes and to change
     * its attributes.
     * 
     * @param name
     *            The name of the elements that shall be modified.
     * @param attributes
     *            The attributes the element must contain in order to be
     *            modified.
     * @param newAttributes
     *            The new attributes of the element.
     */
    public StartElementAttributeModificationTransformer(String name, List<Attribute> attributes,
            List<Attribute> newAttributes) {
        this.navigator = new FindStartElementNavigator(name, attributes);
        XMLEventFactory factory = XMLEventFactory.newInstance();

        this.newStartElement = factory.createStartElement(new QName(name), newAttributes.iterator(), null);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.AbstractStAXTransformer#produceEvents()
     */
    @Override
    public void produceEvents() throws XMLStreamException {
        if (!this.getParent().hasNext()) {
            return;
        }
        XMLEvent event = this.getParent().nextEvent();
        if (this.navigator.fulfillsCriteria(event)) {
            this.addEventToQueue(this.newStartElement);
            return;
        }
        this.addEventToQueue(event);
    }
}
