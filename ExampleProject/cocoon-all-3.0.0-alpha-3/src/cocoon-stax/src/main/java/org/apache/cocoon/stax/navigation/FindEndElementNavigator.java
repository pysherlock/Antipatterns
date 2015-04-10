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
 * This implementation of the {@link Navigator} returns true of
 * {@link Navigator#fulfillsCriteria(XMLEvent)} any time it is called with a {@link EndElement} full
 * filling all conditions the {@link Navigator} was created with.
 * <p>
 * The "partner" of this {@link Navigator} is the {@link FindStartElementNavigator} working exactly
 * as the {@link FindEndElementNavigator} for the {@link StartElement}.
 * 
 * <pre>
 * &lt;anyElement&gt; -&gt; false
 *  &lt;searchedElement&gt; -&gt; false
 *      &lt;anyElement\&gt; -&gt; false
 *      &lt;searchedElement\&gt; -&gt; true
 *  &lt;\searchedElement&gt; -&gt; true
 * &lt;\anyElement&gt; -&gt; false
 * </pre>
 */
public class FindEndElementNavigator implements Navigator {

    private String name;
    private List<Attribute> attributes = new ArrayList<Attribute>();
    private List<String> levelRememberer = new ArrayList<String>();
    private int count = 0;
    private boolean active;

    public FindEndElementNavigator(String name) {
        this.name = name;
    }

    public FindEndElementNavigator(String name, List<Attribute> attributes) {
        this.name = name;

        if (attributes != null) {
            this.attributes = attributes;
        }

    }

    public FindEndElementNavigator(String name, Attribute... attributes) {
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
            this.active = false;
            return this.active;
        } else if (event.isEndElement()) {
            EndElement element = (EndElement) event;
            if (element.getName().getLocalPart().equals(this.name) && this.count > 0) {
                if (this.levelRememberer.contains(String.valueOf(this.count))) {
                    this.levelRememberer.remove(String.valueOf(this.count));
                    this.count--;
                    this.active = true;
                    return this.active;
                }
                this.count--;
            }
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
