/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.optional.pipeline.components.sax.neko;

import static org.custommonkey.xmlunit.XMLAssert.assertNodeTestPasses;

import java.io.ByteArrayOutputStream;

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.sax.SAXPipelineComponent;
import org.apache.cocoon.sax.component.XMLSerializer;
import org.custommonkey.xmlunit.examples.CountingNodeTester;
import org.junit.Test;
import org.w3c.dom.Node;

public final class NekoGeneratorTestCase {

    @Test
    public void testPipelineWithNekoGenerator() throws Exception {
        Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
        pipeline.addComponent(new NekoGenerator(this.getClass().getResource("sample.html")));
        pipeline.addComponent(new XMLSerializer());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipeline.setup(baos);
        pipeline.execute();

        CountingNodeTester countingNodeTester = new CountingNodeTester(9);

        assertNodeTestPasses(new String(baos.toByteArray()), countingNodeTester, Node.TEXT_NODE);
    }
}
