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
package org.apache.cocoon.stax.stress.src;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.cocoon.pipeline.component.Starter;
import org.apache.cocoon.stax.AbstractStAXProducer;

/**
 * This Starter implementation generates events of a simple document with a specified root element
 * followed by a number of specified elements.
 */
public class GeneratingStarter extends AbstractStAXProducer implements Starter {

    private XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private String rootElement;
    private String repeatingElementName;
    private int repetitionCount;
    private int currentElementNumber = 0;
    private GenerationState state = GenerationState.START_DOCUMENT;

    /**
     * Creates a new {@link GeneratingStarter} {@link Starter} that generates events of a simple
     * document with a specified root element followed by a number of specified elements.
     * 
     * @param rootElement
     * @param repeatingElementName
     * @param repetitionCount
     */
    public GeneratingStarter(String rootElement, String repeatingElementName, int repetitionCount) {
        this.rootElement = rootElement;
        this.repeatingElementName = repeatingElementName;
        this.repetitionCount = repetitionCount;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.component.Starter#execute()
     */
    public void execute() {
        this.getConsumer().initiatePullProcessing();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.StAXProducer#hasNext()
     */
    public boolean hasNext() {
        return !this.state.equals(GenerationState.FINISHED);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.StAXProducer#nextEvent()
     */
    public XMLEvent nextEvent() throws XMLStreamException {
        return this.createNextElement(false);
    }

    /**
     * Creates the next element.
     * 
     * @param isPeeking Indicates if the element is peeked or consumed.
     * @return The next element.
     * @throws XMLStreamException Is thrown if an error occurs.
     */
    private XMLEvent createNextElement(boolean isPeeking) throws XMLStreamException {
        switch (this.state) {
        case START_REPEATING_ELEMENT:
            if (!isPeeking) {
                this.state = GenerationState.END_REPEATING_ELEMENT;
            }
            return this.eventFactory.createStartElement("", "", this.repeatingElementName);
        case END_REPEATING_ELEMENT:
            if (!isPeeking) {
                this.currentElementNumber++;
                if (this.currentElementNumber == this.repetitionCount) {
                    this.state = GenerationState.END_ROOT;
                } else {
                    this.state = GenerationState.START_REPEATING_ELEMENT;
                }
            }
            return this.eventFactory.createEndElement("", "", this.repeatingElementName);
        case START_DOCUMENT:
            if (!isPeeking) {
                this.state = GenerationState.START_ROOT;
            }
            return this.eventFactory.createStartDocument();
        case START_ROOT:
            if (!isPeeking) {
                if (this.currentElementNumber == this.repetitionCount) {
                    this.state = GenerationState.END_ROOT;
                } else {
                    this.state = GenerationState.START_REPEATING_ELEMENT;
                }
            }
            return this.eventFactory.createStartElement("", "", this.rootElement);
        case END_DOCUMENT:
            if (!isPeeking) {
                this.state = GenerationState.FINISHED;
            }
            return this.eventFactory.createEndDocument();
        case END_ROOT:
            if (!isPeeking) {
                this.state = GenerationState.END_DOCUMENT;
            }
            return this.eventFactory.createEndElement("", "", this.rootElement);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.StAXProducer#peek()
     */
    public XMLEvent peek() throws XMLStreamException {
        return this.createNextElement(true);
    }
}
