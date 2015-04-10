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

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.stax.StAXPipelineComponent;
import org.apache.cocoon.stax.component.XMLSerializer;
import org.apache.cocoon.stax.stress.src.GeneratingStarter;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;

/**
 * Contains unit tests of the generating starter, which generates a document on the fly.<br>
 * 
 */
public class GeneratingStarterTest {

    /**
     * Tests an empty document - that is an document without any child elements.
     * 
     * @exception Exception Is thrown if an error occurs loading the files or in the pipeline
     *                itself.
     */
    @Test
    public void testGeneratingStarterEmpty() throws Exception {
        this.doTest(0);
    }

    /**
     * Tests an document with one child element.
     * 
     * @exception Exception Is thrown if an error occurs loading the files or in the pipeline
     *                itself.
     */
    @Test
    public void testGeneratingStarterOneChildElement() throws Exception {
        this.doTest(1);
    }

    /**
     * Tests an document with more child elements.
     * 
     * @exception Exception Is thrown if an error occurs loading the files or in the pipeline
     *                itself.
     */
    @Test
    public void testGeneratingStarterMultipleChildElements() throws Exception {
        this.doTest(100);
    }

    /**
     * Helper test method, sets up the pipeline and GeneratingStarter and verifies the result.
     * 
     * @param count the number of child elements of the GeneratingStarter
     * @throws Exception
     */
    private void doTest(int count) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Pipeline<StAXPipelineComponent> pipe = new NonCachingPipeline<StAXPipelineComponent>();
        pipe.addComponent(new GeneratingStarter("root", "element", count));
        pipe.addComponent(new XMLSerializer());
        pipe.setup(out);
        pipe.execute();

        String created = new String(out.toByteArray());
        out.close();
        StringBuffer correctOne = new StringBuffer("<root>");
        for (int i = 0; i < count; i++) {
            correctOne.append("<element/>");
        }
        correctOne.append("</root>");

        XMLUnit.setIgnoreWhitespace(true);
        Diff myDiff = new Diff(correctOne.toString(), created);
        assertTrue("pieces of XML are similar " + myDiff, myDiff.similar());
    }
}
