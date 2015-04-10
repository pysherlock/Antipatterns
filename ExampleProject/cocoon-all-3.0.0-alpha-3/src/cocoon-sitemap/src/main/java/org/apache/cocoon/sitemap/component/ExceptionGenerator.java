/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.sitemap.component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.component.Starter;
import org.apache.cocoon.sax.AbstractSAXProducer;
import org.apache.cocoon.sitemap.util.ParameterHelper;
import org.apache.cocoon.xml.sax.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class ExceptionGenerator extends AbstractSAXProducer implements Starter {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

    public static final String EXCEPTION_NS = "";

    private Map<String, Object> parameters;

    // public static final String EXCEPTION_NS = "http://apache.org/cocoon/exception/1.0";

    private static void simpleElement(String name, Attributes attr, String value, ContentHandler handler)
            throws SAXException {
        handler.startElement(EXCEPTION_NS, name, name, attr);
        // handler.startElement(EXCEPTION_NS, name, "ex:" + name, attr);
        if (value != null && value.length() > 0) {
            handler.characters(value.toCharArray(), 0, value.length());
        }
        handler.endElement(EXCEPTION_NS, name, name);
        // handler.endElement(EXCEPTION_NS, name, "ex:" + name);
    }

    public void execute() {
        Throwable throwable = ParameterHelper.getThrowable(this.parameters);

        try {
            this.getSAXConsumer().startDocument();
            this.toSAX(throwable, this.getSAXConsumer());
            this.getSAXConsumer().endDocument();
        } catch (SAXException e) {
            throw new ProcessingException("Failed to generate exception document.", e);
        }
    }

    private void toSAX(Throwable throwable, ContentHandler handler) throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        // handler.startPrefixMapping("ex", EXCEPTION_NS);
        attr.addCDATAAttribute("class", throwable.getClass().getName());
        attr.addCDATAAttribute("timestamp", this.dateFormat.format(new Date()));
        handler.startElement(EXCEPTION_NS, "exception-report", "exception-report", attr);
        // handler.startElement(EXCEPTION_NS, "exception-report", "ex:exception-report", attr);

        // exception message
        attr.clear();
        simpleElement("message", attr, throwable.getMessage(), handler);

        // exception stacktrace
        attr.clear();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        simpleElement("stacktrace", attr, sw.getBuffer().toString(), handler);

        handler.endElement(EXCEPTION_NS, "exception-report", "exception-report");
        // handler.endElement(EXCEPTION_NS, "exception-report", "ex:exception-report");
        // handler.endPrefixMapping("ex");
    }

    @Override
    public void setup(Map<String, Object> parameters) {
        super.setup(parameters);
        this.parameters = parameters;
    }
}
