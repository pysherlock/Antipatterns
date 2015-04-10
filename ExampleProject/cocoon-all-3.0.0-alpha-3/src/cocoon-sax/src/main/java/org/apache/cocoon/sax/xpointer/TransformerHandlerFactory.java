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
package org.apache.cocoon.sax.xpointer;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.ContentHandler;

/**
 * $Id: TransformerHandlerFactory.java 892809 2009-12-21 13:14:59Z reinhard $
 */
final class TransformerHandlerFactory {

    /**
     * A generic transformer factory to parse XSLTs.
     */
    private static final SAXTransformerFactory TRAX_FACTORY = (SAXTransformerFactory) TransformerFactory.newInstance();

    private static final String XMLNS_PATTERN = "xmlns:%s=\"%s\"";

    private static final String XSLT_PATTERN =
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" %s>"
        + "<xsl:template match=\"%s\">"
        + "<xsl:copy-of select=\".\"/>"
        + "</xsl:template></xsl:stylesheet>";

    private static final Map<String, Templates> TEMPLATES = new HashMap<String, Templates>();

    /**
     * This class can't be instantiated
     */
    private TransformerHandlerFactory() {
        // do nothing
    }

    public static TransformerHandler borrowHandler(final Map<String, String> namespaces,
            final String expression,
            final ContentHandler delegate) throws TransformerConfigurationException {
        Templates templates = null;
        if (TEMPLATES.containsKey(expression)) {
            templates = TEMPLATES.get(expression);
        } else {
            StringBuilder builder = new StringBuilder();
            for (Entry<String, String> namespace : namespaces.entrySet()) {
                builder.append('\n');
                builder.append(String.format(XMLNS_PATTERN, namespace.getKey(), namespace.getValue()));
            }
            String xmlNamespaces = builder.toString();

            String xslt = String.format(XSLT_PATTERN, xmlNamespaces, expression);
            Source source = new StreamSource(new StringReader(xslt));
            templates = TRAX_FACTORY.newTemplates(source);
            TEMPLATES.put(expression, templates);
        }

        final SAXResult result = new SAXResult();
        result.setHandler(delegate);

        TransformerHandler transformerHandler = TRAX_FACTORY.newTransformerHandler(templates);
        transformerHandler.setResult(result);
        return transformerHandler;
    }
}
