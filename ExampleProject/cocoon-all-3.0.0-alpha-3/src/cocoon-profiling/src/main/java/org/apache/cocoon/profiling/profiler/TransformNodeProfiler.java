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
package org.apache.cocoon.profiling.profiler;

import java.lang.reflect.Field;

import org.apache.cocoon.profiling.ProfileMethod;
import org.apache.cocoon.profiling.data.ProfilingData;
import org.apache.cocoon.sitemap.node.TransformNode;

public class TransformNodeProfiler extends Profiler<TransformNode> {

    public TransformNodeProfiler() {
        super(TransformNode.class);
    }

    @ProfileMethod(name = "invoke")
    public void beforeInvoke(ProfilingData data, TransformNode component, Object[] args) {
        try {
            Field srcField = TransformNode.class.getDeclaredField("src");

            srcField.setAccessible(true);
            String src = (String) srcField.get(component);

            if (src != null) {
                data.addData("src", src);
            }

            Field typeField = TransformNode.class.getDeclaredField("type");

            typeField.setAccessible(true);
            String type = (String) typeField.get(component);

            if (type != null) {
                data.addData("type", type);

                String simpleName = component.getClass().getSimpleName();
                data.setDisplayName(String.format("%s (%s=%s)", simpleName, "type", type));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading private fields from TransformNode", e);
        }
    }
}
