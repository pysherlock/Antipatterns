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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.cocoon.pipeline.component.Producer;

/**
 * Extension of the cocoon {@link Producer} element for all StAX specific components. This interface
 * extends the base {@link Producer} with three methods directly taken from the
 * {@link XMLEventReader} interface which are required to implement an StAX component.
 */
public interface StAXProducer extends Producer, StAXPipelineComponent {

    /**
     * Check the next XMLEvent without reading it from the stream. Returns null if the stream is at
     * EOF or has no more XMLEvents. A call to peek() will be equal to the next return of next().
     * 
     * @see XMLEvent
     */
    public XMLEvent peek() throws XMLStreamException;

    /**
     * Check if there are more events. Returns true if there are more events and false otherwise.
     * 
     * @return true if the event reader has more events, false otherwise
     */
    public boolean hasNext() throws XMLStreamException;

    /**
     * Get the next XMLEvent
     * 
     * @see XMLEvent
     */
    public XMLEvent nextEvent() throws XMLStreamException;
}
