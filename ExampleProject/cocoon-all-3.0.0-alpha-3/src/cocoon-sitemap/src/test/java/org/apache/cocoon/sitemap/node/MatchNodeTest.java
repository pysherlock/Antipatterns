/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.sitemap.node;

import static junit.framework.Assert.*;

import java.util.Map;

import junitx.util.PrivateAccessor;

import org.apache.cocoon.sitemap.node.MatchNode;
import org.apache.cocoon.sitemap.node.MatchNode.MatcherContext;
import org.junit.Test;

public class MatchNodeTest {

    @Test
    public void endsWithMatchingAttribute() throws Exception {
        MatchNode matchNode = new MatchNode();
        PrivateAccessor.setField(matchNode, "endsWith", ".xml");
        MatcherContext matcherContext = matchNode.lookupMatcherContext();
        Map<String, String> matches = matcherContext.match("abc.xml");
        assertTrue(matches.containsValue("abc.xml"));
        assertTrue(matches.containsValue("abc"));
        assertEquals(2, matches.size());
    }

    @Test
    public void startsWithMatchingAttribute() throws Exception {
        MatchNode matchNode = new MatchNode();
        PrivateAccessor.setField(matchNode, "startsWith", "abc");
        MatcherContext matcherContext = matchNode.lookupMatcherContext();
        Map<String, String> matches = matcherContext.match("abc/44");
        assertTrue(matches.containsValue("abc/44"));
        assertTrue(matches.containsValue("/44"));
        assertEquals(2, matches.size());
    }

    @Test
    public void regexpMatchingAttribute() throws Exception {
        MatchNode matchNode = new MatchNode();
        PrivateAccessor.setField(matchNode, "regexp", "([a-zA-Z\\-]+)/(.*)");
        MatcherContext matcherContext = matchNode.lookupMatcherContext();
        Map<String, String> matches = matcherContext.match("abc/44");
        assertTrue(matches.containsValue("abc/44"));
        assertTrue(matches.containsValue("abc"));
        assertTrue(matches.containsValue("44"));
        assertEquals(3, matches.size());
    }

    @Test
    public void regexpMatchingAttributeWithSlash() throws Exception {
        MatchNode matchNode = new MatchNode();
        PrivateAccessor.setField(matchNode, "regexp", "([a-zA-Z\\-]+)/(.*)");
        MatcherContext matcherContext = matchNode.lookupMatcherContext();
        Map<String, String> matches = matcherContext.match("abc/44");
        assertTrue(matches.containsValue("abc/44"));
        assertTrue(matches.containsValue("abc"));
        assertTrue(matches.containsValue("44"));
        assertEquals(3, matches.size());
    }

    @Test
    public void containsMatchingAttribute() throws Exception {
        MatchNode matchNode = new MatchNode();
        PrivateAccessor.setField(matchNode, "contains", "123");
        MatcherContext matcherContext = matchNode.lookupMatcherContext();
        Map<String, String> matches = matcherContext.match("000123456");
        assertTrue(matches.containsValue("000123456"));
        assertEquals(1, matches.size());
    }

    @Test
    public void equalsMatchingAttribute() throws Exception {
        MatchNode matchNode = new MatchNode();
        PrivateAccessor.setField(matchNode, "equals", "123");
        MatcherContext matcherContext = matchNode.lookupMatcherContext();
        Map<String, String> matches = matcherContext.match("123");
        assertTrue(matches.containsValue("123"));
        assertEquals(1, matches.size());
    }

    @Test
    public void wildcardMatchingPathAttribute() throws Exception {
        MatchNode matchNode = new MatchNode();
        PrivateAccessor.setField(matchNode, "wildcard", "abc/*/*");
        MatcherContext matcherContext = matchNode.lookupMatcherContext();
        Map<String, String> matches = matcherContext.match("abc/def/ghi");
        assertTrue(matches.containsValue("abc/def/ghi"));
        assertTrue(matches.containsValue("def"));
        assertTrue(matches.containsValue("ghi"));
        assertEquals(3, matches.size());
    }

    @Test
    public void wildcardMatchingPathAttributeWithSlash() throws Exception {
        MatchNode matchNode = new MatchNode();
        PrivateAccessor.setField(matchNode, "wildcard", "abc/*/*");
        MatcherContext matcherContext = matchNode.lookupMatcherContext();
        Map<String, String> matches = matcherContext.match("abc/def/ghi");
        assertTrue(matches.containsValue("abc/def/ghi"));
        assertTrue(matches.containsValue("def"));
        assertTrue(matches.containsValue("ghi"));
        assertEquals(3, matches.size());
    }

    @Test
    public void noMatchingAttribute() {
        MatchNode matchNode = new MatchNode();
        MatcherContext matcherContext = matchNode.lookupMatcherContext();
        assertNull("If there is no match attribute, no matcher can be found.", matcherContext);
    }

    @Test
    public void moreThanOneMatchingAttribute() throws Exception {
        MatchNode matchNode = new MatchNode();
        PrivateAccessor.setField(matchNode, "pattern", "123");
        PrivateAccessor.setField(matchNode, "equals", "123");
        try {
            matchNode.lookupMatcherContext();
            fail();
        } catch (Exception e) {
            // expected
        }
    }

}
