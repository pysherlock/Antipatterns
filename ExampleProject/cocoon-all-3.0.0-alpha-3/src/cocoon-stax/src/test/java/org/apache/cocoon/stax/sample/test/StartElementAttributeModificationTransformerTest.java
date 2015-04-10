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
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.stax.StAXPipelineComponent;
import org.apache.cocoon.stax.component.XMLGenerator;
import org.apache.cocoon.stax.component.XMLSerializer;
import org.apache.cocoon.stax.sample.src.StartElementAttributeModificationTransformer;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;

/**
 * Contains unit tests showing how a transformer could work that allows to modify the attributes of
 * elements.<br> * Show correct modification of an attribute of a specific element.<br> * Show
 * that trying to modify an element with several attributes will not affect elements containing only
 * some of these attributes.
 */
public class StartElementAttributeModificationTransformerTest {

    /**
     * Show correct modification of an attribute of a specific element.
     * 
     * @throws Exception Is thrown if an error occurs loading the files or in the pipeline itself.
     */
    @Test
    public void testModification() throws Exception {
        InputStream input = StartElementAttributeModificationTransformerTest.class.getResource(
                "/org/apache/cocoon/stax/sample/stax-test-document.xml").openStream();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Pipeline<StAXPipelineComponent> pipe = new NonCachingPipeline<StAXPipelineComponent>();
        pipe.addComponent(new XMLGenerator(input));
        XMLEventFactory factory = XMLEventFactory.newInstance();
        List<Attribute> atts = new ArrayList<Attribute>();
        atts.add(factory.createAttribute("attribute", "bad"));
        List<Attribute> newAtts = new ArrayList<Attribute>();
        newAtts.add(factory.createAttribute("attribute", "good"));
        StartElementAttributeModificationTransformer modifier = new StartElementAttributeModificationTransformer(
                "anyelement", atts, newAtts);
        pipe.addComponent(modifier);
        pipe.addComponent(new XMLSerializer());
        pipe.setup(out);
        pipe.execute();

        input.close();
        String created = out.toString();
        out.close();
        String correctOne = IOUtils.toString(StartElementAttributeModificationTransformerTest.class.getResource(
                "/org/apache/cocoon/stax/sample/" + "stax-test-document-modified2.xml").openStream());

        XMLUnit.setIgnoreWhitespace(true);
        Diff myDiff = new Diff(correctOne, created);
        assertTrue("pieces of XML are similar " + myDiff, myDiff.similar());
    }

    /**
     * Show that trying to modify an element with several attributes will not affect elements
     * containing only some of these attributes.
     * 
     * @throws Exception Is thrown if an error occurs loading the files or in the pipeline itself.
     */
    @Test
    public void testNoModification() throws Exception {
        InputStream input = StartElementAttributeModificationTransformerTest.class.getResource(
                "/org/apache/cocoon/stax/sample/stax-test-document.xml").openStream();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Pipeline<StAXPipelineComponent> pipe = new NonCachingPipeline<StAXPipelineComponent>();
        pipe.addComponent(new XMLGenerator(input));
        XMLEventFactory factory = XMLEventFactory.newInstance();
        List<Attribute> atts = new ArrayList<Attribute>();
        atts.add(factory.createAttribute("attribute", "bad"));
        atts.add(factory.createAttribute("attribute", "good"));
        List<Attribute> newAtts = new ArrayList<Attribute>();
        StartElementAttributeModificationTransformer modifier = new StartElementAttributeModificationTransformer(
                "anyelement", atts, newAtts);
        pipe.addComponent(modifier);
        pipe.addComponent(new XMLSerializer());
        pipe.setup(out);
        pipe.execute();

        input.close();
        String created = out.toString();
        out.close();
        String correctOne = IOUtils.toString(StartElementAttributeModificationTransformerTest.class.getResource(
                "/org/apache/cocoon/stax/sample/" + "stax-test-document.xml").openStream());

        XMLUnit.setIgnoreWhitespace(true);
        Diff myDiff = new Diff(correctOne, created);
        assertTrue("pieces of XML are similar " + myDiff, myDiff.similar());
    }
}
