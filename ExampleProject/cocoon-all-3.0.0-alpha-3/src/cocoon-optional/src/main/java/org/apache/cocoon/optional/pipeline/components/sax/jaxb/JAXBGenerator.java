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
package org.apache.cocoon.optional.pipeline.components.sax.jaxb;

import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.component.CachingPipelineComponent;
import org.apache.cocoon.pipeline.util.StringRepresentation;
import org.apache.cocoon.sax.AbstractSAXGenerator;
import org.xml.sax.helpers.AttributesImpl;

/**
 * SAX generator that produces SAX events from Object using the JAXB marshaller.
 */
public class JAXBGenerator extends AbstractSAXGenerator implements CachingPipelineComponent {

    private static final String DEFAULT = "##default";

    private static final String UTF_8 = "UTF-8";

    private static final String EMPTY = "";

    private InMemoryLRUMarshallerCache marshallerCache = InMemoryLRUMarshallerCache.getInstance();

    private PluralStemmer pluralStemmer = PluralStemmer.getInstance();

    private GenericType<?> toBeMarshalled;

    private String charset = UTF_8;

    private boolean formattedOutput = false;

    public JAXBGenerator(GenericType<?> toBeMarshalled) {
        if (toBeMarshalled == null) {
            throw new IllegalArgumentException("Argument 'toBeMarshalled' must not be null");
        }
        this.toBeMarshalled = toBeMarshalled;
    }

    public String getCharset() {
        return this.charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public boolean isFormattedOutput() {
        return this.formattedOutput;
    }

    public void setFormattedOutput(boolean formattedOutput) {
        this.formattedOutput = formattedOutput;
    }

    public void execute() {
        // retrieve the right type has to be marshaled
        String xmlRootElementName = null;
        String xmlRootElementNameSpace = null;
        boolean pluralize = false;
        Class<?> type;

        // analyze generics for collections
        if (Iterable.class.isAssignableFrom(toBeMarshalled.getRawType())) {
            type = this.toBeMarshalled.getType();
            pluralize = true;
        } else if (this.toBeMarshalled.getRawType().isArray()) { // else get array type
            pluralize = true;
            type = this.toBeMarshalled.getRawType().getComponentType();
        } else { // otherwise is the passed argument type
            type = this.toBeMarshalled.getRawType();
        }

        // validate the type can be marshaled using JAXB
        if (!type.isAnnotationPresent(XmlRootElement.class)
                && !type.isAnnotationPresent(XmlType.class)) {
            throw new IllegalArgumentException("Object of type "
                    + type.getName()
                    + " can't be marshalled since neither of "
                    + XmlRootElement.class.getName()
                    + " or "
                    + XmlType.class.getName()
                    + " are present");
        }

        // get the root element name if needed
        if (pluralize) {
            String name = null;
            if (type.isAnnotationPresent(XmlRootElement.class)) {
                XmlRootElement xmlRootElement = type.getAnnotation(XmlRootElement.class);
                name = xmlRootElement.name();
                xmlRootElementNameSpace = xmlRootElement.namespace();
            } else if (type.isAnnotationPresent(XmlType.class)) {
                XmlType xmlType = type.getAnnotation(XmlType.class);
                name = xmlType.name();
                xmlRootElementNameSpace = xmlType.namespace();
            }

            if (name != null && !DEFAULT.equals(name)) {
                xmlRootElementName = name;
            } else {
                xmlRootElementName = type.getSimpleName();
            }

            if (DEFAULT.equals(xmlRootElementNameSpace)) {
                xmlRootElementNameSpace = EMPTY;
            }

            xmlRootElementName = pluralStemmer.toPlural(xmlRootElementName);
        }

        try {
            Marshaller xmlMarshaller = this.marshallerCache.getMarshaller(type);
            if (this.charset != null) {
                xmlMarshaller.setProperty(Marshaller.JAXB_ENCODING, this.charset);
            }
            xmlMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, this.formattedOutput);

            // setup the Marshaler if a collection has to be marshaled
            if (pluralize) {
                xmlMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
                this.getSAXConsumer().startDocument();
                this.getSAXConsumer().startElement(xmlRootElementNameSpace, xmlRootElementName, xmlRootElementName, new AttributesImpl());

                if (Iterable.class.isAssignableFrom(this.toBeMarshalled.getRawType())) {
                    for (Object object : (Iterable<Object>) this.toBeMarshalled.getObject()) {
                        xmlMarshaller.marshal(object, this.getSAXConsumer());
                    }
                } else if (this.toBeMarshalled.getRawType().isArray()) {
                    for (Object object : (Object[]) this.toBeMarshalled.getObject()) {
                        xmlMarshaller.marshal(object, this.getSAXConsumer());
                    }
                }

                this.getSAXConsumer().endElement(xmlRootElementNameSpace, xmlRootElementName, xmlRootElementName);
                this.getSAXConsumer().endDocument();
            } else {
                xmlMarshaller.marshal(this.toBeMarshalled.getObject(), this.getSAXConsumer());
            }
        } catch (Exception e) {
            throw new ProcessingException("Impossible to marshal object of type "
                    + type
                    + " to XML", e);
        }
    }

    public CacheKey constructCacheKey() {
        return new ObjectCacheKey(this.toBeMarshalled);
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "toBeMarshalled=" + this.toBeMarshalled);
    }
}
