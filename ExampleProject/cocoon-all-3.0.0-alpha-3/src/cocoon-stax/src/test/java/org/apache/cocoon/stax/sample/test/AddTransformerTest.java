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

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.stax.StAXPipelineComponent;
import org.apache.cocoon.stax.component.XMLGenerator;
import org.apache.cocoon.stax.component.XMLSerializer;
import org.apache.cocoon.stax.sample.src.ExampleAddTransformer;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;

/**
 * Contains unit tests showing how a transformer could work that adds additional elements in a
 * document.
 * 
 * <br>
 * 
 * Shows a transformer adding additional elements at specific points into a document.
 */
public class AddTransformerTest {

    /**
     * Shows a transformer adding additional elements at specific points into a document.
     * 
     * @exception Exception Is thrown if an error occurs loading the files or in the pipeline
     *                itself.
     */
    @Test
    public void testAddition() throws Exception {
        InputStream input = AddTransformerTest.class.getResource(
                "/org/apache/cocoon/stax/sample/stax-test-document.xml").openStream();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Pipeline<StAXPipelineComponent> pipe = new NonCachingPipeline<StAXPipelineComponent>();
        pipe.addComponent(new XMLGenerator(input));
        pipe.addComponent(new ExampleAddTransformer("somethingdifferent", null));
        pipe.addComponent(new XMLSerializer());
        pipe.setup(out);
        pipe.execute();

        String created = out.toString();
        String correctOne = IOUtils.toString(AddTransformerTest.class.getResource(
                "/org/apache/cocoon/stax/sample/stax-test-document-add.xml").openStream());

        XMLUnit.setIgnoreWhitespace(true);
        Diff myDiff = new Diff(correctOne, created);
        assertTrue("pieces of XML are not similar: " + myDiff, myDiff.similar());
    }
}
