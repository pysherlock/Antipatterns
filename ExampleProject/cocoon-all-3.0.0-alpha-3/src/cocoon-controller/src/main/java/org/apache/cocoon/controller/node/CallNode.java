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
package org.apache.cocoon.controller.node;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.cocoon.sitemap.Invocation;
import org.apache.cocoon.sitemap.node.AbstractSitemapNode;
import org.apache.cocoon.sitemap.node.InvocationResult;
import org.apache.cocoon.sitemap.node.Node;
import org.apache.cocoon.sitemap.node.annotations.Parameter;

@Node(name = "call")
public class CallNode extends AbstractSitemapNode {

    @Parameter
    private String wrapperType = "default";

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.node.SitemapNode#getType()
     */
    public Class<?> getType() {
        return CallNode.class;
    }

    @Override
    public InvocationResult invoke(Invocation invocation) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        for (Entry<String, String> entry : this.getParameters().entrySet()) {
            String resolvedValue = invocation.resolveParameter(entry.getValue());
            parameters.put(entry.getKey(), resolvedValue);
        }

        parameters.put("baseUrl", invocation.resolve(""));

        invocation.installComponent("controller:" + this.wrapperType, parameters);
        return InvocationResult.COMPLETED;
    }
}
