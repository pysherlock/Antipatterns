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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.cocoon.sitemap.util.WildcardMatcherHelper;

public class WildcardMatcher implements Matcher {

    public Map<String, String> match(String expression, String testValue) {
        if (testValue == null) {
            return null;
        }

        WildcardExpressionRewriter rewriter = new WildcardExpressionRewriter(expression);
        Map<String, String> matchingResult = WildcardMatcherHelper.match(rewriter.rewrittenExpression, testValue);
        Map<String, String> parameterGroups = rewriter.getParameters();

        if (matchingResult == null) {
            return null;
        }

        Map<String, String> result = new HashMap<String, String>();
        for (String key : matchingResult.keySet()) {
            String value = matchingResult.get(key);
            result.put(key, value);
            String param = parameterGroups.get(key);
            if (param != null) {
                result.put(param, value);
            }
        }

        return result;
    }

    /**
     * The {@link WildcardExpressionRewriter} takes an expression like /abc/{id}/{name}.html and replaces all
     * placeholders by stars ('*') so that the WildcardHelper can be used. It also provides a list of all results so
     * that the WildcardHelper results can be matched with the parameters set in the expression.
     */
    protected static class WildcardExpressionRewriter {

        private static final Pattern PARAMETER_PATTERN = Pattern.compile("(\\{[\\w\\.\\-]+})");

        private static final Pattern PARAMETER_WILDCARD_PATTERN = Pattern.compile("([\\*]{1,2}|\\{([\\w\\.\\-]+)})");

        private String originalExpression;

        private java.util.regex.Matcher parameterAndWildcardMatcher;

        private Map<String, String> parameters;

        private java.util.regex.Matcher parameterMatcher;

        private String rewrittenExpression;

        public WildcardExpressionRewriter(String originalExpression) {
            super();
            this.originalExpression = originalExpression;
            this.checkExpression();

            this.parameterAndWildcardMatcher = PARAMETER_WILDCARD_PATTERN.matcher(originalExpression);
            this.parameterMatcher = PARAMETER_PATTERN.matcher(originalExpression);
            this.parameters = this.findParameters();
            this.rewrittenExpression = this.rewriteExpression();
        }

        public String getOriginalExpression() {
            return this.originalExpression;
        }

        public Map<String, String> getParameters() {
            return this.parameters;
        }

        public int getParameterPosition(String parameterName) {
            return 1;
        }

        public String getRewrittenExpression() {
            return this.rewrittenExpression;
        }

        private void checkExpression() {
            if (this.originalExpression.contains("}{")) {
                throw new UnsupportedNamedWildcardExpressionException("}{ is not allowed (" + this.originalExpression
                        + "). There must be a seperating character between two named wildcards.");
            }
        }

        private Map<String, String> findParameters() {
            Map<String, String> parameterNames = new HashMap<String, String>();

            int count = 1;
            while (true) {
                if (this.parameterAndWildcardMatcher.find()) {
                    String group2 = this.parameterAndWildcardMatcher.group(2);
                    if (group2 != null) {
                        parameterNames.put(Integer.toString(count), group2);
                    }
                } else {
                    break;
                }

                count++;
            }

            return parameterNames;
        }

        private String rewriteExpression() {
            return this.parameterMatcher.replaceAll("*");
        }
    }

    public static class UnsupportedNamedWildcardExpressionException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public UnsupportedNamedWildcardExpressionException(String msg) {
            super(msg);
        }
    }
}
