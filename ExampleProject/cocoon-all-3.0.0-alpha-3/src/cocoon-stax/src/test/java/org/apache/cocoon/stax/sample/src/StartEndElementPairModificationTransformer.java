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
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.cocoon.stax.AbstractStAXTransformer;
import org.apache.cocoon.stax.navigation.FindCorrespondingStartEndElementPairNavigator;

/**
 * A Transformer that allows to modify corresponding Start- and EndElement
 * Pairs.
 * <p>
 * It allows to specify an elements name as well as required attributes and to
 * transform all occurences of this constellation to another element with its
 * own attributes.
 * </p>
 */
public final class StartEndElementPairModificationTransformer extends AbstractStAXTransformer {

    private FindCorrespondingStartEndElementPairNavigator navigator;
    private StartElement newStartElement;
    private EndElement newEndElement;

    /**
     * Creates a new {@link StartElementAttributeModificationTransformer} that
     * allows to specify an elements name as well as required attributes and to
     * transform all occurences of this constellation to another element with
     * its own attributes.
     * 
     * @param name
     *            The name of the element that shall be modified.
     * @param attributes
     *            Required attributes an element has to contain in order to be
     *            modified.
     * @param newName
     *            The new name the specified elements will have after
     *            modification.
     * @param newAttributes
     *            The attributes the specified elements will have after
     *            modification.
     */
    public StartEndElementPairModificationTransformer(String name, List<Attribute> attributes, String newName,
            List<Attribute> newAttributes) {
        this.navigator = new FindCorrespondingStartEndElementPairNavigator(name, attributes);
        XMLEventFactory factory = XMLEventFactory.newInstance();
        this.newStartElement = factory.createStartElement(new QName(newName), newAttributes.iterator(), null);
        this.newEndElement = factory.createEndElement(new QName(newName), null);
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
            if (event.isStartElement()) {
                this.addEventToQueue(this.newStartElement);
                return;
            } else if (event.isEndElement()) {
                this.addEventToQueue(this.newEndElement);
                return;
            }
        }
        this.addEventToQueue(event);
    }

}
