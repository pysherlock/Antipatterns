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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.xml.stream.events.Attribute;

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.stax.StAXPipelineComponent;
import org.apache.cocoon.stax.component.IncludeTransformer;
import org.apache.cocoon.stax.component.XMLGenerator;
import org.apache.cocoon.stax.component.XMLSerializer;
import org.apache.cocoon.stax.stress.src.EmitElementTransformer;
import org.apache.cocoon.stax.stress.src.GeneratingStarter;
import org.apache.cocoon.stax.stress.src.OmitElementTransformer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * Contains the tests with huge data sets.<br>
 * It should show that the cocoon-stax implementation is capable to process data bigger than the
 * available virtual memory.
 * 
 */
public class HugeDataTest {

    private final int TEST_SIZE;

    public HugeDataTest() {
        // results in documents with approx. 2 times of the maximum memory. The
        // number is designed for systems running with 256mb of ram. Since some
        // maven processes require more than that a minimum value was added that
        // the tests could be run in acceptable time.
        this.TEST_SIZE = Math.min((int) (Runtime.getRuntime().maxMemory() * 2 / 10), 102196838);
    }

    /**
     * Tests an huge input document, which is processed to a very small output.
     * 
     * @exception Exception Is thrown if an error occurs loading the files or in the pipeline
     *                itself.
     */
    @Test
    public void testHugeInSmallOut() throws Exception {
        Pipeline<StAXPipelineComponent> pipe = new NonCachingPipeline<StAXPipelineComponent>();
        pipe.addComponent(new GeneratingStarter("root", "element", this.TEST_SIZE));
        pipe.addComponent(new OmitElementTransformer("element", new ArrayList<Attribute>()));
        pipe.addComponent(new XMLSerializer());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pipe.setup(out);
        pipe.execute();
        out.close();
    }

    /**
     * Tests an adding transformer variant, which produces a huge output document from a small input
     * document.
     * 
     * @exception Exception Is thrown if an error occurs loading the files or in the pipeline
     *                itself.
     */
    @Test
    public void testSmallInHugeOut() throws Exception {
        File tmpOutput = File.createTempFile("C3HUGE", ".xml");
        tmpOutput.deleteOnExit();
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tmpOutput));
        InputStream in = new ByteArrayInputStream("<root/>".getBytes());

        Pipeline<StAXPipelineComponent> pipe = new NonCachingPipeline<StAXPipelineComponent>();
        pipe.addComponent(new XMLGenerator(in));
        pipe.addComponent(new EmitElementTransformer("root", new ArrayList<Attribute>(), "element", this.TEST_SIZE));
        pipe.addComponent(new XMLSerializer());
        pipe.setup(out);
        pipe.execute();
    }

    /**
     * Tests using an {@link IncludeTransformer} to include a huge file.
     * 
     * @exception Exception Is thrown if an error occurs loading the files or in the pipeline
     *                itself.
     */
    @Test
    public void testSmallIn_IncludeHuge_HugeOut() throws Exception {
        File input = File.createTempFile("C3STAXHUGEINC_INPUT", ".xml");
        File include = File.createTempFile("C3STAXHUGEINC_INCLUDE", ".xml");
        File output = File.createTempFile("C3STAXHUGEINC_OUTPUT", ".xml");
        input.deleteOnExit();
        include.deleteOnExit();
        output.deleteOnExit();
        PrintStream includeSource = new PrintStream(include);
        includeSource.println("<test>");
        for (int i = 0; i < this.TEST_SIZE; i++) {
            includeSource.println("<element></element>");
        }
        includeSource.println("</test>");
        includeSource.close();

        String inputSource = IOUtils.toString(
                HugeDataTest.class
                        .getResourceAsStream("/org/apache/cocoon/stax/stress/stax-test-huge-include-input.xml"))
                .replaceAll("file_to_include\\.xml", include.toURI().toURL().toString());
        FileUtils.writeStringToFile(input, inputSource);
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(output));
        Pipeline<StAXPipelineComponent> pipe = new NonCachingPipeline<StAXPipelineComponent>();
        pipe.addComponent(new XMLGenerator(new FileInputStream(input)));
        pipe.addComponent(new IncludeTransformer());
        pipe.addComponent(new XMLSerializer());
        pipe.setup(out);
        pipe.execute();

        out.close();
    }
}
