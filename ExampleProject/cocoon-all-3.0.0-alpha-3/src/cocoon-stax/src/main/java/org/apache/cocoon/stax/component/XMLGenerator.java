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
package org.apache.cocoon.stax.component;

import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.pipeline.component.Starter;
import org.apache.cocoon.stax.AbstractStAXProducer;
import org.apache.cocoon.stax.StAXProducer;

/**
 * General element generator for a StAX pipeline directly taking all elements from an internal
 * {@link XMLEventReader} created from an {@link InputStream} or directly from an {@link URL}.
 */
public class XMLGenerator extends AbstractStAXProducer implements Starter {

    private XMLEventReader reader;

    /**
     * Creating an {@link XMLGenerator} with an {@link XMLEventReader} from an {@link InputStream}.
     * 
     * @param inputStream from which a {@link XMLEventReader} is produced read from during the
     *            {@link XMLGenerator#execute()} method call.
     * @throws SetupException if any error occurred during the creation of the
     *             {@link XMLEventReader}.
     */
    public XMLGenerator(InputStream inputStream) {
        try {
            this.reader = XMLInputFactory.newInstance().createXMLEventReader(inputStream);
        } catch (XMLStreamException e) {
            throw new SetupException("Error during setup an XMLEventReader on the inputStream", e);
        } catch (FactoryConfigurationError e) {
            throw new SetupException("Error during setup the XMLInputFactory for creating an XMLEventReader", e);
        }
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
        return this.reader.hasNext();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.StAXProducer#nextEvent()
     */
    public XMLEvent nextEvent() throws XMLStreamException {
        return this.reader.nextEvent();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.StAXProducer#peek()
     */
    public XMLEvent peek() throws XMLStreamException {
        return this.reader.peek();
    }
}
