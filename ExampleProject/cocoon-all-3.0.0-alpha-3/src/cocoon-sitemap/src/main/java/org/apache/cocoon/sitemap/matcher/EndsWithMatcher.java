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

public class EndsWithMatcher implements Matcher {

    public Map<String, String> match(String expression, String testValue) {
        if (testValue == null) {
            return null;
        }

        if (testValue.endsWith(expression)) {
            Map<String, String> result = new HashMap<String, String>();
            result.put("0", testValue);
            String left = testValue.substring(0, testValue.length() - expression.length());
            result.put("1", left);
            return result;
        }

        return null;
    }

}
