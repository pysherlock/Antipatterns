/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.rest.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.rest.controller.method.Delete;
import org.apache.cocoon.rest.controller.method.Get;
import org.apache.cocoon.rest.controller.method.Head;
import org.apache.cocoon.rest.controller.method.Options;
import org.apache.cocoon.rest.controller.method.Post;
import org.apache.cocoon.rest.controller.method.Put;
import org.apache.cocoon.rest.controller.response.RestResponse;
import org.apache.cocoon.rest.controller.response.Status;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MethodDelegator {

    private static Map<String, MethodDelegate> delegates = new HashMap<String, MethodDelegate>();

    private final Log logger = LogFactory.getLog(this.getClass());

    static {
        delegates.put("DELETE", new DeleteDelegate());
        delegates.put("GET", new GetDelegate());
        delegates.put("HEAD", new HeadDelegate());
        delegates.put("OPTIONS", new OptionsDelegate());
        delegates.put("POST", new PostDelegate());
        delegates.put("PUT", new PutDelegate());
    }

    public MethodDelegator() {
        super();
    }

    public RestResponse delegate(HttpServletRequest request, Object controller) throws Exception {
        if (request != null && request.getMethod() != null) {
            MethodDelegate methodDelegate = delegates.get(this.getMethod(request));
            if (methodDelegate != null) {
                return methodDelegate.execute(controller);
            }
        }

        return new Status(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    private String getMethod(HttpServletRequest request) {
        String alternativeMethod = request.getParameter("_method");
        if (alternativeMethod != null) {
            alternativeMethod = alternativeMethod.toUpperCase();
            if (delegates.keySet().contains(alternativeMethod)) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Using alternative request method '" + alternativeMethod
                            + "' as provided by the request parameter '_method'");
                }

                return alternativeMethod;
            }

            if (this.logger.isWarnEnabled()) {
                this.logger.warn("The request parameter '_request' refers to an unsupported request method: _method='" + alternativeMethod + "'");
            }
        }

        return request.getMethod().toUpperCase();
    }

    private static class DeleteDelegate extends MethodDelegate {

        public DeleteDelegate() {
            super();
        }

        @Override
        public RestResponse execute(Object controller) throws Exception {
            if (controller instanceof Delete) {
                Delete delete = (Delete) controller;
                return delete.doDelete();
            }
            return super.execute(controller);
        }
    }

    private static class GetDelegate extends MethodDelegate {

        public GetDelegate() {
            super();
        }

        @Override
        public RestResponse execute(Object controller) throws Exception {
            if (controller instanceof Get) {
                Get get = (Get) controller;
                return get.doGet();
            }
            return super.execute(controller);
        }
    }

    private static class HeadDelegate extends MethodDelegate {

        public HeadDelegate() {
            super();
        }

        @Override
        public RestResponse execute(Object controller) throws Exception {
            if (controller instanceof Head) {
                Head head = (Head) controller;
                return head.doHead();
            }

            // According to http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html a
            // HEAD response behaves exactly the same as a GET request except that
            // no content is sent.
            return new GetDelegate().execute(controller);
        }
    }

    private static abstract class MethodDelegate {

        public MethodDelegate() {
            super();
        }

        public RestResponse execute(Object controller) throws Exception {
            return new Status(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private static class OptionsDelegate extends MethodDelegate {

        public OptionsDelegate() {
            super();
        }

        @Override
        public RestResponse execute(Object controller) throws Exception {
            if (controller instanceof Options) {
                Options options = (Options) controller;
                return options.doOptions();
            }

            return super.execute(controller);
        }
    }

    private static class PostDelegate extends MethodDelegate {

        public PostDelegate() {
            super();
        }

        @Override
        public RestResponse execute(Object controller) throws Exception {
            if (controller instanceof Post) {
                Post post = (Post) controller;
                return post.doPost();
            }

            return super.execute(controller);
        }
    }

    private static class PutDelegate extends MethodDelegate {

        public PutDelegate() {
            super();
        }

        @Override
        public RestResponse execute(Object controller) throws Exception {
            if (controller instanceof Put) {
                Put put = (Put) controller;
                return put.doPut();
            }

            return super.execute(controller);
        }
    }
}
