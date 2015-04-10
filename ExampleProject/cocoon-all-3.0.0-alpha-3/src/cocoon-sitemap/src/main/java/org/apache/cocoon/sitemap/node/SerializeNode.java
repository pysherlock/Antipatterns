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

import org.apache.cocoon.sitemap.Invocation;
import org.apache.cocoon.sitemap.node.annotations.Parameter;

@Node(name = "serialize")
public class SerializeNode extends AbstractSitemapNode {

    private static final String SERIALIZER_CATEGORY = "serializer:";

    @Parameter
    private String type = "xml"; // default is "xml";

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.node.SitemapNode#getType()
     */
    public Class<?> getType() {
        return SerializeNode.class;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.node.AbstractSitemapNode#invoke(org.apache.cocoon.sitemap.Invocation)
     */
    @Override
    public InvocationResult invoke(final Invocation invocation) {
        // install the component
        invocation.installComponent(SERIALIZER_CATEGORY + this.type, this.getParameters());

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
        return "SerializeNode(type=" + this.type + ")";
    }
}
