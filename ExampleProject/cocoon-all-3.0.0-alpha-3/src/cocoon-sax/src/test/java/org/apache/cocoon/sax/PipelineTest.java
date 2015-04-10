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

import static org.apache.cocoon.pipeline.builder.PipelineBuilder.newCachingPipeline;
import static org.apache.cocoon.sax.builder.SAXPipelineBuilder.newNonCachingPipeline;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.cocoon.sax.component.XMLGenerator;
import org.apache.cocoon.sax.component.XMLSerializer;
import org.apache.cocoon.sax.component.XSLTTransformer;
import org.custommonkey.xmlunit.Diff;

public class PipelineTest extends TestCase {

    /**
     * A pipeline that performs a simple transformation: generator -&gt; transformer -&gt;
     * serializer.
     */
    public void testPipelineWithTransformer() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        newNonCachingPipeline()
            .setStringGenerator("<x></x>")
            .addXSLTTransformer(this.getClass().getResource("/test.xslt"))
            .addSerializer()
            .withEmptyConfiguration()
            .setup(baos)
            .execute();

        Diff diff = new Diff("<?xml version=\"1.0\" encoding=\"UTF-8\"?><p></p>", new String(baos.toByteArray()));
        assertTrue("XSL transformation didn't work as expected " + diff, diff.identical());
    }

    /**
     * A pipeline that performs a simple transformation: generator -&gt; transformer -&gt;
     * serializer; the transformer uses a compiled XSLT using Xalan xsltc.
     *
     * @throws Exception if any error occurs.
     */
    public void testPipelineWithCompiledXSLT() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("translet-name", "CompiledXslt");
        attributes.put("package-name", "org.apache.cocoon.sax");

        newCachingPipeline()
            .setStarter(new XMLGenerator("<x></x>"))
            .addComponent(new XSLTTransformer(this.getClass().getResource("/test.xslt"), attributes))
            .setFinisher(new XMLSerializer())
            .withEmptyConfiguration()
            .setup(baos)
            .execute();

        Diff diff = new Diff("<?xml version=\"1.0\" encoding=\"UTF-8\"?><p></p>", new String(baos.toByteArray()));
        assertTrue("XSL transformation didn't work as expected " + diff, diff.identical());
    }
}
