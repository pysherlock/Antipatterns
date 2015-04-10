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

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.pipeline.component.AbstractPipelineComponent;
import org.apache.cocoon.pipeline.component.Consumer;
import org.apache.cocoon.sax.SAXConsumer;
import org.apache.cocoon.sax.SAXProducer;
import org.apache.cocoon.stax.StAXConsumer;
import org.apache.cocoon.stax.StAXProducer;
import org.apache.cocoon.stax.converter.util.XMLEventToContentHandler;
import org.apache.cocoon.stax.converter.util.XMLFilterImplEx;

/**
 * A transformer which transforms between a StAX-only pipeline and a SAX-only pipeline. The first
 * part of the pipeline have to be out of StAX-Components, where the second part have to be out of
 * SAX-components.
 */
public class StAXToSAXPipelineAdapter extends AbstractPipelineComponent implements StAXConsumer, SAXProducer {

    private XMLEventToContentHandler staxToSaxHandler;
    private StAXProducer parent;

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.component.Producer#setConsumer(org.apache.cocoon.pipeline.component.Consumer)
     */
    public void setConsumer(Consumer consumer) {
        if (!(consumer instanceof SAXConsumer)) {
            throw new SetupException("SAXProducer requires an SAXConsumer.");
        }

        XMLFilterImplEx filter = new XMLFilterImplEx();
        filter.setContentHandler((SAXConsumer) consumer);
        filter.setLexicalHandler((SAXConsumer) consumer);
        this.staxToSaxHandler = new XMLEventToContentHandler(filter);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.StAXConsumer#initiatePullProcessing()
     */
    public void initiatePullProcessing() {
        try {
            while (this.parent.hasNext()) {
                this.staxToSaxHandler.convertEvent(this.parent.nextEvent());
            }
        } catch (XMLStreamException e) {
            throw new ProcessingException("Error during retrieving StAXEvents.", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.StAXConsumer#setParent(org.apache.cocoon.stax.StAXProducer)
     */
    public void setParent(StAXProducer parent) {
        this.parent = parent;
    }
}
