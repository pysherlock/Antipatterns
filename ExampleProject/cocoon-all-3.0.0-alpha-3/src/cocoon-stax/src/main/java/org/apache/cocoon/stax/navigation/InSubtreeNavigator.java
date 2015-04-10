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
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * This {@link Navigator} implementation returns as long true as long it is in an opened clause. The
 * navigator starts the moment an {@link XMLEvent} is a {@link StartElement} and matches all
 * {@link Attribute}s and the name the {@link Navigator} is initialized with. The moment the
 * corresponding {@link EndElement} is inserted the {@link Navigator} returns false. If the same
 * element is contained nested more often the {@link Navigator} starts returning true the first
 * element is inserted to the {@link Navigator#fulfillsCriteria(XMLEvent)} method till exactly this
 * node is closed again with an {@link EndElement}.
 * 
 * <pre>
 *  &lt;anyElement&gt; -&gt; false
 *      &lt;searchedElement&gt; -&gt; true
 *          &lt;searchedElement&gt; -&gt; true
 *          &lt;anyElement/&gt; -&gt; true
 *          &lt;/searchedElement&gt; -&gt; true
 *      &lt;/searchedElement&gt; -&gt; true
 *  &lt;/anyElement&gt; -&gt; false
 * </pre>
 */
public final class InSubtreeNavigator implements Navigator {

    private String name;
    private List<Attribute> attributes = new ArrayList<Attribute>();
    private int count = 0;
    private boolean active;
    private MatchingType matchingType = MatchingType.StringMatch;

    public InSubtreeNavigator(String name) {
        this.name = name;
    }

    public InSubtreeNavigator(String name, List<Attribute> attributes) {
        this(name);

        if (attributes != null) {
            this.attributes = attributes;
        }
    }

    public InSubtreeNavigator(String name, Attribute... attributes) {
        this(name);

        if (attributes != null) {
            this.attributes = new ArrayList<Attribute>(Arrays.asList(attributes));
        }
    }

    public InSubtreeNavigator(String name, MatchingType matchingType, List<Attribute> attributes) {
        this(name, attributes);

        this.matchingType = matchingType;
    }

    public InSubtreeNavigator(String name, MatchingType matchingType, Attribute... attributes) {
        this(name, attributes);

        this.matchingType = matchingType;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.navigation.Navigator#fulfillsCriteria(javax.xml.stream.events.XMLEvent)
     */
    @SuppressWarnings("unchecked")
    public boolean fulfillsCriteria(XMLEvent event) {
        if (event.isStartElement()) {
            StartElement element = (StartElement) event;

            boolean nameMatches = false;

            switch (this.matchingType) {
            case RegexMatch:
                nameMatches = Pattern.compile(this.name).matcher(element.getName().getLocalPart()).matches();
                break;
            default:
                nameMatches = element.getName().getLocalPart().equals(this.name);

            }

            if (!nameMatches) {
                this.active = this.count > 0;
                return this.count > 0;
            }

            if (this.count > 0) {
                this.count++;
                this.active = true;
                return this.active;
            }

            for (Attribute attribute : this.attributes) {
                switch (this.matchingType) {
                case RegexMatch:
                    boolean elementMatches = false;
                    Pattern namePattern = Pattern.compile(attribute.getName().getLocalPart());
                    Pattern valuePattern = Pattern.compile(attribute.getValue());

                    Iterator<Attribute> elementAttributeIter = element.getAttributes();
                    while (elementAttributeIter.hasNext()) {
                        Attribute elementAttribute = elementAttributeIter.next();

                        Matcher nameMatcher = namePattern.matcher(elementAttribute.getName().getLocalPart());
                        Matcher valueMatcher = valuePattern.matcher(elementAttribute.getValue());

                        if (nameMatcher.matches() && valueMatcher.matches()) {
                            elementMatches = true;
                            break;
                        }
                    }

                    if (!elementMatches) {
                        this.active = false;
                        return this.active;
                    }

                    break;
                default:
                    Attribute foundAttribute = element.getAttributeByName(attribute.getName());
                    if (foundAttribute == null || !attribute.getValue().equals(foundAttribute.getValue())) {
                        this.active = false;
                        return this.active;
                    }
                }
            }

            this.count++;
            this.active = true;
            return this.active;
        } else if (event.isEndElement()) {
            EndElement element = (EndElement) event;

            boolean nameMatches = false;

            switch (this.matchingType) {
            case RegexMatch:
                nameMatches = Pattern.compile(this.name).matcher(element.getName().getLocalPart()).matches();

            default:
                nameMatches = element.getName().getLocalPart().equals(this.name);
            }

            if (nameMatches && this.count > 0) {
                this.count--;
                if (this.count == 0) {
                    this.active = true;
                    return this.active;
                }
            }
        }

        return this.count > 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.navigation.Navigator#isActive()
     */
    public boolean isActive() {
        return this.active;
    }

    public MatchingType getMatchingType() {
        return this.matchingType;
    }

    public void setMatchingType(MatchingType matchingType) {
        this.matchingType = matchingType;
    }
}
