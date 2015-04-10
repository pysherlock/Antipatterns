/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.sax.builder;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.pipeline.component.Starter;
import org.apache.cocoon.sax.SAXPipelineComponent;
import org.apache.cocoon.sax.component.XMLGenerator;
import org.apache.cocoon.xml.sax.SAXBuffer;
import org.w3c.dom.Node;

/**
 * 
 *
 * @version $Id: LinkedSAXPipelineStarterBuilderImpl.java 1044302 2010-12-10 11:36:22Z simonetripodi $
 */
final class LinkedSAXPipelineStarterBuilderImpl implements LinkedSAXPipelineStarterBuilder {

    private final Pipeline<SAXPipelineComponent> pipeline;

    /**
     * 
     * @param pipeline
     */
    public LinkedSAXPipelineStarterBuilderImpl(Pipeline<SAXPipelineComponent> pipeline) {
        this.pipeline = pipeline;
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder setByteArrayGenerator(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("Parameter 'bytes' must be not null");
        }
        return this.setStarter(new XMLGenerator(bytes));
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder setByteArrayGenerator(byte[] bytes, String encoding) {
        if (bytes == null) {
            throw new IllegalArgumentException("Parameter 'bytes' must be not null");
        }
        if (encoding == null) {
            throw new IllegalArgumentException("Parameter 'encoding' must be not null");
        }
        return this.setStarter(new XMLGenerator(bytes, encoding));
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder setFileGenerator(File file) {
        if (file == null) {
            throw new IllegalArgumentException("Parameter 'file' must be not null");
        }
        return this.setStarter(new XMLGenerator(file));
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder setInputStreamGenerator(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Parameter 'inputStream' must be not null");
        }
        return this.setStarter(new XMLGenerator(inputStream));
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder setNodeGenerator(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Parameter 'node' must be not null");
        }
        return this.setStarter(new XMLGenerator(node));
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder setSAXBufferGenerator(SAXBuffer saxBuffer) {
        if (saxBuffer == null) {
            throw new IllegalArgumentException("Parameter 'saxBuffer' must be not null");
        }
        return this.setStarter(new XMLGenerator(saxBuffer));
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder setStringGenerator(String xmlString) {
        if (xmlString == null) {
            throw new IllegalArgumentException("Parameter 'xmlString' must be not null");
        }
        return this.setStarter(new XMLGenerator(xmlString));
    }

    /**
     * {@inheritDoc}
     */
    public LinkedSAXPipelineComponentBuilder setURLGenerator(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("Parameter 'url' must be not null");
        }
        return this.setStarter(new XMLGenerator(url));
    }

    /**
     * {@inheritDoc}
     */
    public <SPC extends SAXPipelineComponent> LinkedSAXPipelineComponentBuilder setStarter(SPC starter) {
        if (starter == null) {
            throw new IllegalArgumentException("Parameter 'starter' must be not null");
        }
        if (!(starter instanceof Starter)) {
            throw new IllegalArgumentException("Parameter 'starter' must be org.apache.cocoon.pipeline.component.Starter instance");
        }
        this.pipeline.addComponent(starter);
        return new LinkedSAXPipelineComponentBuilderImpl(this.pipeline);
    }

}
