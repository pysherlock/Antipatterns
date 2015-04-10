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
package org.apache.cocoon.optional.pipeline.components.sax.betwixt;

import java.beans.IntrospectionException;
import java.io.IOException;

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.util.StringRepresentation;
import org.apache.cocoon.sax.AbstractSAXGenerator;
import org.apache.cocoon.sax.SAXConsumer;
import org.apache.commons.betwixt.BindingConfiguration;
import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.SAXBeanWriter;
import org.xml.sax.SAXException;

public final class BetwixtBeanGenerator extends AbstractSAXGenerator {

    private Object bean;

    private SAXBeanWriter saxBeanWriter;

    private BindingConfiguration bindingConfiguration;

    private XMLIntrospector xmlIntrospector;

    public BetwixtBeanGenerator(Object bean) {
        this(bean, null, null);
    }

    public BetwixtBeanGenerator(Object bean, BindingConfiguration bindingConfiguration, XMLIntrospector xmlIntrospector) {
        super();
        if (bean == null) {
            throw new IllegalArgumentException("A Bean has to be passed.");
        }

        this.bean = bean;
        this.bindingConfiguration = bindingConfiguration;
        this.xmlIntrospector = xmlIntrospector;
    }

    @Override
    protected void setSAXConsumer(SAXConsumer xmlConsumer) {
        this.saxBeanWriter = new SAXBeanWriter(xmlConsumer);

        if (this.bindingConfiguration != null) {
            this.saxBeanWriter.setBindingConfiguration(this.bindingConfiguration);
        }
        if (this.xmlIntrospector != null) {
            this.saxBeanWriter.setXMLIntrospector(this.xmlIntrospector);
        }
    }

    public void execute() {
        try {
            this.saxBeanWriter.write(this.bean);
        } catch (IOException e) {
            throw new ProcessingException("Fatal protocol violation", e);
        } catch (SAXException e) {
            throw new ProcessingException("Fatal XML error", e);
        } catch (IntrospectionException e) {
            throw new ProcessingException("Impossible to analyze input bean", e);
        }
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "bean=" + this.bean, "bindingConfiguration="
                + this.bindingConfiguration, "xmlIntrospector=" + this.xmlIntrospector);
    }
}
