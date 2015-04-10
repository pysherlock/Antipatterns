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
package org.apache.cocoon.sitemap.node;

import java.util.LinkedList;
import java.util.Map;

import org.apache.cocoon.sitemap.Invocation;
import org.apache.cocoon.sitemap.matcher.ContainsMatcher;
import org.apache.cocoon.sitemap.matcher.EndsWithMatcher;
import org.apache.cocoon.sitemap.matcher.EqualsMatcher;
import org.apache.cocoon.sitemap.matcher.Matcher;
import org.apache.cocoon.sitemap.matcher.RegexpMatcher;
import org.apache.cocoon.sitemap.matcher.StartsWithMatcher;
import org.apache.cocoon.sitemap.matcher.WildcardMatcher;
import org.apache.cocoon.sitemap.node.annotations.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Node(name = "match")
public class MatchNode extends AbstractSitemapNode {

    private final Log logger = LogFactory.getLog(this.getClass());

    @Parameter
    private String name;

    @Parameter
    private String value;

    @Parameter
    private String pattern;

    @Parameter
    private String regexp;

    @Parameter
    private String equals;

    @Parameter
    private String contains;

    @Parameter
    private String wildcard;

    @Parameter
    private String startsWith;

    @Parameter
    private String endsWith;

    private MatcherContext matcherContext;

    private Map<String, String> matches;

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.node.AbstractSitemapNode#invoke(org.apache.cocoon.sitemap.Invocation)
     */
    @Override
    public InvocationResult invoke(Invocation invocation) {
        // try to resolve the test value in the case that it is an expression
        String testValue = invocation.resolveParameter(this.value);
        if (testValue == null) {
            testValue = invocation.getRequestURI();
            if (testValue.startsWith("/")) {
                testValue = testValue.substring(1);
            }
        }

        // create the matching context based on the used matching attributes (regexp, equals, etc.)
        this.matcherContext = this.lookupMatcherContext();
        if (this.matcherContext == null) {
            throw new MatchingAttributeException("Use on of the matching attributes: "
                    + "wildcard, equals, regexp, starts-with, ends-with, contains");
        }

        this.matches = this.matcherContext.match(testValue);
        if (this.matches == null) {
            // do not ask the children, there was no match
            return InvocationResult.NONE;
        }

        // invoke the child nodes
        invocation.pushSitemapParameters(this.name, this.matches);
        InvocationResult invocationResult = super.invoke(invocation);
        invocation.popSitemapParameters();

        // check if there has been some processing in the child nodes
        if (invocationResult.isContinued()) {
            // stop here if the invocation contains a complete pipeline
            if (invocation.hasCompletePipeline()) {
                return InvocationResult.COMPLETED;
            }

            // continue with the next sibling node
            return InvocationResult.CONTINUE;
        }

        // although this match node has matched, there was no processing in the child nodes
        return InvocationResult.NONE;
    }

    protected boolean isMatching() {
        return this.matches != null;
    }

    /**
     * Find out what matching attribute (pattern, wildcard, equals, etc.) is used, check that it's not more than one
     * that is used and throw an exception otherwise.
     */
    protected MatcherContext lookupMatcherContext() {
        // determine the matching type and check if there are conflicting match attributes
        LinkedList<MatcherContext> matcherContextList = new LinkedList<MatcherContext>();
        if (this.pattern != null) {
            matcherContextList.add(new MatcherContext(new WildcardMatcher(), this.pattern));
        }
        if (this.regexp != null) {
            matcherContextList.add(new MatcherContext(new RegexpMatcher(), this.regexp));
        }
        if (this.equals != null) {
            matcherContextList.add(new MatcherContext(new EqualsMatcher(), this.equals));
        }
        if (this.contains != null) {
            matcherContextList.add(new MatcherContext(new ContainsMatcher(), this.contains));
        }
        if (this.wildcard != null) {
            matcherContextList.add(new MatcherContext(new WildcardMatcher(), this.wildcard));
        }
        if (this.startsWith != null) {
            matcherContextList.add(new MatcherContext(new StartsWithMatcher(), this.startsWith));
        }
        if (this.endsWith != null) {
            matcherContextList.add(new MatcherContext(new EndsWithMatcher(), this.endsWith));
        }
        if (matcherContextList.size() > 1) {
            String message = "Only one matching attribute (regexp, equals, contains, wildcard, pattern) can be set: "
                    + matcherContextList;
            this.logger.error(message);
            throw new MatchingAttributeException(message);
        }
        return matcherContextList.isEmpty() ? null : matcherContextList.getFirst();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.node.SitemapNode#getType()
     */
    public Class<?> getType() {
        return MatchNode.class;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    protected static class MatcherContext {

        private static final Log LOG = LogFactory.getLog(MatcherContext.class);

        private Matcher matcher;
        private String expression;

        public MatcherContext(Matcher matcher, String expression) {
            super();
            this.matcher = matcher;
            this.expression = expression;
        }

        public Map<String, String> match(String testValue) {
            Map<String, String> result = this.matcher.match(this.expression, testValue);

            if (LOG.isDebugEnabled()) {
                String message = "Matching: expression=" + this.expression + ", testValue=" + testValue + ", result="
                        + result;
                LOG.debug(message);
            }

            return result;
        }

        @Override
        public String toString() {
            return "matcher=" + this.matcher + ", expression=" + this.expression;
        }
    }

    private static class MatchingAttributeException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public MatchingAttributeException(String message) {
            super(message);
        }
    }
}