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
import org.apache.cocoon.sitemap.node.PipelineNode;

public class PipelineNodeProfiler extends Profiler<PipelineNode> {

    public PipelineNodeProfiler() {
        super(PipelineNode.class);
    }

    @ProfileMethod(name = "invoke")
    public void beforeInvoke(ProfilingData data, PipelineNode component, Object[] args) {
        try {
            Field field = PipelineNode.class.getDeclaredField("type");

            field.setAccessible(true);
            String value = (String) field.get(component);

            if (value != null) {
                data.addData("type", value);

                String simpleName = component.getClass().getSimpleName();
                data.setDisplayName(String.format("%s (%s=%s)", simpleName, "type", value));
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading type from PipelineNode", e);
        }
    }
}
