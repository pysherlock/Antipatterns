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
package org.apache.cocoon.sitemap.matcher;

import java.util.Map;

import junit.framework.Assert;

import org.apache.cocoon.sitemap.matcher.WildcardMatcher;
import org.apache.cocoon.sitemap.matcher.WildcardMatcher.UnsupportedNamedWildcardExpressionException;
import org.apache.cocoon.sitemap.matcher.WildcardMatcher.WildcardExpressionRewriter;
import org.junit.Test;

public class WildcardMatcherTest {

    @Test
    public void testMatcher() {
        WildcardMatcher matcher = new WildcardMatcher();
        Map<String, String> result = matcher.match("*/{name}/**/{id}", "abc/def/ghi/jkl/5");
        Assert.assertEquals("A parameter 'name' is expected.", "def", result.get("name"));
        Assert.assertEquals("A parameter 'id' is expected.", "5", result.get("id"));
        Assert.assertEquals("A result value '1' is expected.", "abc", result.get("1"));
        Assert.assertEquals("A result value '2' is expected.", "def", result.get("2"));
        Assert.assertEquals("A result value '3' is expected.", "ghi/jkl", result.get("3"));
        Assert.assertEquals("A result value '4' is expected.", "5", result.get("4"));
        Assert.assertEquals("A result value '0' is expected.", "abc/def/ghi/jkl/5", result.get("0"));
    }

    @Test
    public void testMatcherWithoutParamters() {
        WildcardMatcher matcher = new WildcardMatcher();
        Map<String, String> result = matcher.match("*/**", "abc/def/ghi/jkl/5");
        Assert.assertEquals("A result value '1' is expected.", "abc", result.get("1"));
        Assert.assertEquals("A result value '2' is expected.", "def/ghi/jkl/5", result.get("2"));
        Assert.assertEquals("A result value '0' is expected.", "abc/def/ghi/jkl/5", result.get("0"));
    }

    @Test
    public void testMatcherWithoutWildcards() {
        WildcardMatcher matcher = new WildcardMatcher();
        Map<String, String> result = matcher.match("abc", "abc");
        Assert.assertNull("No matching result", result.get("1"));
        Assert.assertEquals("A result value '0' is expected.", "abc", result.get("0"));
    }

    @Test
    public void testFindParameterNames() {
        WildcardExpressionRewriter rewriter = new WildcardExpressionRewriter("abc/{name}/*/{id}");
        Map<String, String> parameters = rewriter.getParameters();
        Assert.assertEquals("A parameter 'name' is expected.", "name", parameters.get("1"));
        Assert.assertEquals("A parameter 'id' is expected.", "id", parameters.get("3"));
    }

    @Test
    public void testRewritingExpression() {
        WildcardExpressionRewriter rewriter = new WildcardExpressionRewriter("abc/{name}/*/{id}.html");
        Assert.assertEquals("abc/*/*/*.html", rewriter.getRewrittenExpression());
    }

    @Test
    public void testWrongNamedParameters() {
        try {
            new WildcardMatcher().match("*/{name}{id}", "abc/def/ghi");
            Assert.fail("}{ is not allowed without some character in between "
                    + "because this would be translated into a '**' which has a different meaning");
        } catch (UnsupportedNamedWildcardExpressionException e) {
            // expected
        }

    }
}
