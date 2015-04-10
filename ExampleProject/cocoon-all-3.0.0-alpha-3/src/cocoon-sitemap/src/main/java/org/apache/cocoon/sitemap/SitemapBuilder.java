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
package org.apache.cocoon.sitemap;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.cocoon.sitemap.node.SitemapNode;
import org.apache.cocoon.sitemap.node.SitemapNodeFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class SitemapBuilder {

    private SitemapNodeFactory sitemapNodeFactory;

    public SitemapNode build(URL sitemap) {
        if (sitemap == null) {
            throw new NullPointerException("A valid sitemap URL has to be passed.");
        }

        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setNamespaceAware(true);
        // saxParserFactory.setSchema(SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema").newSchema());

        SitemapHandler sitemapHandler = new SitemapHandler();
        try {
            SAXParser saxParser = saxParserFactory.newSAXParser();

            InputStream inputStream = sitemap.openStream();
            saxParser.parse(inputStream, sitemapHandler);
            inputStream.close();
        } catch (Exception e) {
            throw new SitemapBuilderException("Can't build sitemap.", e);
        }

        return sitemapHandler.getSitemap();
    }

    public void setSitemapNodeFactory(SitemapNodeFactory sitemapNodeFactory) {
        this.sitemapNodeFactory = sitemapNodeFactory;
    }

    protected SitemapNode createSitemapNode(String localName, Map<String, String> parameters) {
        return SitemapBuilder.this.sitemapNodeFactory.createNode(localName, parameters);
    }

    private static class SitemapBuilderException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public SitemapBuilderException(String msg, Throwable t) {
            super(msg, t);
        }
    }

    private class SitemapHandler extends DefaultHandler {

        private SitemapNode currentNode;
        private SitemapNode sitemap;

        @Override
        public void endElement(String uri, String localName, String name) {
            if (this.currentNode == null) {
                throw new IllegalStateException("Received closing '" + localName + "' but there was no node to close.");
            }

            this.currentNode = this.currentNode.getParent();
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        public SitemapNode getSitemap() {
            return this.sitemap;
        }

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) {
            if (this.currentNode == null) {
                if (localName.equals("sitemap")) {
                    this.sitemap = SitemapBuilder.this.createSitemapNode(localName, null);
                    this.currentNode = this.sitemap;
                    return;
                }
                throw new IllegalStateException("Expected 'sitemap' as first element, but received '" + localName + "'");
            }

            Map<String, String> parameters = new HashMap<String, String>();
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                parameters.put(attributes.getQName(i), attributes.getValue(i));
            }

            SitemapNode node = SitemapBuilder.this.createSitemapNode(localName, parameters);
            this.currentNode.addChild(node);
            this.currentNode = node;
        }
    }
}
