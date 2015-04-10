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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.apache.cocoon.sax.SAXConsumer;

/**
 * $Id: XPointerContext.java 892809 2009-12-21 13:14:59Z reinhard $
 */
public final class XPointerContext implements NamespaceContext {

    private final static String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";

    private final static String XMLNS_NAMESPACE = "http://www.w3.org/2000/xmlns/";

    private final static String XSL_NAMESPACE = "http://www.w3.org/1999/XSL/Transform";

    private final Map<String, String> namespaces = new HashMap<String, String>();

    private final String xPointer;

    private final SAXConsumer saxConsumer;

    public XPointerContext(final String xPointer, final SAXConsumer saxConsumer) {
        this.xPointer = xPointer;
        this.saxConsumer = saxConsumer;
    }

    public String getXPointer() {
        return this.xPointer;
    }

    public SAXConsumer getSaxConsumer() {
        return this.saxConsumer;
    }

    public Map<String, String> getNamespaces() {
        return this.namespaces;
    }

    public void addPrefix(final String prefix, final String namespaceURI) {
        if (XML_NAMESPACE.equals(namespaceURI)
                || XMLNS_NAMESPACE.equals(namespaceURI)
                || XSL_NAMESPACE.equals(namespaceURI)) {
            return;
        }

        this.namespaces.put(prefix, namespaceURI);
    }

    // This method isn't necessary for XPath processing either.
    public String getPrefix(String namespaceURI) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    // This method isn't necessary for XPath processing either.
    public Iterator getPrefixes(final String namespaceURI) {
        throw new UnsupportedOperationException();
    }

    // This method isn't necessary for XPath processing either.
    public String getNamespaceURI(final String prefix) {
        return this.namespaces.get(prefix);
    }
}
