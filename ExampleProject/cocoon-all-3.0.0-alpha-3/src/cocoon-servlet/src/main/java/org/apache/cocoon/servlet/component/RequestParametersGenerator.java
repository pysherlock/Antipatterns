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
package org.apache.cocoon.servlet.component;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.cocoon.pipeline.component.Starter;
import org.apache.cocoon.sax.AbstractSAXProducer;
import org.apache.cocoon.servlet.util.HttpContextHelper;
import org.apache.cocoon.sitemap.InvocationException;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class RequestParametersGenerator extends AbstractSAXProducer implements Starter {

    private Map<String, Object> parameters;

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.component.Starter#execute()
     */
    @SuppressWarnings("unchecked")
    public void execute() {
        HttpServletRequest request = HttpContextHelper.getRequest(this.parameters);
        Enumeration<String> parameterNames = request.getParameterNames();

        try {
            this.getSAXConsumer().startDocument();
            this.getSAXConsumer().startElement("", "request-parameters", "request-parameters", new AttributesImpl());

            while (parameterNames.hasMoreElements()) {
                String name = parameterNames.nextElement();
                String value = request.getParameter(name);
                this.getSAXConsumer().startElement("", name, name, new AttributesImpl());
                this.getSAXConsumer().characters(value.toCharArray(), 0, value.length());
                this.getSAXConsumer().endElement("", name, name);
            }

            this.getSAXConsumer().endElement("", "request-parameters", "request-parameters");
            this.getSAXConsumer().endDocument();
        } catch (SAXException e) {
            throw new InvocationException(e);
        }
    }

    @Override
    public void setup(Map<String, Object> parameters) {
        super.setup(parameters);
        this.parameters = parameters;
    }
}
