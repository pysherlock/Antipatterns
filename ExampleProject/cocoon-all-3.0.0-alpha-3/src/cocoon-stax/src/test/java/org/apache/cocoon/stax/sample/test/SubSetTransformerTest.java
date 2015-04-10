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
package org.apache.cocoon.stax.sample.test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.stax.StAXPipelineComponent;
import org.apache.cocoon.stax.component.CleaningTransformer;
import org.apache.cocoon.stax.component.XMLGenerator;
import org.apache.cocoon.stax.component.XMLSerializer;
import org.apache.cocoon.stax.sample.src.SubSetTransformer;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;

/**
 * Contains unit tests for a {@link SubSetTransformer} that removes everything in a xml document but
 * a specified sub tree.<br>
 * 
 * * Removes everything but the element "element" with the attribute "attribute='first'".<br>
 * * Removes everything but the element "element" with the attribute "attribute='second'".<br>
 * * Removes everything because specifying two attributes will not trigger elements containing only
 * one of them.<br>
 * * Removes everything but the element "element" with no attribute specified which results in
 * everything is deleted but the first occurrence of a "element" element containing any attributes.
 */
public class SubSetTransformerTest {

    /**
     * Shows a {@link SubSetTransformer} that removes everything but the element "element" with the
     * attribute "attribute='first'".
     * 
     * @exception Exception Is thrown if an error occurs loading the files or in the pipeline
     *                itself.
     */
    @Test
    public void testFirstSubtree() throws Exception {
        InputStream input = SubSetTransformerTest.class.getResource("/org/apache/cocoon/stax/sample/special_root.xml")
                .openStream();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Pipeline<StAXPipelineComponent> pipe = new NonCachingPipeline<StAXPipelineComponent>();
        pipe.addComponent(new XMLGenerator(input));
        XMLEventFactory factory = XMLEventFactory.newInstance();
        pipe.addComponent(new SubSetTransformer("element", factory.createAttribute(new QName("attribute"), "first")));
        pipe.addComponent(new XMLSerializer());
        pipe.setup(out);
        pipe.execute();

        String created = out.toString();
        String correctOne = IOUtils.toString(SubSetTransformerTest.class.getResource(
                "/org/apache/cocoon/stax/sample/special_root_first.xml").openStream());

        XMLUnit.setIgnoreWhitespace(true);
        Diff myDiff = new Diff(correctOne, created);
        assertTrue("pieces of XML are not similar: " + myDiff, myDiff.similar());
    }

    /**
     * Shows a {@link SubSetTransformer} that removes everything but the element "element" with the
     * attribute "attribute='second'".
     * 
     * @exception Exception Is thrown if an error occurs loading the files or in the pipeline
     *                itself.
     */
    @Test
    public void testSecondSubtree() throws Exception {
        InputStream input = SubSetTransformerTest.class.getResource("/org/apache/cocoon/stax/sample/special_root.xml")
                .openStream();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Pipeline<StAXPipelineComponent> pipe = new NonCachingPipeline<StAXPipelineComponent>();
        pipe.addComponent(new XMLGenerator(input));
        XMLEventFactory factory = XMLEventFactory.newInstance();
        pipe.addComponent(new SubSetTransformer("element", factory.createAttribute(new QName("attribute"), "second")));
        pipe.addComponent(new XMLSerializer());
        pipe.setup(out);
        pipe.execute();

        String created = out.toString();
        String correctOne = IOUtils.toString(SubSetTransformerTest.class.getResource(
                "/org/apache/cocoon/stax/sample/special_root_second.xml").openStream());

        XMLUnit.setIgnoreWhitespace(true);
        Diff myDiff = new Diff(correctOne, created);
        assertTrue("pieces of XML are not similar: " + myDiff, myDiff.similar());
    }

    /**
     * Shows a {@link SubSetTransformer} that removes everything because specifying two attributes
     * will not trigger elements containing only one of them.
     * 
     * @exception Exception Is thrown if an error occurs loading the files or in the pipeline
     *                itself.
     */
    @Test
    public void testNoSubtree() throws Exception {
        InputStream input = SubSetTransformerTest.class.getResource("/org/apache/cocoon/stax/sample/special_root.xml")
                .openStream();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Pipeline<StAXPipelineComponent> pipe = new NonCachingPipeline<StAXPipelineComponent>();
        pipe.addComponent(new XMLGenerator(input));
        XMLEventFactory factory = XMLEventFactory.newInstance();
        pipe.addComponent(new CleaningTransformer());
        pipe.addComponent(new SubSetTransformer("element", factory.createAttribute(new QName("attribute"), "second"),
                factory.createAttribute(new QName("attribute"), "first")));
        pipe.addComponent(new XMLSerializer());
        pipe.setup(out);
        pipe.execute();

        // The replace step is required since the jdk1.6 default implementation produces other chars
        // than the woodstox implementation used for jdk1.5
        String created = out.toString().replace("\'", "\"");

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", created);
    }

    /**
     * Shows a {@link SubSetTransformer} that removes everything but the element "element" with no
     * attribute specified which results in everything is deleted but the first occurrence of a
     * "element" element containing any attributes.
     * 
     * @exception Exception Is thrown if an error occurs loading the files or in the pipeline
     *                itself.
     */
    @Test
    public void testNoAttributesFirstSubtree() throws Exception {
        InputStream input = SubSetTransformerTest.class.getResource("/org/apache/cocoon/stax/sample/special_root.xml")
                .openStream();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Pipeline<StAXPipelineComponent> pipe = new NonCachingPipeline<StAXPipelineComponent>();
        pipe.addComponent(new XMLGenerator(input));
        pipe.addComponent(new SubSetTransformer("element"));
        pipe.addComponent(new XMLSerializer());
        pipe.setup(out);
        pipe.execute();

        String created = out.toString();
        String correctOne = IOUtils.toString(SubSetTransformerTest.class.getResource(
                "/org/apache/cocoon/stax/sample/special_root_first.xml").openStream());

        XMLUnit.setIgnoreWhitespace(true);
        Diff myDiff = new Diff(correctOne, created);
        assertTrue("pieces of XML are not similar: " + myDiff, myDiff.similar());
    }
}
