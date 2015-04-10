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
import org.apache.cocoon.sitemap.NoMatchingPipelineException;
import org.apache.cocoon.sitemap.node.annotations.NodeChild;
import org.apache.cocoon.sitemap.util.ExceptionHandler;

@Node(name = "pipelines")
public class PipelinesNode extends AbstractSitemapNode {

    @NodeChild
    private ErrorNode errorNode;

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.node.SitemapNode#getType()
     */
    public Class<?> getType() {
        return PipelinesNode.class;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.node.AbstractSitemapNode#invoke(org.apache.cocoon.sitemap.Invocation)
     */
    @Override
    public InvocationResult invoke(Invocation invocation) {
        try {
            if (super.invoke(invocation).isContinued()) {
                // signal that we handled this successfully
                return InvocationResult.COMPLETED;
            }

            // none of our children was responsible
            throw new NoMatchingPipelineException("No pipeline matched the request '" + invocation.getRequestURI() + "'");
        } catch (Exception ex) {
            return this.handleException(invocation, ex);
        }
    }

    private InvocationResult handleException(Invocation invocation, Exception ex) {
        if (this.errorNode != null) {
            invocation.setThrowable(ExceptionHandler.getCause(ex));
            if (this.errorNode.invoke(invocation).isCompleted()) {
                // signal that we handled this successfully
                return InvocationResult.COMPLETED;
            }
        }

        // no error-handling configured or error-handling itself failed
        // -> let the parent handle this
        throw ExceptionHandler.getInvocationException(ex);
    }
}
