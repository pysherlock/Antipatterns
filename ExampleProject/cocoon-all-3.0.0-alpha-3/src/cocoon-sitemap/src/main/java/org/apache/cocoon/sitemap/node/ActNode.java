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

@Node(name="act")
public class ActNode extends AbstractSitemapNode {

    private static final String ACTION_CATEGORY = "action:";

    @Parameter
    private String type;

    @Override
    public InvocationResult invoke(Invocation invocation) {
        invocation.installAction(ACTION_CATEGORY + this.type);

        // signal that we did some processing
        return InvocationResult.CONTINUE;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.sitemap.node.SitemapNode#getType()
     */
    public Class<?> getType() {
        return ActNode.class;
    }

    @Override
    public String toString() {
        return "ActNode(" + this.type + ")";
    }
}
