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
import org.apache.cocoon.sitemap.node.annotations.NodeChild;
import org.apache.cocoon.sitemap.node.annotations.Parameter;
import org.apache.cocoon.sitemap.util.ExceptionHandler;

@Node(name = "pipeline")
public class PipelineNode extends AbstractSitemapNode {

    private static final String PIPELINE_CATEGORY = "pipeline:";

    @NodeChild
    private ErrorNode errorNode;

    @Parameter
    private String type = "caching"; // "caching" is the default type


    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.node.SitemapNode#getType()
     */
    public Class<?> getType() {
        return PipelineNode.class;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.node.AbstractSitemapNode#invoke(org.apache.cocoon.sitemap.Invocation)
     */
    @Override
    public InvocationResult invoke(Invocation invocation) {
        this.installPipeline(invocation);
        this.clearActions(invocation);

        try {
            // now proceed as usual (invoking our children)
            if (!super.invoke(invocation).isContinued()) {
                // indicate that we couldn't handle this
                return InvocationResult.NONE;
            }

            // one of our children was responsible for handling this invocation
            // assume the invocation is properly prepared and execute it
            invocation.execute();

            // indicate that we handled this successfully
            return InvocationResult.COMPLETED;
        } catch (Exception ex) {
            return this.handleException(invocation, ex);
        }
    }

    private void clearActions(Invocation invocation) {
        invocation.reset();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.node.AbstractSitemapNode#toString()
     */
    @Override
    public String toString() {
        return "PipelineNode(" + this.type + ")";
    }

    private InvocationResult handleException(Invocation invocation, Exception ex) {
        if (this.errorNode != null) {
            // try to recover from the exception, using our error node
            invocation.setThrowable(ExceptionHandler.getCause(ex));
            if (this.errorNode.invoke(invocation).isCompleted()) {
                // indicate that we handled this successfully
                return InvocationResult.COMPLETED;
            }
        }

        // no error-handling configured or error-handling itself failed
        // -> let the parent handle this
        throw ExceptionHandler.getInvocationException(ex);
    }

    protected void installPipeline(Invocation invocation) {
        invocation.installPipeline(PIPELINE_CATEGORY + this.type, this.getParameters());
    }
}
