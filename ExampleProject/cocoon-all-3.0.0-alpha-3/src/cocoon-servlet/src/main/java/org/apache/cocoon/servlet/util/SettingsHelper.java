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
package org.apache.cocoon.servlet.util;

import java.util.Map;

import org.apache.cocoon.configuration.Settings;

public class SettingsHelper {

    private static final String SETTINGS_KEY = Settings.class.getName();

    public static Settings getSettings(Map<String, ? extends Object> parameters) {
        Object parameter = parameters.get(SETTINGS_KEY);
        if (parameter instanceof Settings) {
            return (Settings) parameter;
        }

        throw new IllegalStateException(
                "A Settings object is not available. This might indicate that Cocoon 3 doesn't run on top of the Cocoon Spring Configurator.");
    }

    public static void storeSettings(Settings settings, Map<String, Object> parameters) {
        parameters.put(SETTINGS_KEY, settings);
    }
}
