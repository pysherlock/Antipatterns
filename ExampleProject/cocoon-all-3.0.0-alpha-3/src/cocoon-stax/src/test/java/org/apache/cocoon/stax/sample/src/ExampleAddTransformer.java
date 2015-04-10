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

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.apache.cocoon.stax.AbstractStAXTransformer;
import org.apache.cocoon.stax.navigation.FindStartElementNavigator;
import org.apache.cocoon.stax.navigation.Navigator;

/**
 * A transformer that adds "new" elements at specified positions to a document.
 * <p>
 * It allows to add an Start- EndElement pair named "new" inside of every element with a specified
 * name an containing specified attributes.
 * </p>
 */
public class ExampleAddTransformer extends AbstractStAXTransformer {

    private XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private Navigator navigator;

    /**
     * Creates a new {@link ExampleAddTransformer} that allows to add an Start- EndElement pair
     * named "new" inside of every element with a specified name an containing specified attributes.
     * 
     * @param name The name of the element inside of which the "new" element will be added.
     * @param attributes The attributes the element must contain in order to be expanded by the
     *            "new" element.
     */
    public ExampleAddTransformer(String name, List<Attribute> attributes) {
        this.navigator = new FindStartElementNavigator(name, attributes);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.AbstractStAXTransformer#procudeEvents()
     */
    @Override
    public void produceEvents() throws XMLStreamException {
        if (!this.getParent().hasNext()) {
            return;
        }

        XMLEvent event = this.getParent().nextEvent();
        this.addEventToQueue(event);

        if (this.navigator.fulfillsCriteria(event)) {
            this.addEventToQueue(this.eventFactory.createStartElement("", "", "new"));
            this.addEventToQueue(this.eventFactory.createEndElement("", "", "new"));
        }
    }
}
