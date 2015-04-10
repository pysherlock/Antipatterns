/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.cocoon.stax.stress.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.pipeline.component.Consumer;
import org.apache.cocoon.stax.StAXPipelineComponent;
import org.apache.cocoon.stax.StAXProducer;
import org.apache.cocoon.stax.component.XMLGenerator;
import org.apache.cocoon.stax.component.XMLSerializer;
import org.apache.cocoon.stax.stress.src.EmitElementTransformer;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;

/**
 * Contains unit tests for the emit transformer.<br>
 * 
 * It emits a number of configurable elements at a specific point of the document.
 * 
 */
public class EmitTransformerTest {

    /**
     * Shows a transformer emitting new elements at a point of the document step by step.
     * 
     * @exception Exception Is thrown if an error occurs loading the files or in the pipeline
     *                itself.
     */
    @Test
    public void testEmitTransformerBySteps() throws Exception {
        EmitElementTransformer transformer = new EmitElementTransformer("parent", new ArrayList<Attribute>(), "child",
                1);

        transformer.setParent(new StAXProducer() {
            private final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
            private boolean startEmitted = false;

            public boolean hasNext() {
                return true;
            }

            public XMLEvent nextEvent() throws XMLStreamException {
                if (this.startEmitted) {
                    return this.eventFactory.createEndElement("", "", "parent");
                }
                
                this.startEmitted = true;
                return this.eventFactory.createStartElement("", "", "parent");
            }

            public XMLEvent peek() throws XMLStreamException {
                throw new RuntimeException("not impl");
            }

            public void setConsumer(Consumer consumer) {
            }

            public void finish() {
            }

            public void setConfiguration(Map<String, ? extends Object> configuration) {
            }

            public void setup(Map<String, Object> parameters) {
            }
        });

        // "parent" start-tag
        assertTrue(transformer.hasNext());
        XMLEvent event = transformer.nextEvent();
        assertTrue(event.isStartElement());
        assertEquals(event.asStartElement().getName().getLocalPart(), "parent");

        // "child" start-tag
        assertTrue(transformer.hasNext());
        event = transformer.nextEvent();
        assertTrue(event.isStartElement());
        assertEquals(event.asStartElement().getName().getLocalPart(), "child");

        // "child" end-tag
        assertTrue(transformer.hasNext());
        event = transformer.nextEvent();
        assertTrue(event.isEndElement());
        assertEquals(event.asEndElement().getName().getLocalPart(), "child");

        // "element" end-tag
        assertTrue(transformer.hasNext());
        event = transformer.nextEvent();
        assertTrue(event.isEndElement());
        assertEquals(event.asEndElement().getName().getLocalPart(), "parent");
    }

    /**
     * Shows a transformer emitting new elements at a point of the document included in a pipeline.
     * 
     * @exception Exception Is thrown if an error occurs loading the files or in the pipeline
     *                itself.
     */
    @Test
    public void testEmitTransformer() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = new ByteArrayInputStream("<root/>".getBytes());

        Pipeline<StAXPipelineComponent> pipe = new NonCachingPipeline<StAXPipelineComponent>();
        pipe.addComponent(new XMLGenerator(in));
        pipe.addComponent(new EmitElementTransformer("root", new ArrayList<Attribute>(), "element", 5));
        pipe.addComponent(new XMLSerializer());
        pipe.setup(out);
        pipe.execute();

        String created = new String(out.toByteArray());
        out.close();

        StringBuffer correctOne = new StringBuffer("<root>");
        for (int i = 0; i < 5; i++) {
            correctOne.append("<element/>");
        }
        correctOne.append("</root>");
        XMLUnit.setIgnoreWhitespace(true);
        Diff myDiff = new Diff(correctOne.toString(), created);
        assertTrue("pieces of XML are similar " + myDiff, myDiff.similar());
    }
}
