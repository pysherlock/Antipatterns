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
package org.apache.cocoon.sax.component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.sax.AbstractSAXTransformer;
import org.apache.cocoon.sax.SAXConsumer;
import org.apache.cocoon.sax.util.XMLUtils;
import org.apache.cocoon.sax.xpointer.ParseException;
import org.apache.cocoon.sax.xpointer.XPointer;
import org.apache.cocoon.sax.xpointer.XPointerContext;
import org.apache.cocoon.sax.xpointer.XPointerFrameworkParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * $Id: XIncludeTransformer.java 907774 2010-02-08 19:49:59Z simonetripodi $
 */
public final class XIncludeTransformer extends AbstractSAXTransformer implements SAXConsumer {

    private static final String XINCLUDE_NAMESPACE_URI = "http://www.w3.org/2001/XInclude";

    private static final String XINCLUDE_INCLUDE = "include";

    private static final String XINCLUDE_FALLBACK = "fallback";

    private static final String XINCLUDE_HREF = "href";

    private static final String XINCLUDE_XPOINTER = "xpointer";

    private static final String XINCLUDE_PARSE = "parse";

    private static final String XINCLUDE_ENCODING = "encoding";

    private static final String DEFAULT_CHARSET = "UTF-8";

    private static final String XINCLUDE_ACCEPT = "accept";

    private static final String XINCLUDE_ACCEPT_LANGUAGE = "accept-language";

    private static final String XINCLUDE_PARSE_XML = "xml";

    private static final String XINCLUDE_PARSE_TEXT = "text";

    private static final String UNKNOWN_LOCATION = "unknow location";

    private static final String HTTP_ACCEPT = "Accept";

    private static final String HTTP_ACCEPT_LANGUAGE = "Accept-Language";

    private static final String CHARSET = "charset=";

    private static final String BASE_URL = "baseUrl";

    private static final String EMPTY = "";

    private final Log logger = LogFactory.getLog(this.getClass());

    /**
     * The nesting level of xi:include elements that have been encountered.
     */
    private int xIncludeElementLevel = 0;

    /**
     * The nesting level of fallback that should be used
     */
    private int useFallbackLevel = 0;

    /**
     * The nesting level of xi:fallback elements that have been encountered.
     */
    private int fallbackElementLevel;

    /**
     * The nesting level of document events.
     */
    private int documentLevel = 0;

    /**
     * Locator of the current stream, stored here so that it can be restored after
     * another document send its content to the consumer.
     */
    private Locator locator;

    /**
     * The base URL from which document will be included, if XInclude element
     * points to a relative path.
     */
    private URL baseUrl;

    /**
     * Keep a map of namespaces prefix in the source document to pass it
     * to the XPointerContext for correct namespace identification.
     */
    private final Map<String, String> namespaces = new HashMap<String, String>();

    public XIncludeTransformer() {
        // default empty constructor - used in the sitemap
    }

    /**
     *
     * @param baseUrl
     */
    public XIncludeTransformer(URL baseUrl) {
        this.setBaseUrl(baseUrl);
    }

    @Override
    public void setConfiguration(Map<String, ? extends Object> configuration) {
        this.setBaseUrl((URL) configuration.get(BASE_URL));
    }

    /**
     *
     * @param baseUrl
     */
    public void setBaseUrl(URL baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Eventually previous errors don't reset local variables status, so
     * every time a new consumer is set, local variables should be re-initialized
     */
    @Override
    protected void setSAXConsumer(final SAXConsumer xmlConsumer) {
        super.setSAXConsumer(xmlConsumer);
        this.xIncludeElementLevel = 0;
        this.fallbackElementLevel = 0;
        this.useFallbackLevel = 0;
    }

    /**
     * Determine whether the pipe is currently in a state where contents
     * should be evaluated, i.e. xi:include elements should be resolved
     * and elements in other namespaces should be copied through. Will
     * return false for fallback contents within a successful xi:include,
     * and true for contents outside any xi:include or within an xi:fallback
     * for an unsuccessful xi:include.
     */
    private boolean isEvaluatingContent() {
        return this.xIncludeElementLevel == 0 ||
            this.fallbackElementLevel > 0
                    && this.fallbackElementLevel == this.useFallbackLevel;
    }

    /**
     *
     * @return
     */
    private String getLocation() {
        if (this.locator == null) {
            return UNKNOWN_LOCATION;
        } else {
            return this.locator.getSystemId()
                + ":"
                + this.locator.getColumnNumber()
                + ":"
                + this.locator.getLineNumber();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startDocument() throws SAXException {
        if (this.documentLevel++ == 0) {
            this.getSAXConsumer().startDocument();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDocument() throws SAXException {
        if (--this.documentLevel == 0) {
            this.getSAXConsumer().endDocument();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(final String uri, final String localName, final String name, final Attributes atts)
            throws SAXException {
        if (XINCLUDE_NAMESPACE_URI.equals(uri)) {
            // Handle xi:include:
            if (XINCLUDE_INCLUDE.equals(localName)) {
                // Process the include, unless in an ignored fallback:
                if (this.isEvaluatingContent()) {
                    String href = atts.getValue(EMPTY, XINCLUDE_HREF);

                    String parse = atts.getValue(EMPTY, XINCLUDE_PARSE);
                    // Default for @parse is "xml"
                    if (parse == null) {
                        parse = XINCLUDE_PARSE_XML;
                    }

                    String xpointer = atts.getValue(EMPTY, XINCLUDE_XPOINTER);
                    String encoding = atts.getValue(EMPTY, XINCLUDE_ENCODING);

                    String accept = atts.getValue(EMPTY, XINCLUDE_ACCEPT);
                    String acceptLanguage = atts.getValue(EMPTY, XINCLUDE_ACCEPT_LANGUAGE);

                    this.processXIncludeElement(href, parse, xpointer, encoding, accept, acceptLanguage);
                }
                this.xIncludeElementLevel++;
            } else if (XINCLUDE_FALLBACK.equals(localName)) {
                // Handle xi:fallback
                this.fallbackElementLevel++;
            } else {
                // Unknown element:
                throw new SAXException("Unknown XInclude element " + localName + " at " + this.getLocation());
            }
        } else if (this.isEvaluatingContent()) {
            // Copy other elements through when appropriate:
            this.getSAXConsumer().startElement(uri, localName, name, atts);
        }
    }

    /**
     *
     * @param href
     * @param parse
     * @param xpointer
     * @param encoding
     * @param accept
     * @param acceptLanguage
     * @throws SAXException
     */
    private void processXIncludeElement(String href, final String parse, String xpointer, String encoding,
            final String accept, final String acceptLanguage) throws SAXException {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Processing XInclude element: href="
                 + href
                 + ", parse="
                 + parse
                 + ", xpointer="
                 + xpointer
                 + ", encoding="
                 + encoding
                 + ", accept="
                 + accept
                 + ", acceptLanguage="
                 + acceptLanguage);
        }

        int fragmentIdentifierPos = href.indexOf('#');
        if (fragmentIdentifierPos != -1) {
            if (this.logger.isWarnEnabled()) {
                this.logger.warn("Fragment identifer found in 'href' attribute: "
                    + href
                    + "\nFragment identifiers are forbidden by the XInclude specification. "
                    + "They are still handled by XIncludeTransformer for backward "
                    + "compatibility, but their use is deprecated and will be prohibited "
                    + "in a future release. Use the 'xpointer' attribute instead.");
            }
            if (xpointer == null) {
                xpointer = href.substring(fragmentIdentifierPos + 1);
            }
            href = href.substring(0, fragmentIdentifierPos);
        }

        // An empty or absent href is a reference to the current document -- this can be different than the current base
        if (!isNotEmpty(href)) {
            throw new SAXException("XIncludeTransformer: encountered empty href (= href pointing to the current document).");
        }
        URL source = this.createSource(href);
        URLConnection urlConnection = null;

        try {
            urlConnection = source.openConnection();
        } catch (IOException ioe) {
            this.useFallbackLevel++;
            this.logger.error("Error including document: " + source, ioe);
        }

        if (urlConnection != null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Parse type=" + parse);
            }

            if (XINCLUDE_PARSE_XML.equals(parse)) {
                /* sends Accept and Accept-Language */
                if (urlConnection instanceof HttpURLConnection) {
                    HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;

                    if (isNotEmpty(accept)) {
                        httpURLConnection.setRequestProperty(HTTP_ACCEPT, accept);
                    }

                    if (isNotEmpty(acceptLanguage)) {
                        httpURLConnection.setRequestProperty(HTTP_ACCEPT_LANGUAGE, acceptLanguage);
                    }
                }

                if (xpointer != null && xpointer.length() > 0) {
                    try {
                        // create the context
                        XPointerContext xPointerContext = new XPointerContext(xpointer, this);
                        for (Entry<String, String> namespace : this.namespaces.entrySet()) {
                            xPointerContext.addPrefix(namespace.getKey(), namespace.getValue());
                        }

                        // initialize the XPointer handler by parsing the xpointer
                        XPointer xPointer = XPointerFrameworkParser.parse(xpointer);
                        xPointer.setLog(this.logger);

                        // setup components
                        xPointer.setUp(xPointerContext);
                        xPointer.setDocumentLocator(this.locator);

                        // go!
                        XMLUtils.toSax(urlConnection, xPointer);
                    } catch (ParseException e) {
                        // this exception is thrown in case of an invalid xpointer expression
                        this.useFallbackLevel++;
                        if (this.logger.isErrorEnabled()) {
                            this.logger.error("Error parsing XPointer expression, will try to use fallback.", e);
                        }
                    } catch (IOException e) {
                        this.useFallbackLevel++;
                        if (this.logger.isErrorEnabled()) {
                            this.logger.error("Error processing an xInclude, will try to use fallback.", e);
                        }
                    }
                } else {
                    // just parse the whole document and stream it
                    XMLUtils.toSax(urlConnection, this);
                }
            } else if (XINCLUDE_PARSE_TEXT.equals(parse)) {
                if (xpointer != null) {
                    throw new SAXException("xpointer attribute must not be present when parse='text': "
                         + this.getLocation());
                }

                // content type will be string like "text/xml; charset=UTF-8" or "text/xml"
                String rawContentType = urlConnection.getContentType();

                if (encoding == null) {
                    // text/xml and application/xml offer only one optional parameter
                    int index = rawContentType != null ? rawContentType.indexOf(';') : -1;

                    String charset = null;
                    if (index != -1) {
                        // this should be something like "charset=UTF-8", but we want to
                        // strip it down to just "UTF-8"
                        charset = rawContentType.substring(index + 1).trim();
                        if (charset.startsWith(CHARSET)) {
                            charset = charset.substring(CHARSET.length()).trim();
                            // strip quotes, if present
                            if (charset.charAt(0) == '"'
                                && charset.charAt(charset.length() - 1) == '"'
                                || charset.charAt(0) == '\''
                                    && charset.charAt(charset.length() - 1)
                                        == '\'') {
                                encoding =
                                    charset.substring(1, charset.length() - 1);
                            }
                        } else {
                            encoding = DEFAULT_CHARSET;
                        }
                    } else {
                        encoding = DEFAULT_CHARSET;
                    }
                }

                InputStream is = null;
                InputStreamReader isr = null;
                Reader reader = null;

                try {
                    is = urlConnection.getInputStream();
                    isr = new InputStreamReader(is, encoding);
                    reader = new BufferedReader(isr);

                    int read;
                    char ary[] = new char[1024 * 4];
                    while ((read = reader.read(ary)) != -1) {
                        this.getSAXConsumer().characters(ary, 0, read);
                    }
                } catch (IOException e) {
                    this.useFallbackLevel++;
                    if (this.logger.isErrorEnabled()) {
                        this.logger.error("Error including text: ", e);
                    }
                } finally {
                    closeQuietly(reader);
                    closeQuietly(isr);
                    closeQuietly(is);
                }
            } else {
                throw new SAXException("Found 'parse' attribute with unknown value "
                     + parse
                     + " at "
                     + this.getLocation());
            }
        }
    }

    /**
     *
     * @param sourceAtt
     * @return
     */
    private URL createSource(final String sourceAtt) {
        try {
            URL source = null;
            if (sourceAtt.contains(":")) {
                source = new URL(sourceAtt);
            } else {
                source = new URL(this.baseUrl, sourceAtt);
            }
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Including source: " + source);
            }

            return source;
        } catch (MalformedURLException e) {
            String message = "Can't parse URL " + sourceAtt;
            if (this.logger.isErrorEnabled()) {
                this.logger.error(message, e);
            }
            throw new ProcessingException(message, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(final String uri, final String localName, final String name) throws SAXException {
        // Handle elements in xinclude namespace:
        if (XINCLUDE_NAMESPACE_URI.equals(uri)) {
            // Handle xi:include:
            if (XINCLUDE_INCLUDE.equals(localName)) {
                this.xIncludeElementLevel--;
                if (this.useFallbackLevel > this.xIncludeElementLevel) {
                    this.useFallbackLevel = this.xIncludeElementLevel;
                }
            } else if (XINCLUDE_FALLBACK.equals(localName)) {
                // Handle xi:fallback:
                this.fallbackElementLevel--;
            }
        } else if (this.isEvaluatingContent()) {
            // Copy other elements through when appropriate:
            this.getSAXConsumer().endElement(uri, localName, name);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
        if (this.isEvaluatingContent()) {
            // removed xinclude namespace from result document
            if (!uri.equals(XINCLUDE_NAMESPACE_URI)) {
                this.getSAXConsumer().startPrefixMapping(prefix, uri);
            }
            this.namespaces.put(prefix, uri);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endPrefixMapping(final String prefix) throws SAXException {
        if (this.isEvaluatingContent()) {
            // remove xinclude namespace prefix from result document
            if (!XINCLUDE_NAMESPACE_URI.equals(this.namespaces.get(prefix))) {
                this.getSAXConsumer().endPrefixMapping(prefix);
            }
            this.namespaces.remove(prefix);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startCDATA() throws SAXException {
        if (this.isEvaluatingContent()) {
            this.getSAXConsumer().startCDATA();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endCDATA() throws SAXException {
        if (this.isEvaluatingContent()) {
            this.getSAXConsumer().startCDATA();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startDTD(final String name, final String publicId, final String systemId) throws SAXException {
        // ignoring DTD
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDTD() throws SAXException {
        // ignoring DTD
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startEntity(final String name) throws SAXException {
        if (this.isEvaluatingContent()) {
            this.getSAXConsumer().startEntity(name);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endEntity(final String name) throws SAXException {
        if (this.isEvaluatingContent()) {
            this.getSAXConsumer().endEntity(name);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        if (this.isEvaluatingContent()) {
            this.getSAXConsumer().characters(ch, start, length);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
        if (this.isEvaluatingContent()) {
            this.getSAXConsumer().ignorableWhitespace(ch, start, length);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void comment(final char[] ch, final int start, final int length) throws SAXException {
        // skip comments
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processingInstruction(final String target, final String data) throws SAXException {
        if (this.isEvaluatingContent()) {
            this.getSAXConsumer().processingInstruction(target, data);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDocumentLocator(final Locator locator) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("setDocumentLocator called "
                    + locator.getSystemId());
        }

        this.locator = locator;
        this.getSAXConsumer().setDocumentLocator(locator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void skippedEntity(final String name) throws SAXException {
        if (this.isEvaluatingContent()) {
            this.getSAXConsumer().skippedEntity(name);
        }
    }

    private static boolean isNotEmpty(final String string) {
        return string != null && string.length() > 0;
    }

    private static void closeQuietly(final Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    private static void closeQuietly(final InputStream input) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }
}
