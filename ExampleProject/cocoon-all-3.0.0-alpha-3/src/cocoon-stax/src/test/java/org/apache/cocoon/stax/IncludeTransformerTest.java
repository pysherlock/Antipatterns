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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.stax.component.IncludeTransformer;
import org.apache.cocoon.stax.component.XMLGenerator;
import org.apache.cocoon.stax.component.XMLSerializer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;

/**
 * Contains unit tests for the include transformer.<br>
 * 
 * It parses the input document for special include tags, which are replaced with a referenced
 * documents.
 */
public class IncludeTransformerTest {

    /**
     * Shows a transformer including another document. Since the included document has to be
     * referenced by an URL, it is copied to a temporary file.
     * 
     * @exception Exception Is thrown if an error occurs loading the files or in the pipeline
     *                itself.
     */
    @Test
    public void testInclude() throws Exception {
        File input = File.createTempFile("C3STAXINC_INPUT", ".xml");
        File include = File.createTempFile("C3STAXINC_INCLUDE", ".xml");
        input.deleteOnExit();
        include.deleteOnExit();

        IOUtils.copy(IncludeTransformerTest.class.getResourceAsStream("/org/apache/cocoon/stax/file_to_include.xml"),
                new FileOutputStream(include));
        String inputSource = IOUtils.toString(
                IncludeTransformerTest.class.getResourceAsStream("/org/apache/cocoon/stax/includetest_input.xml"))
                .replaceAll("file_to_include\\.xml", include.toURI().toURL().toString());
        FileUtils.writeStringToFile(input, inputSource);

        Pipeline<StAXPipelineComponent> pipe = new NonCachingPipeline<StAXPipelineComponent>();
        pipe.addComponent(new XMLGenerator(new FileInputStream(input)));
        pipe.addComponent(new IncludeTransformer());
        pipe.addComponent(new XMLSerializer());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pipe.setup(out);
        pipe.execute();
        out.close();

        String correctOne = IOUtils.toString(IncludeTransformerTest.class
                .getResourceAsStream("/org/apache/cocoon/stax/includetest_output.xml"));

        XMLUnit.setIgnoreWhitespace(true);
        Diff myDiff = new Diff(correctOne.toString(), new String(out.toByteArray()));
        assertTrue("pieces of XML are similar " + myDiff, myDiff.similar());
    }
}
