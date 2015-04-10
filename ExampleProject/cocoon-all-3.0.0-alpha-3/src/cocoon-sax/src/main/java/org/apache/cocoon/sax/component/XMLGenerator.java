/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.sax.component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.apache.cocoon.pipeline.PipelineException;
import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.caching.TimestampCacheKey;
import org.apache.cocoon.pipeline.component.CachingPipelineComponent;
import org.apache.cocoon.pipeline.component.Starter;
import org.apache.cocoon.pipeline.util.StringRepresentation;
import org.apache.cocoon.pipeline.util.URLConnectionUtils;
import org.apache.cocoon.sax.AbstractSAXGenerator;
import org.apache.cocoon.sax.AbstractSAXProducer;
import org.apache.cocoon.sax.util.XMLUtils;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.cocoon.xml.sax.SAXBuffer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * General purpose SAX generator that produces SAX events from different sources.
 */
public class XMLGenerator extends AbstractSAXGenerator implements CachingPipelineComponent {

    private Starter generator;

    private final Log logger = LogFactory.getLog(this.getClass());

    public XMLGenerator() {
        this((URL) null);
    }

    public XMLGenerator(byte[] bytes) {
        this.generator = new ByteArrayGenerator(bytes);
    }

    public XMLGenerator(byte[] bytes, String encoding) {
        this.generator = new ByteArrayGenerator(bytes, encoding);
    }

    public XMLGenerator(File file) {
        this.generator = new FileGenerator(file);
    }

    public XMLGenerator(InputStream inputStream) {
        this.generator = new InputStreamGenerator(inputStream);
    }

    public XMLGenerator(Node node) {
        this.generator = new NodeGenerator(node);
    }

    public XMLGenerator(SAXBuffer saxBuffer) {
        this.generator = new SAXBufferGenerator(saxBuffer);
    }

    public XMLGenerator(String xmlString) {
        this.generator = new StringGenerator(xmlString);
    }

    public XMLGenerator(URL url) {
        this.generator = new URLGenerator(url);
    }

    public CacheKey constructCacheKey() {
        if (this.generator instanceof CachingPipelineComponent) {
            return ((CachingPipelineComponent) this.generator).constructCacheKey();
        }

        return null;
    }

    public void execute() {
        this.generator.execute();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sax.AbstractSAXProducer#setConfiguration(java.util.Map)
     */
    @Override
    public void setConfiguration(Map<String, ? extends Object> configuration) {
        ((URLGenerator) this.generator).setSource((URL) configuration.get("source"));
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "internalGenerator=" + this.generator);
    }

    private class ByteArrayGenerator extends AbstractSAXGenerator {

        private final byte[] bytes;
        private final String encoding;

        public ByteArrayGenerator(byte[] bytes) {
            this(bytes, null);
        }

        public ByteArrayGenerator(byte[] bytes, String encoding) {
            if (bytes == null) {
                throw new SetupException("A byte array has to be passed.");
            }

            this.bytes = bytes;
            this.encoding = encoding;
        }

        public void execute() {
            try {
                if (XMLGenerator.this.logger.isDebugEnabled()) {
                    XMLGenerator.this.logger.debug("Using a byte array as source to produce SAX events.");
                }

                if (this.encoding == null) {
                    XMLUtils.toSax(new ByteArrayInputStream(this.bytes), XMLGenerator.this.getSAXConsumer());
                } else {
                    XMLUtils.toSax(new String(this.bytes, this.encoding), XMLGenerator.this.getSAXConsumer());
                }
            } catch (PipelineException e) {
                throw e;
            } catch (UnsupportedEncodingException e) {
                throw new ProcessingException("The encoding " + this.encoding + " is not supported.", e);
            } catch (Exception e) {
                throw new ProcessingException("Can't parse byte array " + this.bytes, e);
            }
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "bytes=" + this.bytes, "encoding=" + this.encoding);
        }
    }

    private class FileGenerator extends AbstractSAXGenerator {

        private final File file;

        public FileGenerator(File file) {
            if (file == null) {
                throw new SetupException("A file has to be passed.");
            }

            this.file = file;
        }

        public void execute() {
            try {
                if (XMLGenerator.this.logger.isDebugEnabled()) {
                    XMLGenerator.this.logger.debug("Using file " + this.file.getAbsolutePath()
                            + " as source to produce SAX events.");
                }

                XMLUtils.toSax(new FileInputStream(this.file), XMLGenerator.this.getSAXConsumer());
            } catch (PipelineException e) {
                throw e;
            } catch (Exception e) {
                throw new ProcessingException("Can't read or parse file " + this.file.getAbsolutePath(), e);
            }
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "file=" + this.file);
        }
    }

    private class InputStreamGenerator extends AbstractSAXGenerator {

        private final InputStream inputStream;

        public InputStreamGenerator(InputStream inputStream) {
            super();

            if (inputStream == null) {
                throw new SetupException("An input stream has to be passed.");
            }

            this.inputStream = inputStream;
        }

        public void execute() {
            try {
                if (XMLGenerator.this.logger.isDebugEnabled()) {
                    XMLGenerator.this.logger.debug("Using input stream " + this.inputStream
                            + " as source to produce SAX events.");
                }

                XMLUtils.toSax(this.inputStream, XMLGenerator.this.getSAXConsumer());
            } catch (PipelineException e) {
                throw e;

            } catch (Exception e) {
                throw new ProcessingException("Can't read or parse file " + this.inputStream, e);
            }
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "inputStream=" + this.inputStream);
        }
    }

    private class NodeGenerator extends AbstractSAXGenerator {

        private final Node node;

        public NodeGenerator(Node document) {
            if (document == null) {
                throw new SetupException("A DOM document has to be passed.");
            }

            this.node = document;
        }

        public void execute() {
            if (XMLGenerator.this.logger.isDebugEnabled()) {
                XMLGenerator.this.logger.debug("Using a DOM node to produce SAX events.");
            }

            DOMStreamer streamer = new DOMStreamer(XMLGenerator.this.getSAXConsumer());
            try {
                streamer.stream(this.node);
            } catch (SAXException e) {
                throw new SetupException("Can't stream DOM node + " + this.node);
            }
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "node=" + this.node);
        }
    }

    private class SAXBufferGenerator extends AbstractSAXGenerator {

        private final SAXBuffer saxBuffer;

        public SAXBufferGenerator(SAXBuffer saxBuffer) {
            super();

            if (saxBuffer == null) {
                throw new SetupException("A SAXBuffer has to be passed.");
            }

            this.saxBuffer = saxBuffer;
        }

        public void execute() {
            if (XMLGenerator.this.logger.isDebugEnabled()) {
                XMLGenerator.this.logger.debug("Using a SAXBuffer to produce SAX events.");
            }

            try {
                this.saxBuffer.toSAX(XMLGenerator.this.getSAXConsumer());
            } catch (SAXException e) {
                throw new ProcessingException("Can't stream " + this + " into the content handler.", e);
            }
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "saxBuffer=" + this.saxBuffer);
        }
    }

    private class StringGenerator extends AbstractSAXProducer implements Starter {

        private String xmlString;

        public StringGenerator(String xmlString) {
            super();
            if (xmlString == null) {
                throw new SetupException("An XML string has to be passed.");
            }

            this.xmlString = xmlString;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.apache.cocoon.pipeline.component.Starter#execute()
         */
        public void execute() {
            try {
                if (XMLGenerator.this.logger.isDebugEnabled()) {
                    XMLGenerator.this.logger.debug("Using a string to produce SAX events.");
                }

                XMLUtils.toSax(new ByteArrayInputStream(this.xmlString.getBytes()), XMLGenerator.this.getSAXConsumer());
            } catch (PipelineException e) {
                throw e;
            } catch (Exception e) {
                throw new ProcessingException("Can't parse the XML string.", e);
            }
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "xmlString=" + this.xmlString);
        }
    }

    private class URLGenerator extends AbstractSAXGenerator implements CachingPipelineComponent {

        private URL source;

        public URLGenerator(URL source) {
            super();
            this.source = source;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.apache.cocoon.pipeline.component.CachingPipelineComponent#constructCacheKey()
         */
        public CacheKey constructCacheKey() {
            if (this.source == null) {
                throw new SetupException(this.getClass().getSimpleName() + " has no source.");
            }

            URLConnection connection = null;
            try {
                connection = this.source.openConnection();
                TimestampCacheKey timestampCacheKey = new TimestampCacheKey(this.source, connection.getLastModified());
                return timestampCacheKey;
            } catch (IOException e) {
                XMLGenerator.this.logger
                        .error("Can't construct cache key. Error while connecting to " + this.source, e);
            } finally {
                URLConnectionUtils.closeQuietly(connection);
            }

            return null;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.apache.cocoon.pipeline.component.Starter#execute()
         */
        public void execute() {
            if (this.source == null) {
                throw new ProcessingException(this.getClass().getSimpleName() + " has no source.");
            }

            if (XMLGenerator.this.logger.isDebugEnabled()) {
                XMLGenerator.this.logger.debug("Using the URL " + this.source + " to produce SAX events.");
            }

            try {
                XMLUtils.toSax(this.source.openConnection(), XMLGenerator.this.getSAXConsumer());
            } catch (IOException e) {
                throw new ProcessingException("Can't open connection to " + this.source, e);
            }
        }

        public void setSource(URL source) {
            this.source = source;
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "source=" + this.source);
        }
    }
}
