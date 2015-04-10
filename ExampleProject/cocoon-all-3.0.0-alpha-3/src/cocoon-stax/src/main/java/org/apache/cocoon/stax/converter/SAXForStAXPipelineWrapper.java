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
package org.apache.cocoon.stax.converter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.pipeline.component.Consumer;
import org.apache.cocoon.pipeline.component.PipelineComponent;
import org.apache.cocoon.pipeline.component.Producer;
import org.apache.cocoon.sax.SAXConsumer;
import org.apache.cocoon.sax.SAXProducer;
import org.apache.cocoon.stax.AbstractStAXTransformer;
import org.apache.cocoon.stax.StAXConsumer;
import org.apache.cocoon.stax.StAXProducer;
import org.apache.cocoon.stax.converter.util.StAXEventContentHandler;
import org.apache.cocoon.stax.converter.util.XMLEventToContentHandler;
import org.apache.cocoon.stax.converter.util.XMLFilterImplEx;

/**
 * This class could be seen as the default implementation to use SAX components in StAX pipelines.
 * <p>
 * If an {@link XMLEvent} is pulled from the {@link SAXForStAXPipelineWrapper} by an
 * {@link StAXConsumer} the {@link SAXForStAXPipelineWrapper} retrieves an {@link XMLEvent} from its
 * {@link StAXConsumer} and translate it to an SAXEvent. Each event produced during calling the
 * SAX-component are transformed to {@link XMLEvent}s and are stored internally. If the
 * SAX-component produced at least one {@link XMLEvent} all produced {@link XMLEvent}s are returned.
 * Otherwise the next event is pulled from the {@link StAXProducer} and the process is repeated.
 */
public class SAXForStAXPipelineWrapper extends AbstractStAXTransformer implements XMLEventConsumer {

    private SAXProducer saxTransformer;
    private XMLEventToContentHandler staxToSaxHandler;
    private StAXEventContentHandler saxToStaxHandler;

    /**
     * Constructs an Wrapper around an SAXTransformer and made it available to add it to an
     * {@link Pipeline} only containing StAX- {@link PipelineComponent}s.
     * 
     * @param The Transformer which should be wrapped. As an SAXTransformer it have to implement the
     *            {@link SAXProducer} and the {@link SAXConsumer} interface. Since no transformer
     *            interface exists at the moment at the cocoon-sax project this way is chosen. But
     *            this may be changed later.
     */
    public SAXForStAXPipelineWrapper(SAXProducer saxTransformer) {
        // check if component is the right one
        if (!(saxTransformer instanceof SAXConsumer)) {
            throw new SetupException("SAX component does not fulfill all preconditions.");
        }

        this.saxTransformer = saxTransformer;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Producer#setConsumer(Consumer)
     */
    @Override
    public void setConsumer(Consumer consumer) {
        // call the super method to set the consumer internal
        super.setConsumer(consumer);

        // setup first component to stax part
        XMLFilterImplEx filter = new XMLFilterImplEx();
        filter.setContentHandler((SAXConsumer) this.saxTransformer);
        filter.setLexicalHandler((SAXConsumer) this.saxTransformer);
        this.staxToSaxHandler = new XMLEventToContentHandler(filter);

        // setup last component to stax part
        this.saxToStaxHandler = new StAXEventContentHandler(this);
        this.saxTransformer.setConsumer(this.saxToStaxHandler);
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.stream.util.XMLEventConsumer#add(javax.xml.stream.events.XMLEvent)
     */
    public void add(XMLEvent event) throws XMLStreamException {
        this.addEventToQueue(event);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.AbstractStAXTransformer#produceEvents()
     */
    @Override
    protected void produceEvents() throws XMLStreamException {
        while (this.isQueueEmpty() && this.getParent().hasNext()) {
            this.staxToSaxHandler.convertEvent(this.getParent().nextEvent());
        }
    }
}
