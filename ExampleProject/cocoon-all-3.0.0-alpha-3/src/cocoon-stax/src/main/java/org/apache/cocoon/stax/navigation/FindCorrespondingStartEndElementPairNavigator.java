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
package org.apache.cocoon.stax.navigation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * This implementation of the {@link Navigator} returns true every time a {@link StartElement} is
 * entered to {@link Navigator#fulfillsCriteria(XMLEvent)} and matches all other conditions entered
 * to the {@link Navigator} during its creation. Also true is returned if the corresponding
 * {@link EndElement} is found. This {@link Navigator} is quite similar to the
 * {@link InSubtreeNavigator} with the difference that false is returned for all elements between
 * the found {@link StartElement} and {@link EndElement} except another {@link StartElement}
 * matching all conditions.
 * 
 * <pre>
 *  &lt;anyElement&gt; -&gt; false
 *      &lt;searchedElement&gt; -&gt; true
 *          &lt;searchedElement&gt; -&gt; true
 *          &lt;anyElement/&gt; -&gt; false
 *          &lt;/searchedElement&gt; -&gt; true
 *      &lt;/searchedElement&gt; -&gt; true
 *  &lt;/anyElement&gt; -&gt; false
 * </pre>
 * 
 */
public class FindCorrespondingStartEndElementPairNavigator implements Navigator {

    private String name;
    private List<Attribute> attributes = new ArrayList<Attribute>();
    private List<String> levelRememberer = new ArrayList<String>();
    private int count = 0;
    private boolean active;

    public FindCorrespondingStartEndElementPairNavigator(String name) {
        this.name = name;
    }

    public FindCorrespondingStartEndElementPairNavigator(String name, List<Attribute> attributes) {
        this.name = name;

        if (attributes != null) {
            this.attributes = attributes;
        }
    }

    public FindCorrespondingStartEndElementPairNavigator(String name, Attribute... attributes) {
        this.name = name;

        if (attributes != null) {
            this.attributes = new ArrayList<Attribute>(Arrays.asList(attributes));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.navigation.Navigator#fulfillsCriteria(javax.xml.stream.events.XMLEvent)
     */
    public boolean fulfillsCriteria(XMLEvent event) {
        if (event.isStartElement()) {
            StartElement element = (StartElement) event;
            if (!element.getName().getLocalPart().equals(this.name)) {
                this.active = false;
                return this.active;
            }
            this.count++;
            for (Attribute attribute : this.attributes) {
                if (!attribute.getValue().equals(element.getAttributeByName(attribute.getName()).getValue())) {
                    this.active = false;
                    return this.active;
                }
            }
            this.levelRememberer.add(String.valueOf(this.count));
            this.active = true;
            return this.active;
        } else if (event.isEndElement()) {
            EndElement element = (EndElement) event;
            if (!element.getName().getLocalPart().equals(this.name) || this.count <= 0) {
                this.active = false;
                return this.active;
            }
            if (this.levelRememberer.contains(String.valueOf(this.count))) {
                this.levelRememberer.remove(String.valueOf(this.count));
                this.count--;
                this.active = true;
                return this.active;
            }
            this.count--;
        }

        this.active = false;
        return this.active;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.navigation.Navigator#isActive()
     */
    public boolean isActive() {
        return this.active;
    }
}
