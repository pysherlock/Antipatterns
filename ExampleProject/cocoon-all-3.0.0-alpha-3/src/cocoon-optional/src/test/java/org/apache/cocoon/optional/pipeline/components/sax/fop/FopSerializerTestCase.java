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
package org.apache.cocoon.optional.pipeline.components.sax.fop;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.sax.SAXPipelineComponent;
import org.apache.cocoon.sax.component.XMLGenerator;
import org.apache.cocoon.sax.component.XSLTTransformer;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

public final class FopSerializerTestCase {

    @Test
    public void testPipelineWithFOPSerializer() throws Exception {
        Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
        pipeline.addComponent(new XMLGenerator(this.getClass().getResource("page.xml")));
        pipeline.addComponent(new XSLTTransformer(this.getClass().getResource("page2fo.xsl")));
        pipeline.addComponent(new FopSerializer());

        File tmp = File.createTempFile("fop_test_case", ".pdf");
        FileOutputStream fos = new FileOutputStream(tmp);
        pipeline.setup(fos);
        pipeline.execute();

        assertTrue("FOP Serialization didn't work as expected, " + tmp.getAbsolutePath()
                + " should not be an empty file", tmp.length() > 0);

        FileInputStream fis = new FileInputStream(tmp);
        String pdfContent = IOUtils.toString(fis);
        assertTrue(pdfContent.startsWith("%PDF-1.4"));
    }
}
