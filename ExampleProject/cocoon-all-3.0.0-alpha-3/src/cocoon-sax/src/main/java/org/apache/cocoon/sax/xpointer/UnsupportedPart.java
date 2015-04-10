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

import java.io.IOException;

import org.xml.sax.SAXException;

/**
 * $Id: UnsupportedPart.java 892809 2009-12-21 13:14:59Z reinhard $
 */
public final class UnsupportedPart extends AbstractPointerPart {

    private String schemeName;

    public UnsupportedPart(final String schemeName) {
        this.schemeName = schemeName;
    }

    public void setUp(final XPointerContext xpointerContext) throws SAXException, IOException {
        throw new SAXException("Scheme '"
                + this.schemeName
                + "' not supported by this XPointer implementation, as used in the fragment identifier '"
                + xpointerContext.getXPointer()
                + "'");
    }
}
