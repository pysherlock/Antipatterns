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

import java.io.OutputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.pipeline.component.AbstractPipelineComponent;
import org.apache.cocoon.pipeline.component.Finisher;
import org.apache.cocoon.stax.StAXConsumer;
import org.apache.cocoon.stax.StAXProducer;

/**
 * Standard implementation of the cocoon {@link Finisher} for StAX pipelines. This pipeline pushes
 * all events through the pipeline and write them UTF-8 encoded to an {@link XMLEventWriter} created
 * from an {@link OutputStream} inserted in the {@link Finisher#setOutputStream(OutputStream)}.
 */
public class XMLSerializer extends AbstractPipelineComponent implements StAXConsumer, Finisher {

    private StAXProducer parent;
    private XMLEventWriter writer;

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.component.Finisher#setOutputStream(java.io.OutputStream)
     */
    public void setOutputStream(OutputStream outputStream) {
        try {
            this.writer = XMLOutputFactory.newInstance().createXMLEventWriter(outputStream, "UTF-8");
        } catch (XMLStreamException e) {
            throw new SetupException("Error during setup an XMLEventWriter on the outputStream", e);
        } catch (FactoryConfigurationError e) {
            throw new SetupException("Error during setup the XMLOutputFactory for creating an XMLEventWriter", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.StAXConsumer#initiatePullProcessing()
     */
    public void initiatePullProcessing() {
        try {
            while (this.parent.hasNext()) {
                this.writer.add(this.parent.nextEvent());
            }
        } catch (XMLStreamException e) {
            throw new ProcessingException("Error during writing output elements.", e);
        }

        try {
            this.writer.flush();
        } catch (XMLStreamException e) {
            throw new ProcessingException("Finally cant flush the output stream.", e);
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

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.component.Finisher#getContentType()
     */
    public String getContentType() {
        return null;
    }
}
