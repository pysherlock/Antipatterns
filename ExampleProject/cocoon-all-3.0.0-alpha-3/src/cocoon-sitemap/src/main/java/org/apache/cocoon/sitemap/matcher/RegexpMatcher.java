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

public class RegexpMatcher implements Matcher {

    private static Map<String, Pattern> patterns = new HashMap<String, Pattern>();

    public Map<String, String> match(String expression, String testValue) {
        if (testValue == null) {
            return null;
        }

        Pattern pattern = patterns.get(expression);
        if (pattern == null) {
            pattern = Pattern.compile(expression);
            patterns.put(expression, pattern);
        }

        java.util.regex.Matcher matcher = null;
        if (testValue.startsWith("/")) {
            matcher = pattern.matcher(testValue.substring(1));
        } else {
            matcher = pattern.matcher(testValue);
        }

        if (matcher.matches()) {
            Map<String, String> result = new HashMap<String, String>();
            for (int i = 0; i <= matcher.groupCount(); i++) {
                result.put(Integer.toString(i), matcher.group(i));
            }
            return result;
        }

        return null;
    }

}
