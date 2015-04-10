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

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class ManifestUtils {

    public static String getAttribute(Class<?> c, String property) throws IOException {
        URL codeBase = c.getProtectionDomain().getCodeSource().getLocation();
        if (codeBase.getPath().endsWith(".jar")) {
            JarInputStream jin = null;
            try {
                jin = new JarInputStream(codeBase.openStream());
                Manifest mf = jin.getManifest();
                Map<Object, Object> entries = mf.getMainAttributes();
                for (Iterator<Object> it = entries.keySet().iterator(); it.hasNext();) {
                    Attributes.Name key = (Attributes.Name) it.next();
                    String keyName = key.toString();
                    if (property.equals(keyName)) {
                        String value = (String) entries.get(key);
                        return value;
                    }
                }
            } finally {
                jin.close();
            }
        }

        return null;
    }
}
