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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.sax.AbstractSAXTransformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Transformer that can be plugged into a pipeline to print as XML the SAX
 * events which passes through this transformer in a readable form to a file
 * or sysout.
 *
 * This class is not thread-safe!
 */
public final class LogAsXMLTransformer extends AbstractSAXTransformer {

    private static final String LOG_FILE = "logfile";

    /**
     * This class log.
     */
    private static final Log LOG = LogFactory.getLog(LogAsXMLTransformer.class);

    private final transient XMLSerializer xmlSerializer =
            XMLSerializer.createXMLSerializer();

    private transient OutputStream outputStream;

    public LogAsXMLTransformer() {
        this(null);
    }

    public LogAsXMLTransformer(final File logFile) {
        super();

        if (logFile == null) {
            this.init(System.out);
        } else {
            try {
                this.init(new FileOutputStream(logFile));
            } catch (FileNotFoundException e) {
                throw new SetupException(
                        "Impossible to create an XML log file '"
                        + logFile
                        + "'", e);
            }
        }
    }

    private void init(final OutputStream outputStream) {
        this.xmlSerializer.setup(new HashMap<String, Object>());
        this.xmlSerializer.setOutputStream(outputStream);
        this.xmlSerializer.setIndent(true);
        this.outputStream = outputStream;
    }

    @Override
    public void setConfiguration(
            final Map<String, ? extends Object> configuration) {

        this.setup((Map<String, Object>) configuration);
    }

    @Override
    public void setup(final Map<String, Object> parameters) {
        if (parameters == null || !parameters.containsKey(LOG_FILE)) {
            return;
        }

        String logFileName = String.valueOf(parameters.get(LOG_FILE));
        if (logFileName != null) {
            File logFile = new File(logFileName);
            OutputStream os;
            try {
                os = new FileOutputStream(logFile);
                this.init(os);
            } catch (FileNotFoundException e) {
                throw new SetupException("Impossible to open XML log file '"
                        + logFile
                        + "'", e);
            }
        }
    }

    @Override
    public void finish() {
        this.xmlSerializer.finish();
        if (this.outputStream != null) {
            try {
                if (System.out.equals(this.outputStream)) {
                    this.outputStream.flush();
                } else {
                    this.outputStream.close();
                }
            } catch (IOException e) {
                LOG.debug("Impossible to close the log writer", e);
            }
        }
    }

    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {

        this.xmlSerializer.characters(ch, start, length);
        super.characters(ch, start, length);
    }

    @Override
    public void comment(final char[] ch, final int start, final int length)
            throws SAXException {

        this.xmlSerializer.comment(ch, start, length);
        super.comment(ch, start, length);
    }

    @Override
    public void endCDATA()
            throws SAXException {

        this.xmlSerializer.endCDATA();
        super.endCDATA();
    }

    @Override
    public void endDocument()
            throws SAXException {

        this.xmlSerializer.endDocument();
        super.endDocument();
    }

    @Override
    public void endDTD()
            throws SAXException {

        this.xmlSerializer.endDTD();
        super.endDTD();
    }

    @Override
    public void endElement(final String uri, final String localName,
            final String name)
            throws SAXException {

        this.xmlSerializer.endElement(uri, localName, name);
        super.endElement(uri, localName, name);
    }

    @Override
    public void endEntity(final String name)
            throws SAXException {

        this.xmlSerializer.endEntity(name);
        super.endEntity(name);
    }

    @Override
    public void endPrefixMapping(final String prefix)
            throws SAXException {

        this.xmlSerializer.endPrefixMapping(prefix);
        super.endPrefixMapping(prefix);
    }

    @Override
    public void ignorableWhitespace(final char[] ch,
            final int start, final int length)
            throws SAXException {

        this.xmlSerializer.ignorableWhitespace(ch, start, length);
        super.ignorableWhitespace(ch, start, length);
    }

    @Override
    public void processingInstruction(final String target, final String data)
            throws SAXException {

        this.xmlSerializer.processingInstruction(target, data);
        super.processingInstruction(target, data);
    }

    @Override
    public void setDocumentLocator(final Locator locator) {

        this.xmlSerializer.setDocumentLocator(locator);
        super.setDocumentLocator(locator);
    }

    @Override
    public void skippedEntity(final String name)
            throws SAXException {

        this.xmlSerializer.skippedEntity(name);
        super.skippedEntity(name);
    }

    @Override
    public void startCDATA()
            throws SAXException {

        this.xmlSerializer.startCDATA();
        super.startCDATA();
    }

    @Override
    public void startDocument()
            throws SAXException {

        this.xmlSerializer.startDocument();
        super.startDocument();
    }

    @Override
    public void startDTD(final String name, final String publicId,
            final String systemId)
            throws SAXException {

        this.xmlSerializer.startDTD(name, publicId, systemId);
        super.startDTD(name, publicId, systemId);
    }

    @Override
    public void startElement(final String uri, final String localName,
            final String name, final Attributes atts)
            throws SAXException {

        this.xmlSerializer.startElement(uri, localName, name, atts);
        super.startElement(uri, localName, name, atts);
    }

    @Override
    public void startEntity(final String name)
            throws SAXException {

        this.xmlSerializer.startEntity(name);
        super.startEntity(name);
    }

    @Override
    public void startPrefixMapping(final String prefix, final String uri)
            throws SAXException {

        this.xmlSerializer.startPrefixMapping(prefix, uri);
        super.startPrefixMapping(prefix, uri);
    }
}
