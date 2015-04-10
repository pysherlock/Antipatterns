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
package org.apache.cocoon.stax;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.sax.SAXPipelineComponent;
import org.apache.cocoon.stax.component.CleaningTransformer;
import org.apache.cocoon.stax.component.XMLGenerator;
import org.apache.cocoon.stax.component.XMLSerializer;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;

/**
 * Contains unit tests for a {@link CleaningTransformer}.<br> *
 * Compares if SAX and StAX Cleaning Transformer work identically.
 */
public class CleaningTransformerTest {

    /**
     * Compares the output of the {@link CleaningTransformer} with the output of the
     * {@link CleaningTransformer} to show that they process input data identically.
     *
     * @throws Exception Is thrown if an error occurs loading the files or in the pipeline itself.
     */
    @Test
    public void testCleaningTransformer() throws Exception {
        NonCachingPipeline<SAXPipelineComponent> pipeSAX = new NonCachingPipeline<SAXPipelineComponent>();
        pipeSAX.addComponent(new org.apache.cocoon.sax.component.XMLGenerator(IOUtils
                .toString(CleaningTransformerTest.class.getResource(
                        "/org/apache/cocoon/stax/complex-stax-test-document.xml").openStream())));

        pipeSAX.addComponent(new org.apache.cocoon.sax.component.CleaningTransformer());
        pipeSAX.addComponent(new org.apache.cocoon.sax.component.XMLSerializer());

        ByteArrayOutputStream outputSAX = new ByteArrayOutputStream();
        pipeSAX.setup(outputSAX);
        pipeSAX.execute();

        String outputSAXString = outputSAX.toString();
        outputSAX.close();

        InputStream input = CleaningTransformerTest.class.getResource(
                "/org/apache/cocoon/stax/complex-stax-test-document.xml").openStream();

        ByteArrayOutputStream outputStAX = new ByteArrayOutputStream();

        NonCachingPipeline<StAXPipelineComponent> pipeStAX = new NonCachingPipeline<StAXPipelineComponent>();
        pipeStAX.addComponent(new XMLGenerator(input));
        pipeStAX.addComponent(new CleaningTransformer());
        pipeStAX.addComponent(new XMLSerializer());

        pipeStAX.setup(outputStAX);
        pipeStAX.execute();
        input.close();

        String outputStAXString = outputStAX.toString();
        outputStAX.close();

        XMLUnit.setIgnoreWhitespace(false);
        Diff myDiff = new Diff(outputSAXString, outputStAXString);
        assertTrue("pieces of XML are similar " + myDiff, myDiff.identical());
    }
}
