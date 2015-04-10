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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.sax.AbstractSAXTransformer;
import org.apache.cocoon.sax.SAXPipelineComponent;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * $Id: XIncludeTransformerTest.java 907774 2010-02-08 19:49:59Z simonetripodi $
 */
public final class XIncludeTransformerTest {

    @BeforeClass
    public static void setUp() {
        XMLUnit.setIgnoreWhitespace(true);
    }

    /**
     * A pipeline that reads from a file and perform a simple XInclude operation.
     */
     @Test
     public void testPipelineWithXInclude() throws Exception {
         this.internalXIncludeTest("xinclude-xml.xml",
             "<?xml version=\"1.0\" encoding=\"UTF-8\"?><x><test/></x>");
     }

    /**
     * A pipeline that performs an XInclude operation, including just text.
     **/
     @Test
     public void testPipelineWithXIncludeText() throws Exception {
         this.internalXIncludeTest("xinclude-text-only.xml",
             "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                 + "<x>in girum imus nocte et cosumimur igni</x>");
     }

    /**
     * A pipeline that performs an XInclude operation, forced to use the fallback.
     **/
     @Test
     public void testPipelineWithXIncludeFallback() throws Exception {
         this.internalXIncludeTest("xinclude-fallback.xml",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><x>"
                 + "<error>the linked document has not found</error></x>");
     }

    /**
     * A pipeline that performs an XInclude operation and use XPointer to extract
     * a fragment from the included document.
     **/
    @Test
    public void testPipelineWithXIncludeAndXPointer() throws Exception {
        this.internalXIncludeTest("xinclude-xpointer.xml",
               "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<x xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"><p>"
                + "<xsl:value-of select=\"$myParam\"/></p></x>");
    }

    /**
     * A pipeline that performs an XInclude operation and use the deprecated
     * XPointer to extract a fragment from the included document.
     **/
    @Test
    public void testPipelineWithXIncludeAndDeprecatedXPointer() throws Exception {
         this.internalXIncludeTest("xinclude-deprecated_xpointer.xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                 + "<x xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"><p>"
                 + "<xsl:value-of select=\"$myParam\"/></p></x>");
    }

    /**
     * A pipeline that performs an XInclude operation and use the shorthand
     * XPointer to extract a fragment from the included document.
     **/
    @Test
    public void testPipelineWithShorthandXPointerPart() throws Exception {
        this.internalXIncludeTest("xinclude-shorthand.xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><x>"
                + "<url id=\"url\">http://www.opensource.org/licenses/apache2.0.php</url>"
                + "<url id=\"url\">http://www.opensource.org/licenses/lgpl-3.0.html</url>"
                + "<url id=\"url\">http://www.opensource.org/licenses/mit-license.php</url>"
                + "<url id=\"url\">http://www.opensource.org/licenses/W3C.php</url></x>");
    }

    /**
     * Test that the number of startPrefixMapping and endPrefixMapping events is the same.
     */
    @Test
    public void testForPrefixMappingSymmetry() throws Exception {
        URL base = this.getClass().getResource("/");
        URL source = new URL(base, "multiple-prefixes.xml");

        final List<Integer> countCollector = new ArrayList<Integer>(1);
        countCollector.add(0);

        Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
        pipeline.addComponent(new XMLGenerator(source));
        pipeline.addComponent(new XIncludeTransformer(base));
        pipeline.addComponent(new AbstractSAXTransformer(){
            public void startPrefixMapping(String prefix, String url) {
                countCollector.add(countCollector.remove(0).intValue() + 1);
            }

            public void endPrefixMapping(String prefix) {
                countCollector.add(countCollector.remove(0).intValue() - 1);
            }
        });
        pipeline.addComponent(new XMLSerializer());
        pipeline.setup(new ByteArrayOutputStream());
        pipeline.execute();
        assertEquals("startPrefixMapping and endPrefixMapping not symmetric: ", 0, countCollector.get(0).intValue());
    }

    /**
     * Test that only one each of startDocument and endDocument events are produced.
     */
    @Test
    public void testExtraDocumentEvents() throws Exception {
        URL base = this.getClass().getResource("/");
        URL source = new URL(base, "multiple-prefixes.xml");

        final List<Integer> startCollector = new ArrayList<Integer>(1);
        startCollector.add(0);

        final List<Integer> endCollector = new ArrayList<Integer>(1);
        endCollector.add(0);

        Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
        pipeline.addComponent(new XMLGenerator(source));
        pipeline.addComponent(new XIncludeTransformer(base));
        pipeline.addComponent(new AbstractSAXTransformer(){

            public void startDocument() {
                startCollector.add(startCollector.remove(0).intValue() + 1);
            }

            public void endDocument() {
                endCollector.add(endCollector.remove(0).intValue() + 1);
            }
        });
        pipeline.addComponent(new XMLSerializer());
        pipeline.setup(new ByteArrayOutputStream());
        pipeline.execute();
        assertEquals("extra startDocument event: ", 1, startCollector.get(0).intValue());
        assertEquals("extra endDocument event: ", 1, endCollector.get(0).intValue());
    }

   /**
    *
    */
   private void internalXIncludeTest(final String testResource, final String expectedDocument) throws Exception {
       URL base = this.getClass().getResource("/");
       URL source = new URL(base, testResource);

       Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
       pipeline.addComponent(new XMLGenerator(source));
       pipeline.addComponent(new XIncludeTransformer(base));
       pipeline.addComponent(new XMLSerializer());

       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       pipeline.setup(baos);
       pipeline.execute();

       String actualDocument = new String(baos.toByteArray());

       Diff diff = new Diff(expectedDocument, actualDocument);
       assertTrue("XInclude transformation didn't work as expected " + diff,
               diff.identical());
   }

}
