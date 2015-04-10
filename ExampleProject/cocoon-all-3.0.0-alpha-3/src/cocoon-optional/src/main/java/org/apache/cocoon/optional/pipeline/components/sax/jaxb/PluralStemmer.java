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
package org.apache.cocoon.optional.pipeline.components.sax.jaxb;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of the plural name stemmer which
 * tests for some common English plural/singular patterns and
 * then uses a simple starts-with algorithm.
 */
final class PluralStemmer {

    private static final PluralStemmer INSTANCE = new PluralStemmer();

    public static PluralStemmer getInstance() {
        return INSTANCE;
    }

    private final Map<Pattern, String> rules = new LinkedHashMap<Pattern, String>();

    /**
     * This class can't be instantiated
     */
    private PluralStemmer() {
        // uncountable
        addIdentityRule("advice");
        addIdentityRule("air");
        addIdentityRule("alcohol");
        addIdentityRule("art");
        addIdentityRule("beef");
        addIdentityRule("blood");
        addIdentityRule("butter");
        addIdentityRule("cheese");
        addIdentityRule("chocolate");
        addIdentityRule("coffee");
        addIdentityRule("confusion");
        addIdentityRule("cotton");
        addIdentityRule("education");
        addIdentityRule("electricity");
        addIdentityRule("entertainment");
        addIdentityRule("equipment");
        addIdentityRule("experience");
        addIdentityRule("fiction");
        addIdentityRule("flour");
        addIdentityRule("food");
        addIdentityRule("forgiveness");
        addIdentityRule("furniture");
        addIdentityRule("gold");
        addIdentityRule("grass");
        addIdentityRule("ground");
        addIdentityRule("happiness");
        addIdentityRule("history");
        addIdentityRule("homework");
        addIdentityRule("honey");
        addIdentityRule("hope");
        addIdentityRule("ice");
        addIdentityRule("information");
        addIdentityRule("jam");
        addIdentityRule("juice");
        addIdentityRule("knowledge");
        addIdentityRule("lamb");
        addIdentityRule("lightning");
        addIdentityRule("literature");
        addIdentityRule("love");
        addIdentityRule("luck");
        addIdentityRule("luggage");
        addIdentityRule("meat");
        addIdentityRule("milk");
        addIdentityRule("mist");
        addIdentityRule("money");
        addIdentityRule("music");
        addIdentityRule("news");
        addIdentityRule("noise");
        addIdentityRule("oil");
        addIdentityRule("oxygen");
        addIdentityRule("paper");
        addIdentityRule("patience");
        addIdentityRule("pay");
        addIdentityRule("peace");
        addIdentityRule("pepper");
        addIdentityRule("petrol");
        addIdentityRule("plastic");
        addIdentityRule("pork");
        addIdentityRule("power");
        addIdentityRule("pressure");
        addIdentityRule("rain");
        addIdentityRule("rice");
        addIdentityRule("sadness");
        addIdentityRule("salt");
        addIdentityRule("sand");
        addIdentityRule("shopping");
        addIdentityRule("silver");
        addIdentityRule("snow");
        addIdentityRule("space");
        addIdentityRule("speed");
        addIdentityRule("steam");
        addIdentityRule("sugar");
        addIdentityRule("sunshine");
        addIdentityRule("tea");
        addIdentityRule("tennis");
        addIdentityRule("time");
        addIdentityRule("toothpaste");
        addIdentityRule("traffic");
        addIdentityRule("trousers");
        addIdentityRule("vinegar");
        addIdentityRule("water");
        addIdentityRule("weather");
        addIdentityRule("wine");
        addIdentityRule("wood");
        addIdentityRule("wool");
        addIdentityRule("work");

        // irregular
        addRule("person", "people");
        addRule("man", "men");
        addRule("child", "children");
        addRule("sex", "sexes");
        addRule("move", "moves");

        // rules for plural
        addRule("(ax|test)is$", "$1es");
        addRule("(octop|vir)us$", "$1i");
        addRule("(alias|status)$", "$1es");
        addRule("(bu)s$", "$1ses");
        addRule("(buffal|tomat)o$", "$1oes");
        addRule("([ti])um$", "$1a");
        addRule("sis$", "ses");
        addRule("(?:([^f])fe|([lr])f)$", "$1$2ves");
        addRule("(hive)$", "$1s");
        addRule("([^aeiouy]|qu)y$", "$1ies");
        addRule("(x|ch|ss|sh)$", "$1es"); 
        addRule("(matr|vert|ind)(?:ix|ex)$", "$1ices");
        addRule("([m|l])ouse$", "$1ice");
        addRule("^(ox)$", "$1en");
        addRule("(quiz)$", "$1zes");

        // normal pattern
        addRule("s$", "s");
        addRule("$", "s");
    }

    private void addIdentityRule(String pattern) {
        addRule(pattern, "$0");
    }

    private void addRule(String pattern, String rule) {
        rules.put(compile(pattern, CASE_INSENSITIVE), rule);
    }

    /**
     * Algorithm that supports common English plural patterns to 'pluralize' names.
     *
     * If no matches are found then - if one exists - a property starting with the
     * singular name will be returned.
     *
     * @param xmlRootElementName
     * @return
     */
    public String toPlural(final String xmlRootElementName) {
        for (Entry<Pattern, String> replacer : rules.entrySet()) {
            Matcher matcher = replacer.getKey().matcher(xmlRootElementName);
            if (matcher.find()) {
                return matcher.replaceFirst(replacer.getValue());
            }
        }

        return xmlRootElementName;
    }

}
