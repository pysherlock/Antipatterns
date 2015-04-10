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

import org.apache.cocoon.sitemap.Invocation;

@Node(name = "otherwise")
public class OtherwiseNode extends AbstractSitemapNode {


    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.node.SitemapNode#getType()
     */
    public Class<?> getType() {
        return OtherwiseNode.class;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.node.AbstractSitemapNode#invoke(org.apache.cocoon.sitemap.Invocation)
     */
    @Override
    public InvocationResult invoke(Invocation invocation) {
        this.checkParent();

        // execute the child nodes
        invocation.pushSitemapParameters(null, new HashMap<String, Object>());
        super.invoke(invocation);
        invocation.popSitemapParameters();

        // if the pipeline has been finished within the otherwise section,
        // stop the execution
        if (invocation.hasCompletePipeline()) {
            return InvocationResult.COMPLETED;
        }

        // send the BREAK signal
        return InvocationResult.BREAK;
    }

    protected void checkParent() {
        if (!this.getParent().getType().equals(SelectNode.class)) {
            throw new RuntimeException("The parent node has to be a select node.");
        }
    }
}
