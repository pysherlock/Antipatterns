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
package org.apache.cocoon.sitemap.node;

import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.sitemap.Invocation;
import org.apache.cocoon.sitemap.node.annotations.Parameter;

@Node(name = "read")
public class ReadNode extends AbstractSitemapNode {

    private static final String READER_CATEGORY = "reader:";

    @Parameter
    private String type = "file"; // "file" is default

    @Parameter
    private String src;

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.node.SitemapNode#getType()
     */
    public Class<?> getType() {
        return ReadNode.class;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.node.AbstractSitemapNode#invoke(org.apache.cocoon.sitemap.Invocation)
     */
    @Override
    public InvocationResult invoke(Invocation invocation) {
        Map<String, Object> parameters = new HashMap<String, Object>(this.getParameters());
        if (this.src != null) {
            String resolvedSource = invocation.resolveParameter(this.src);
            parameters.put("source", invocation.resolve(resolvedSource));
        }

        // set the baseUrl
        parameters.put("baseUrl", invocation.resolve(""));

        // install the component
        invocation.installComponent(READER_CATEGORY + this.type, parameters);

        // signal that we did some processing
        return InvocationResult.CONTINUE;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.node.AbstractSitemapNode#toString()
     */
    @Override
    public String toString() {
        return "ReadNode(" + this.src + ")";
    }
}
