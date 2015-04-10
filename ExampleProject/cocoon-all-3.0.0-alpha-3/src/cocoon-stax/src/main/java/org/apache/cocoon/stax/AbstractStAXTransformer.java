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
package org.apache.cocoon.stax;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Abstract transformer could/should be used for each StAXTransformer. This class implements the
 * default behavior of the {@link StAXConsumer} and the {@link StAXConsumer} interfaces.
 * 
 * Further more the {@link AbstractStAXTransformer} contains the queuing for StAX-events. This
 * transformer abstracts the entire handling of {@link #nextEvent()} and {@link #peek()} behind a
 * template method {@link #produceEvents()} which saves all produced events in a buffer and handles
 * {@link #peek()}, {@link #nextEvent()} and {@link #hasNext()}.
 */
public abstract class AbstractStAXTransformer extends AbstractStAXProducer implements StAXConsumer {

    private StAXProducer parent;
    private Queue<XMLEvent> queue = new LinkedList<XMLEvent>();

    /**
     * {@inheritDoc}
     * 
     * @see StAXConsumer#initiatePullProcessing()
     */
    public final void initiatePullProcessing() {
        this.getConsumer().initiatePullProcessing();
    }

    /**
     * {@inheritDoc}
     * 
     * @see StAXConsumer#setParent(StAXProducer)
     */
    public final void setParent(StAXProducer parent) {
        this.parent = parent;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws XMLStreamException Error which could occure during producing events.
     * 
     * @see StAXProducer#hasNext()
     */
    public final boolean hasNext() throws XMLStreamException {
        if (this.queue.isEmpty()) {
            this.produceEvents();
        }

        return !this.queue.isEmpty();
    }

    /**
     * {@inheritDoc}
     * 
     * @see StAXProducer#nextEvent()
     */
    public final XMLEvent nextEvent() {
        return this.queue.poll();
    }

    /**
     * {@inheritDoc}
     * 
     * @see StAXProducer#peek()
     */
    public final XMLEvent peek() {
        return this.queue.peek();
    }

    /**
     * Add a specific {@link XMLEvent} to an internal {@link Queue}.
     * 
     * @param event which should be added to the queue.
     */
    protected final void addEventToQueue(XMLEvent event) {
        this.queue.add(event);
    }

    /**
     * This {@link StAXProducer} is the parent of the module overwriting this
     * {@link AbstractStAXTransformer}. It could be used in the {@link #produceEvents()} method to
     * pull events from the parent or ask if it still has some.
     */
    protected final StAXProducer getParent() {
        return this.parent;
    }

    /**
     * Adds a {@link Collection} of {@link XMLEvent}s to an internal {@link Queue}.
     * 
     * @param events are a {@link Collection} of {@link XMLEvent}s which should be added to an
     *            internal {@link Queue}.
     */
    protected final void addAllEventsToQueue(Collection<? extends XMLEvent> events) {
        this.queue.addAll(events);
    }

    /**
     * Checks and returns if the internal {@link Queue} of {@link XMLEvent}s is empty.
     * 
     * @return checks and returns if the internal {@link Queue} of {@link XMLEvent}s is empty.
     */
    protected final boolean isQueueEmpty() {
        return this.queue.isEmpty();
    }

    /**
     * Template method which has to produce at least one {@link XMLEvent} and should add this to the
     * internal {@link Queue} with the {@link #add(XMLEvent)} and {@link #addAll(Collection)}
     * methods. If no {@link XMLEvent} is added by this method to the internal {@link Queue} an
     * exception is thrown.
     * 
     * @throws XMLStreamException thrown if the {@link StAXProducer#nextEvent()} or the
     *             {@link StAXProducer#peek()} method, called in the {@link #produceEvents()} method
     *             throw any exceptions they have to be forwarded to the end of the pipeline.
     */
    protected abstract void produceEvents() throws XMLStreamException;
}
