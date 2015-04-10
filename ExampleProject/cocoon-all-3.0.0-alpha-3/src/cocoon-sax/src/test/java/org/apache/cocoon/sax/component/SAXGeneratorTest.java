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
package org.apache.cocoon.sax.component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.sax.AbstractSAXSerializer;
import org.apache.cocoon.sax.SAXPipelineComponent;
import org.apache.cocoon.sax.SAXProducer;
import org.apache.cocoon.xml.sax.SAXBuffer;
import org.custommonkey.xmlunit.Diff;
import org.junit.Test;

public class SAXGeneratorTest {

    private static final String INVALID_XML_STRING = "<?xml version=\"1.0\"?><test>text<test>";
    private static final String VALID_XML_STRING = "<?xml version=\"1.0\"?><test>text</test>";

    @Test
    public void execByteArrayGenerator() throws Exception {
        runPipeline(new XMLGenerator(VALID_XML_STRING.getBytes()), VALID_XML_STRING);
    }

    @Test
    public void execFileGenerator() throws Exception {
        runPipeline(new XMLGenerator(createXMLFile(VALID_XML_STRING)), VALID_XML_STRING);
    }

    @Test
    public void execInputStreamGenerator() throws Exception {
        runPipeline(new XMLGenerator(new FileInputStream(createXMLFile(VALID_XML_STRING))), VALID_XML_STRING);
    }

    @Test
    public void execSAXBufferGenerator() throws Exception {
        Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
        pipeline.addComponent(new XMLGenerator(VALID_XML_STRING));
        final SAXBuffer saxBuffer = new SAXBuffer();
        pipeline.addComponent(new AbstractSAXSerializer() {

            @Override
            public void setup(Map<String, Object> inputParameters) {
                super.setup(inputParameters);
                this.contentHandler = saxBuffer;
            }
        });

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipeline.setup(baos);
        pipeline.execute();

        runPipeline(new XMLGenerator(saxBuffer), VALID_XML_STRING);
    }

    @Test
    public void execStringGenerator() throws Exception {
        runPipeline(new XMLGenerator(VALID_XML_STRING), VALID_XML_STRING);
    }

    @Test
    public void execURLGenerator() throws Exception {
        runPipeline(new XMLGenerator(createXMLFile(VALID_XML_STRING).toURL()), VALID_XML_STRING);
    }

    @Test
    public void execURLGeneratorBySeparatelyPassingURL() throws Exception {
        XMLGenerator generator = new XMLGenerator();

        Map<String, Object> configurationParams = new HashMap<String, Object>();
        configurationParams.put("source", createXMLFile(VALID_XML_STRING).toURL());
        generator.setConfiguration(configurationParams);

        runPipeline(generator, VALID_XML_STRING);
    }

    @Test(expected = ProcessingException.class)
    public void execURLGeneratorWithEmptySource() throws Exception {
        runPipeline(new XMLGenerator((URL) null), VALID_XML_STRING);
    }

    @Test(expected = ProcessingException.class)
    public void invalidExecByteArrayGenerator() throws Exception {
        runPipeline(new XMLGenerator(INVALID_XML_STRING.getBytes()), INVALID_XML_STRING);
    }

    @Test(expected = ProcessingException.class)
    public void invalidExecFileGenerator() throws Exception {
        runPipeline(new XMLGenerator(createXMLFile(INVALID_XML_STRING)), INVALID_XML_STRING);
    }

    @Test(expected = ProcessingException.class)
    public void invalidExecInputStreamGenerator() throws Exception {
        runPipeline(new XMLGenerator(new FileInputStream(createXMLFile(INVALID_XML_STRING))), INVALID_XML_STRING);
    }

    @Test(expected = ProcessingException.class)
    public void invalidExecStringGenerator() throws Exception {
        runPipeline(new XMLGenerator(INVALID_XML_STRING), INVALID_XML_STRING);
    }

    @Test(expected = SetupException.class)
    public void newStringGenerator() {
        new XMLGenerator((String) null);
    }

    @Test(expected = SetupException.class)
    public void testByteArrayGenerator() {
        new XMLGenerator((byte[]) null);
    }

    @Test(expected = SetupException.class)
    public void testFileGenerator() {
        new XMLGenerator((File) null);
    }

    @Test(expected = SetupException.class)
    public void testInputStreamGenerator() {
        new XMLGenerator((InputStream) null);
    }

    @Test(expected = SetupException.class)
    public void testSAXBufferGenerator() {
        new XMLGenerator((SAXBuffer) null);
    }

    public void testURLGenerator() {
        new XMLGenerator((URL) null);
    }

    private static File createXMLFile(String xmlString) throws IOException {
        File temp = File.createTempFile("cocoon", ".tmp");
        FileWriter writer = new FileWriter(temp);
        writer.write(xmlString);
        writer.flush();
        writer.close();
        return temp;
    }

    private static void runPipeline(SAXProducer generator, String expectedContent) throws Exception {
        Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
        pipeline.addComponent(generator);
        pipeline.addComponent(new XMLSerializer());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipeline.setup(baos);
        pipeline.execute();

        Assert.assertTrue(new Diff(expectedContent, baos.toString()).similar());
    }
}
