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
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.sax.AbstractSAXTransformer;
import org.apache.cocoon.xml.sax.SAXBuffer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Transformer that can be plugged into a pipeline to print the SAX events which
 * passes through this transformer in a readable form to a file or sysout.
 *
 * This class is not thread-safe!
 */
public final class LogTransformer extends AbstractSAXTransformer {

    private static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd'T'hh:mm:ss");

    private static final String LINE_SEPARATOR = System.getProperty(
            "line.separator", "\n");

    private static final String LOG_FILE = "logfile";

    private static final String APPEND = "append";

    private static final String DATE_PATTERN = "append";

    /**
     * This class log.
     */
    private final Log log = LogFactory.getLog(this.getClass());

    /**
     * The file where log will be stored.
     */
    private FileWriter logWriter;

    /**
     * The date format used to prefix log messages.
     */
    private SimpleDateFormat dateFormat;

    /**
     * Anonimous constructor, used in sitemap declaration.
     */
    public LogTransformer() {
        this.logWriter = null;
        this.dateFormat = ISO_FORMAT;
    }

    public LogTransformer(File logFile, boolean append)
            throws IOException {
        this(logFile, append, ISO_FORMAT);
    }

    public LogTransformer(File logFile, boolean append, String datePattern)
            throws IOException {
        this(logFile, append, new SimpleDateFormat(datePattern));
    }

    public LogTransformer(File logFile, boolean append,
            SimpleDateFormat dateFormat)
            throws IOException {

        this.logWriter = new FileWriter(logFile, append);
        this.dateFormat = dateFormat;
    }

    @Override
    public void setConfiguration(Map<String, ? extends Object> configuration) {
        this.setup((Map<String, Object>) configuration);
    }

    @Override
    public void setup(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return;
        }

        Object logFileString = parameters.get(LOG_FILE);
        Object appendString = parameters.get(APPEND);

        if (logFileString != null) {
            File logFile = new File(String.valueOf(logFileString));
            boolean append = false;
            if (appendString != null) {
                append = Boolean.parseBoolean(String.valueOf(appendString));
            }
            try {
                this.logWriter = new FileWriter(logFile, append);
            } catch (IOException e) {
                throw new SetupException("Impossible to open log file '"
                        + logFile
                        + "' (append="
                        + append
                        + ")", e);
            }
        }

        Object datePatternString = parameters.get(DATE_PATTERN);
        if (datePatternString != null) {
            this.dateFormat = new SimpleDateFormat(String.valueOf(
                    datePatternString));
        }
    }

    @Override
    public void finish() {
        if (this.logWriter != null) {
            try {
                if (System.out.equals(this.logWriter)) {
                    this.logWriter.flush();
                } else {
                    this.logWriter.close();
                }
            } catch (IOException e) {
                this.log.debug("Impossible to close the log writer", e);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        this.log("characters", new String(ch, start, length));
        super.characters(ch, start, length);
    }

    @Override
    public void comment(char[] ch, int start, int length)
            throws SAXException {
        this.log("comment", new String(ch, start, length));
        super.comment(ch, start, length);
    }

    @Override
    public void endCDATA()
            throws SAXException {
        this.log("endCDATA", null);
        super.endCDATA();
    }

    @Override
    public void endDocument()
            throws SAXException {
        this.log("endDocument", null);
        super.endDocument();
    }

    @Override
    public void endDTD()
            throws SAXException {
        this.log("endDTD", null);
        super.endDTD();
    }

    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        this.log("endElement", "uri="
                + uri
                + ", local="
                + localName
                + ", name="
                + name);
        super.endElement(uri, localName, name);
    }

    @Override
    public void endEntity(String name)
            throws SAXException {
        this.log("endEntity", "name=" + name);
        super.endEntity(name);
    }

    @Override
    public void endPrefixMapping(String prefix)
            throws SAXException {
        this.log("endPrefixMapping", "prefix=" + prefix);
        super.endPrefixMapping(prefix);
    }

    @Override
    public SAXBuffer endSAXRecording()
            throws SAXException {
        this.log("endSAXRecording", null);
        return super.endSAXRecording();
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        this.log("ignorableWhitespace", new String(ch, start, length));
        super.ignorableWhitespace(ch, start, length);
    }

    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
        this.log("processingInstruction", "target="
                + target
                + ", data="
                + data);
        super.processingInstruction(target, data);
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.log("setDocumentLocator", locator != null ? "systemid="
                + locator.getSystemId()
                + ", publicid="
                + locator.getPublicId() : "(locator is null)");
        super.setDocumentLocator(locator);
    }

    @Override
    public void skippedEntity(String name)
            throws SAXException {
        this.log("skippedEntity", "name=" + name);
        super.skippedEntity(name);
    }

    @Override
    public void startCDATA()
            throws SAXException {
        this.log("startCDATA", null);
        super.startCDATA();
    }

    @Override
    public void startDocument()
            throws SAXException {
        this.log("startDocument", null);
        super.startDocument();
    }

    @Override
    public void startDTD(String name, String publicId, String systemId)
            throws SAXException {
        this.log("startDTD", "name="
                + name
                + ", publicId="
                + publicId
                + ", systemId="
                + systemId);
        super.startDTD(name, publicId, systemId);
    }

    @Override
    public void startElement(String uri, String localName, String name,
            Attributes atts)
            throws SAXException {
        this.log("startElement", "uri="
                + uri
                + ", localName="
                + localName
                + ", name="
                + name);
        for (int i = 0; i < atts.getLength(); i++) {
            this.log("            ", (i + 1)
                    + ". uri="
                    + atts.getURI(i)
                    + ", local="
                    + atts.getLocalName(i)
                    + ", qname="
                    + atts.getQName(i)
                    + ", type="
                    + atts.getType(i)
                    + ", value="
                    + atts.getValue(i));
        }
        super.startElement(uri, localName, name, atts);
    }

    @Override
    public void startEntity(String name)
            throws SAXException {
        this.log("startEntity", "name=" + name);
        super.startEntity(name);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        this.log("startPrefixMapping", "prefix="
                + prefix
                + ", uri="
                + uri);
        super.startPrefixMapping(prefix, uri);
    }

    @Override
    public void startSAXRecording()
            throws SAXException {
        this.log("startSAXRecording", null);
        super.startSAXRecording();
    }

    /**
     * Report to logfile.
     *
     * @param location
     * @param description
     */
    private void log(String location, String description) {
        final StringBuilder logEntry = new StringBuilder();
        logEntry.append(this.dateFormat.format(new Date()));
        logEntry.append(" - [");
        logEntry.append(location);
        logEntry.append("] ");
        if (description != null) {
            logEntry.append(description);
        }
        logEntry.append(LINE_SEPARATOR);
        final String text = logEntry.toString();

        try {
            if (this.logWriter != null) {
                this.logWriter.write(text, 0, text.length());
                this.logWriter.flush();
            } else {
                System.out.print(text);
            }
        } catch (IOException ioe) {
            this.log.error("LogTransformer.log", ioe);
        }
    }
}
