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
package org.apache.cocoon.sax.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.cocoon.sax.util.TransformationUtils;
import org.custommonkey.xmlunit.Diff;

public class TransformationUtilsTest extends TestCase {

    public void testStringTransformation() throws Exception {
        String result = TransformationUtils.transform("<x>test</x>", this.getClass().getClassLoader().getResource("test.xslt"));
        Diff diff = new Diff("<?xml version=\"1.0\" encoding=\"UTF-8\"?><p></p>", result);
        assertTrue("XSL transformation didn't work as expected " + diff, diff.identical());
    }

    public void testStringTransformationWithParameter() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("myParam", "abc");
        String result = TransformationUtils
                .transform("<x>test</x>", params, this.getClass().getClassLoader().getResource("test.xslt"));
        Diff diff = new Diff("<?xml version=\"1.0\" encoding=\"UTF-8\"?><p>abc</p>", result);
        assertTrue("XSL transformation didn't work as expected " + diff, diff.identical());
        assertTrue(result.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
    }

    public void testStringTransformationWithProperties() throws Exception {
        Properties props = new Properties();
        props.put("encoding", "iso-8859-1");
        String result = TransformationUtils.transform("<x>test</x>", props, this.getClass().getClassLoader().getResource("test.xslt"));
        Diff diff = new Diff("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><p/>", result);
        assertTrue("XSL transformation didn't work as expected " + diff, diff.identical());
        assertTrue(result.startsWith("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"));
    }

    public void testStringTransformationWithPropertiesAndParameters() throws Exception {
        Properties props = new Properties();
        props.put("encoding", "iso-8859-1");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("myParam", "abc");
        String result = TransformationUtils.transform("<x>test</x>", params, props, this.getClass().getClassLoader().getResource(
                "test.xslt"));
        Diff diff = new Diff("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><p>abc</p>", result);
        assertTrue("XSL transformation didn't work as expected " + diff, diff.identical());
    }
}
