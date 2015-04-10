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
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.pipeline.component.PipelineComponent;
import org.apache.cocoon.stax.component.XMLSerializer;
import org.apache.cocoon.stax.component.XMLGenerator;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;

/**
 * Contains unit tests for testing and presenting the most simple cases for a StAX pipeline.<br>
 *
 * Communication between a StAX Generator and a StAX Serializer<br>
 * Connection of SAX and StAX Components is detected as error
 */
public class StAXPipelineTest {

    /**
     * Very simple test case loading a simple xml file as input stream, and starts the most simple
     * pipeline (Generator -> Serializer) on it. Finally the output is compared to the file used for
     * input. This test should present the most basic ideas about a StAX pipeline.
     *
     * @exception Exception Is thrown if an error occurs loading the files or in the pipeline
     *                itself.
     */
    @Test
    public void testProducerConsumer() throws Exception {
        InputStream input = StAXPipelineTest.class.getResource("/org/apache/cocoon/stax/simple.xml").openStream();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Pipeline<StAXPipelineComponent> pipe = new NonCachingPipeline<StAXPipelineComponent>();
        pipe.addComponent(new XMLGenerator(input));
        pipe.addComponent(new XMLSerializer());
        pipe.setup(out);
        pipe.execute();

        String created = out.toString();
        String correctOne = IOUtils.toString(StAXPipelineTest.class.getResource("/org/apache/cocoon/stax/simple.xml")
                .openStream());

        XMLUnit.setIgnoreWhitespace(true);
        Diff myDiff = new Diff(correctOne, created);
        assertTrue("pieces of XML are not similar: " + myDiff, myDiff.similar());
    }

    /**
     * Demonstrates that connecting a SAX and a StAX component in the same pipeline results in a
     * {@link SetupException}.
     */
    @Test(expected = SetupException.class)
    public void pipelineWithWrongComponents() {
        Pipeline<PipelineComponent> pipeline = new NonCachingPipeline<PipelineComponent>();
        pipeline.addComponent(new org.apache.cocoon.sax.component.XMLGenerator("<test/>"));
        pipeline.addComponent(new XMLSerializer());

        pipeline.setup(new ByteArrayOutputStream());
        fail("Mixing wrong components which has to result in a SetupException");
    }
}
