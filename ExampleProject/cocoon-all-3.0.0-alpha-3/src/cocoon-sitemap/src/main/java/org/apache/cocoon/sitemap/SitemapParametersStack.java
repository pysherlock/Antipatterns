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
package org.apache.cocoon.sitemap;

import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SitemapParametersStack {

    private static final Pattern ABSOLUTE_PARAMETER_PATTERN = Pattern.compile("([a-zA-z0-9]+)/(.+)");
    private static final String RELATIVE_LOCATION_PREFIX = "../";
    private static final Pattern RELATIVE_PARAMETER_PATTERN = Pattern.compile("(("
            + Pattern.quote(RELATIVE_LOCATION_PREFIX) + ")*)(.+)");

    private Stack<Entry> entries = new Stack<Entry>();

    public String getParameter(String parameterName) {

        Matcher absoluteParameterMatcher = ABSOLUTE_PARAMETER_PATTERN.matcher(parameterName);
        if (absoluteParameterMatcher.matches()) {
            return this.resolveAbsoluteParameter(parameterName, absoluteParameterMatcher);
        }

        Matcher relativeParameterMatcher = RELATIVE_PARAMETER_PATTERN.matcher(parameterName);
        if (relativeParameterMatcher.matches()) {
            return this.resolveRelativeParameter(relativeParameterMatcher);
        }

        throw new IllegalArgumentException("Sitemap parameter '" + parameterName + "' is invalid. Valid formats are: '"
                + ABSOLUTE_PARAMETER_PATTERN.pattern() + "' or '" + RELATIVE_PARAMETER_PATTERN.pattern() + "'");
    }

    public void popParameters() {
        this.entries.pop();
    }

    public void pushParameters(String name, Map<String, ? extends Object> parameters) {
        this.entries.push(new Entry(name, parameters));
    }

    private String resolveAbsoluteParameter(String parameterName, Matcher absoluteParameterMatcher) {
        final String entryName = absoluteParameterMatcher.group(1);
        final String name = absoluteParameterMatcher.group(2);

        for (Entry entry : this.entries) {
            if (entryName.equals(entry.getName())) {
                Object result = entry.getParameter(name);
                if (result == null) {
                    return null;
                }

                return result.toString();
            }
        }

        throw new IllegalArgumentException("Sitemap parameter '" + parameterName
                + "' could not be resolved. There was no entry for the name '" + entryName + "'");
    }

    private String resolveRelativeParameter(Matcher relativeParameterMatcher) {
        final String levelPrefix = relativeParameterMatcher.group(2);
        final String name = relativeParameterMatcher.group(3);
        final int level;
        if (levelPrefix == null) {
            level = 0;
        } else {
            level = levelPrefix.length() / RELATIVE_LOCATION_PREFIX.length();
        }

        final int index = this.entries.size() - level - 1;
        if (index < 0) {
            throw new IllegalArgumentException("Sitemap parameter '" + relativeParameterMatcher.group()
                    + "' could not be resolved. There are only " + this.entries.size() + " entries available, but "
                    + level + " entries were requested.");
        }

        Entry entry = this.entries.get(index);
        Object result = entry.getParameter(name);
        if (result == null) {
            return null;
        }

        return result.toString();
    }

    private class Entry {

        private String name;
        private Map<String, ? extends Object> parameters;

        public Entry(String name, Map<String, ? extends Object> parameters) {
            super();
            this.name = name;
            this.parameters = parameters;
        }

        public String getName() {
            return this.name;
        }

        public Object getParameter(String parameterName) {
            return this.parameters.get(parameterName);
        }
    }
}
