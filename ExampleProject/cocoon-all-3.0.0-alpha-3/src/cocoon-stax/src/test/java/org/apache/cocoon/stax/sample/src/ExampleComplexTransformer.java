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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.cocoon.stax.AbstractStAXTransformer;
import org.apache.cocoon.stax.navigation.FindEndElementNavigator;
import org.apache.cocoon.stax.navigation.FindStartElementNavigator;
import org.apache.cocoon.stax.navigation.InSubtreeNavigator;
import org.apache.cocoon.stax.navigation.Navigator;

/**
 * A transformer explicitly for the example unit tests that performs several
 * operations at once.
 * <p>
 * It allows to delete all elements named "anyelement" containing an attribute
 * named "attribute" that has the value "good" and to modify elements named
 * "anyelement" with an attribute named "attribute" that has the value "bad" to
 * an element named "something different.
 * </p>
 */
public class ExampleComplexTransformer extends AbstractStAXTransformer {

    private XMLEventFactory eventFact = XMLEventFactory.newInstance();

    private Navigator inRoot = new InSubtreeNavigator("root");
    private Navigator inLevel2 = new InSubtreeNavigator("level2");
    private Navigator inGoodAnyElement = new InSubtreeNavigator("anyelement", this.eventFact.createAttribute(
            "attribute", "good"));
    private Navigator badAnyElementStart = new FindStartElementNavigator("anyelement", this.eventFact.createAttribute(
            "attribute", "bad"));
    private Navigator badAnyElementStop = new FindEndElementNavigator("anyelement", this.eventFact.createAttribute(
            "attribute", "bad"));

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
        if (this.inRoot.fulfillsCriteria(event) && this.inLevel2.fulfillsCriteria(event)) {

            while (this.inGoodAnyElement.fulfillsCriteria(event) && this.getParent().hasNext()) {
                event = this.getParent().nextEvent();
            }

            if (this.badAnyElementStop.fulfillsCriteria(event)) {
                this.createSomoethingDifferentEnd();
                return;
            }

            if (this.badAnyElementStart.fulfillsCriteria(event)) {
                this.createSomethingDifferentStart();
                return;
            }

        }
        this.addEventToQueue(event);
    }

    /**
     * Private helper method that creates a StartElement with the name
     * "somethingdifferent" and the attribute "attribute" and its value
     * "neutral".
     */
    private void createSomethingDifferentStart() {
        this.addEventToQueue(this.eventFact.createStartElement(new QName("somethingdifferent"), null, null));
        this.addEventToQueue(this.eventFact.createAttribute("attribute", "neutral"));
    }

    /**
     * Private helper method that creates a EndElement with the name
     * "somethingdifferent".
     */
    private void createSomoethingDifferentEnd() {
        this.addEventToQueue(this.eventFact.createStartElement(new QName("color"), null, null));
        this.addEventToQueue(this.eventFact.createCharacters("green"));
        this.addEventToQueue(this.eventFact.createEndElement(new QName("color"), null));
        this.addEventToQueue(this.eventFact.createStartElement(new QName("addition"), null, null));
        this.addEventToQueue(this.eventFact.createEndElement(new QName("addition"), null));
        this.addEventToQueue(this.eventFact.createEndElement(new QName("somethingdifferent"), null));
    }
}
