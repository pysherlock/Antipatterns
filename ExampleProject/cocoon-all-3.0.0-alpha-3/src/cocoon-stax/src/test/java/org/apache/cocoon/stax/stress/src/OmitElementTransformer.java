/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.stax.stress.src;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.apache.cocoon.stax.AbstractStAXTransformer;
import org.apache.cocoon.stax.navigation.FindCorrespondingStartEndElementPairNavigator;
import org.apache.cocoon.stax.navigation.Navigator;

/**
 * This Transformer omits elements, that match certain criteria.
 */
public class OmitElementTransformer extends AbstractStAXTransformer {

    private Navigator navigator;

    /**
     * Creates a new {@link OmitElementTransformer} that omits elements matching certain criteria.
     * 
     * @param elementName The name of the elements that shall be omitted.
     * @param attributes The attributes an element must contain in order to be omitted.
     */
    public OmitElementTransformer(String elementName, List<Attribute> attributes) {
        this.navigator = new FindCorrespondingStartEndElementPairNavigator(elementName, attributes);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.AbstractStAXTransformer#produceEvents()
     */
    @Override
    protected void produceEvents() throws XMLStreamException {
        while (this.getParent().hasNext()) {
            XMLEvent event = this.getParent().nextEvent();
            if (!this.navigator.fulfillsCriteria(event)) {
                this.addEventToQueue(event);
                return;
            }
        }
    }
}
