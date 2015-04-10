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
import org.apache.cocoon.sitemap.util.SpringProxyHelper;

@Node(name = "when")
public class WhenNode extends MatchNode {


    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.node.SitemapNode#getType()
     */
    @Override
    public Class<?> getType() {
        return WhenNode.class;
    }

    @Override
    public InvocationResult invoke(Invocation invocation) {
        this.checkParent();

        InvocationResult result = super.invoke(invocation);

        // if a complete pipeline has been built, stop here
        if (invocation.hasCompletePipeline()) {
            return InvocationResult.COMPLETED;
        }

        // if the when node is matching, send the break signal because any of the sibling nodes (when/otherwise) should
        // be executed
        if (this.isMatching()) {
            return InvocationResult.BREAK;
        }

        // the when node is not matching
        return result;
    }

    /**
     * Check if the parent is a select node.
     */
    protected void checkParent() {
        if (this.getParent().getType().equals(SelectNode.class)) {
            SelectNode parentMatchNode = (SelectNode) SpringProxyHelper.unpackProxy(this.getParent());
            this.setValue(parentMatchNode.getValue());
        } else {
            throw new RuntimeException("The parent node has to be a select node.");
        }
    }
}