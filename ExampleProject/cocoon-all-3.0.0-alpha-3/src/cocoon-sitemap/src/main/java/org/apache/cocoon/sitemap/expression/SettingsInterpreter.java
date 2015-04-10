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
package org.apache.cocoon.sitemap.expression;

import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.sitemap.objectmodel.ObjectModel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Interpreter that will look up the properties with help of the spring configurator. This is the cocoon 2.2
 * correspondent of {global:...} input module.
 */
public class SettingsInterpreter implements LanguageInterpreter {

    @Autowired
    private Settings settings;

    public String resolve(String expression, ObjectModel objectModel) {
        Object result = null;

        try {
            result = this.settings.getProperty(expression);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result != null ? result.toString() : "";
    }

}
