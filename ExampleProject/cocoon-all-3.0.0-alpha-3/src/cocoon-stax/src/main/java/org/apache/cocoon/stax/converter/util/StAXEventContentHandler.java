/*
 * Copyright (c) 2004, Christian Niles, Unit12
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *      *   Redistributions of source code must retain the above copyright
 *          notice, this list of conditions and the following disclaimer.
 * 
 *      *   Redistributions in binary form must reproduce the above copyright
 *          notice, this list of conditions and the following disclaimer in the
 *          documentation and/or other materials provided with the distribution.
 * 
 *      *   Neither the name of Christian Niles, Unit12, nor the names of its
 *          contributors may be used to endorse or promote products derived from
 *          this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.apache.cocoon.stax.converter.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.Location;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.util.XMLEventConsumer;

import org.apache.cocoon.sax.SAXConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX ContentHandler that writes events to a StAX {@link XMLEventConsumer}.
 * 
 * @author Christian Niles
 * @version $Revision: 1.5 $
 * 
 * @version from stax-utils stable version stax-utils-20070216
 * @original javanet.staxutils.StAXEventContentHandler, javanet.staxutils.StAXContentHandler
 * @modified true (added SAXConsumer and made one class out of StAXEventContentHandler and
 *           StAXContentHandler)
 */
public class StAXEventContentHandler extends DefaultHandler implements SAXConsumer {

    /**
     * Whether the parser is currently within a CDATA section.
     */
    private boolean isCDATA;

    /**
     * Buffer containing text read within the current CDATA section.
     */
    private StringBuffer CDATABuffer;

    /**
     * Stack used to store declared namespaces.
     */
    private SimpleNamespaceContext namespaces;

    /**
     * The SAX {@link Locator}provided to the handler.
     */
    private Locator docLocator;

    /**
     * The STAX {@link XMLReporter}registered to receive notifications.
     */
    private XMLReporter reporter;

    /** The consumer to which events will be written. */
    private XMLEventConsumer consumer;

    /** The factory used to construct events. */
    private XMLEventFactory eventFactory;

    /**
     * A stack of {@link List}s, each containing {@link Namespace}events constructed from a
     * {@link StartElement}event. It is necessary to keep these namespaces so we can report them to
     * the {@link EndElement}event.
     */
    @SuppressWarnings("unchecked")
    private List namespaceStack = new ArrayList();

    /**
     * Constructs a default instance with a default event factory. You must set the
     * {@link XMLEventConsumer}via the {@link #setEventConsumer(XMLEventConsumer)}method.
     */
    public StAXEventContentHandler() {

        this.eventFactory = XMLEventFactory.newInstance();

    }

    /**
     * Constructs an instance that writes events to the provided XMLEventConsumer. Events will be
     * constructed from a default XMLEventFactory instance.
     * 
     * @param consumer The {@link XMLEventConsumer}to which events will be written.
     */
    public StAXEventContentHandler(XMLEventConsumer consumer) {

        this.consumer = consumer;
        this.eventFactory = XMLEventFactory.newInstance();

    }

    /**
     * Constructs an instance that writes events constructed with the provided XMLEventFactory to
     * the provided XMLEventConsumer
     * 
     * @param consumer The {@link XMLEventConsumer} to which events will be written.
     * @param factory The {@link XMLEventFactory} used to construct events. If <code>null</code>, a
     *            default instance will be constructed.
     */
    public StAXEventContentHandler(XMLEventConsumer consumer, XMLEventFactory factory) {
        this.consumer = consumer;
        if (factory != null) {
            this.eventFactory = factory;
        } else {
            this.eventFactory = XMLEventFactory.newInstance();
        }
    }

    /**
     * Returns a reference to the {@link XMLEventConsumer} to which events will be written.
     * 
     * @return The {@link XMLEventConsumer} to which events will be written.
     */
    public XMLEventConsumer getEventConsumer() {
        return this.consumer;
    }

    /**
     * Sets the {@link XMLEventConsumer} to which events are written.
     * 
     * @param consumer The {@link XMLEventConsumer} to which events will be written.
     */
    public void setEventConsumer(XMLEventConsumer consumer) {
        this.consumer = consumer;
    }

    /**
     * Returns a reference to the {@link XMLEventFactory} used to construct events.
     * 
     * @return The {@link XMLEventFactory} used to construct events.
     */
    public XMLEventFactory getEventFactory() {
        return this.eventFactory;
    }

    /**
     * Sets the {@link XMLEventFactory} used to create events.
     * 
     * @param factory The {@link XMLEventFactory} used to create events.
     */
    public void setEventFactory(XMLEventFactory factory) {
        this.eventFactory = factory;
    }

    @Override
    public void startDocument() throws SAXException {
        this.namespaces = new SimpleNamespaceContext();

        // clear the namespaces in case we ended in error before.
        this.namespaceStack.clear();

        this.eventFactory.setLocation(this.getCurrentLocation());
        try {
            this.consumer.add(this.eventFactory.createStartDocument());
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        this.eventFactory.setLocation(this.getCurrentLocation());

        try {
            this.consumer.add(this.eventFactory.createEndDocument());
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }

        this.namespaces = null;

        // clear the namespaces
        this.namespaceStack.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.namespaces = null;
        // set document location
        this.eventFactory.setLocation(this.getCurrentLocation());

        // create attribute and namespace events
        Collection[] events = { null, null };
        this.createStartEvents(attributes, events);

        // save a reference to the namespace collection so we can use them
        // again
        // in the end element
        this.namespaceStack.add(events[0]);

        try {
            String[] qname = { null, null };
            this.parseQName(qName, qname);

            this.consumer.add(this.eventFactory.createStartElement(qname[0], uri, qname[1], events[1].iterator(),
                    events[0].iterator()));
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        } finally {
            super.startElement(uri, localName, qName, attributes);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        this.namespaces = null;
        super.endElement(uri, localName, qName);

        this.eventFactory.setLocation(this.getCurrentLocation());

        // parse name
        String[] qname = { null, null };
        this.parseQName(qName, qname);

        // get namespaces
        Collection nsList = (Collection) this.namespaceStack.remove(this.namespaceStack.size() - 1);
        Iterator nsIter = nsList.iterator();

        try {
            this.consumer.add(this.eventFactory.createEndElement(qname[0], uri, qname[1], nsIter));
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        this.eventFactory.setLocation(this.getCurrentLocation());

        try {
            this.consumer.add(this.eventFactory.createComment(new String(ch, start, length)));
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);

        if (this.isCDATA) {
            this.CDATABuffer.append(ch, start, length);
        }

        try {
            if (!this.isCDATA) {
                this.eventFactory.setLocation(this.getCurrentLocation());
                this.consumer.add(this.eventFactory.createCharacters(new String(ch, start, length)));
            }
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        super.ignorableWhitespace(ch, start, length);
        this.characters(ch, start, length);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        super.processingInstruction(target, data);
        try {
            this.consumer.add(this.eventFactory.createProcessingInstruction(target, data));
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    public void endCDATA() throws SAXException {
        this.eventFactory.setLocation(this.getCurrentLocation());
        try {
            this.consumer.add(this.eventFactory.createCData(this.CDATABuffer.toString()));
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }

        this.isCDATA = false;
        this.CDATABuffer.setLength(0);
    }

    /**
     * Creates the {@link Namespace}and {@link Attribute}events associated with a
     * {@link StartElement}.
     * 
     * @param attributes The SAX attributes object.
     * @param events An array used to return the two collections of {@link Namespace}and
     *            {@link Attribute}events. The namespaces will be placed at <code>events[0]</code>
     *            and the attributes as <code>events[1]</code>.
     */
    @SuppressWarnings("unchecked")
    protected void createStartEvents(Attributes attributes, Collection[] events) {
        Map nsMap = null;
        List attrs = null;

        // create namespaces
        if (this.namespaces != null) {
            Iterator prefixes = this.namespaces.getDeclaredPrefixes();
            while (prefixes.hasNext()) {
                String prefix = (String) prefixes.next();
                String uri = this.namespaces.getNamespaceURI(prefix);

                Namespace ns = this.createNamespace(prefix, uri);
                if (nsMap == null) {
                    nsMap = new HashMap();
                }
                nsMap.put(prefix, ns);
            }
        }

        // create attributes
        String[] qname = { null, null };
        for (int i = 0, s = attributes.getLength(); i < s; i++) {

            this.parseQName(attributes.getQName(i), qname);

            String attrPrefix = qname[0];
            String attrLocal = qname[1];

            String attrQName = attributes.getQName(i);
            String attrValue = attributes.getValue(i);
            String attrURI = attributes.getURI(i);

            if ("xmlns".equals(attrQName) || "xmlns".equals(attrPrefix)) {

                // namespace declaration disguised as an attribute. If the
                // namespace has already been declared, skip it, otherwise
                // write it as an namespace

                if (!nsMap.containsKey(attrPrefix)) {
                    Namespace ns = this.createNamespace(attrPrefix, attrValue);
                    if (nsMap == null) {
                        nsMap = new HashMap();
                    }
                    nsMap.put(attrPrefix, ns);
                }

            } else {
                Attribute attribute;
                if (attrPrefix.length() > 0) {
                    attribute = this.eventFactory.createAttribute(attrPrefix, attrURI, attrLocal, attrValue);
                } else {
                    attribute = this.eventFactory.createAttribute(attrLocal, attrValue);
                }

                if (attrs == null) {
                    attrs = new ArrayList();
                }
                attrs.add(attribute);
            }
        }

        events[0] = nsMap == null ? Collections.EMPTY_LIST : nsMap.values();
        events[1] = attrs == null ? Collections.EMPTY_LIST : attrs;
    }

    protected Namespace createNamespace(String prefix, String uri) {
        if (prefix == null || prefix.length() == 0) {
            return this.eventFactory.createNamespace(uri);
        } else {
            return this.eventFactory.createNamespace(prefix, uri);
        }
    }

    /**
     * Sets the {@link XMLReporter}to which warning and error messages will be sent.
     * 
     * @param reporter The {@link XMLReporter}to notify of errors.
     */
    public void setXMLReporter(XMLReporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.docLocator = locator;
    }

    /**
     * Calculates the STAX {@link Location}from the SAX {@link Locator} registered with this
     * handler. If no {@link Locator}was provided, then this method will return <code>null</code>.
     */
    public Location getCurrentLocation() {
        if (this.docLocator != null) {
            return new SAXLocation(this.docLocator);
        } else {
            return null;
        }
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        this.reportException("ERROR", e);
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        this.reportException("FATAL", e);
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        this.reportException("WARNING", e);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (prefix == null) {
            prefix = "";
        } else if (prefix.equals("xml")) {
            return;
        }

        if (this.namespaces == null) {
            this.namespaces = new SimpleNamespaceContext();
        }
        this.namespaces.setPrefix(prefix, uri);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void startCDATA() throws SAXException {
        this.isCDATA = true;
        if (this.CDATABuffer == null) {
            this.CDATABuffer = new StringBuffer();
        } else {
            this.CDATABuffer.setLength(0);
        }
    }

    public void endDTD() throws SAXException {
    }

    public void endEntity(String name) throws SAXException {
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
    }

    public void startEntity(String name) throws SAXException {
    }

    /**
     * Used to report a {@link SAXException}to the {@link XMLReporter} registered with this handler.
     */
    protected void reportException(String type, SAXException e) throws SAXException {
        if (this.reporter != null) {
            try {
                this.reporter.report(e.getMessage(), type, e, this.getCurrentLocation());
            } catch (XMLStreamException e1) {
                throw new SAXException(e1);
            }
        }
    }

    /**
     * Parses an XML qualified name, and places the resulting prefix and local name in the provided
     * String array.
     * 
     * @param qName The qualified name to parse.
     * @param results An array where parse results will be placed. The prefix will be placed at
     *            <code>results[0]</code>, and the local part at <code>results[1]</code>
     */
    private void parseQName(String qName, String[] results) {
        String prefix, local;
        int idx = qName.indexOf(':');
        if (idx >= 0) {
            prefix = qName.substring(0, idx);
            local = qName.substring(idx + 1);
        } else {
            prefix = "";
            local = qName;
        }

        results[0] = prefix;
        results[1] = local;
    }

    /**
     * {@Link Location}implementation used to expose details from a SAX {@link Locator}.
     * 
     * @author christian
     * @version $Revision: 1.3 $
     */
    private class SAXLocation implements Location {

        private int lineNumber;
        private int columnNumber;
        private String publicId;
        private String systemId;

        private SAXLocation(Locator locator) {
            this.lineNumber = locator.getLineNumber();
            this.columnNumber = locator.getColumnNumber();
            this.publicId = locator.getPublicId();
            this.systemId = locator.getSystemId();
        }

        public int getLineNumber() {
            return this.lineNumber;
        }

        public int getColumnNumber() {
            return this.columnNumber;
        }

        public int getCharacterOffset() {
            return -1;
        }

        public String getPublicId() {
            return this.publicId;
        }

        public String getSystemId() {
            return this.systemId;
        }
    }

    public void finish() {
        // do nothing
    }

    public void setConfiguration(Map<String, ? extends Object> configuration) {
        // do nothing
    }

    public void setup(Map<String, Object> parameters) {
        // do nothing
    }
}
