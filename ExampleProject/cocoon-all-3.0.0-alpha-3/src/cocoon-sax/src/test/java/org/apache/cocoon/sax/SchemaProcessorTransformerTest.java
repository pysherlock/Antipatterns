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
package org.apache.cocoon.sax;

import static junit.framework.Assert.*;

import java.io.ByteArrayOutputStream;

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.sax.component.XMLGenerator;
import org.apache.cocoon.sax.component.SchemaProcessorTransformer;
import org.apache.cocoon.sax.component.SchemaValidationException;
import org.apache.cocoon.sax.component.XMLSerializer;
import org.custommonkey.xmlunit.Diff;
import org.junit.Test;

public class SchemaProcessorTransformerTest {

    /**
     * A pipeline that performs an identity transformation, using the validation: generator -&gt;
     * validator -&gt; serializer
     */
    @Test
    public void testPipelineWithValidation() throws Exception {
        Pipeline<SAXPipelineComponent> pipeline = this.createValidatingPipeline("<x></x>");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipeline.setup(baos);
        pipeline.execute();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><x></x>";
        String actual = new String(baos.toByteArray());

        Diff diff = new Diff(expected, actual);
        assertTrue("XSL transformation didn't work as expected " + diff, diff.identical());
    }

    /**
     * A pipeline that performs an identity transformation, using the validation: generator -&gt;
     * validator -&gt; serializer. An error is expected performing <code>execute</code> method due
     * to not valid xml input.
     */
    @Test(expected = SchemaValidationException.class)
    public void testPipelineWithWrongValidation() throws Exception {
        Pipeline<SAXPipelineComponent> pipeline = this.createValidatingPipeline("<y><z/></y>");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipeline.setup(baos);
        pipeline.execute();
    }

    private Pipeline<SAXPipelineComponent> createValidatingPipeline(String xmlInput) {
        Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
        pipeline.addComponent(new XMLGenerator(xmlInput));
        pipeline.addComponent(new SchemaProcessorTransformer(this.getClass().getResource("/test.xsd")));
        pipeline.addComponent(new XMLSerializer());

        return pipeline;
    }
}
