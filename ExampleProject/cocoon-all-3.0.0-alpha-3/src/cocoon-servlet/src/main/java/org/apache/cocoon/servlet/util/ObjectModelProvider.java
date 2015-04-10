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
package org.apache.cocoon.servlet.util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.cocoon.servlet.controller.ControllerContextHelper;
import org.apache.cocoon.servletservice.util.ServletServiceRequest;
import org.apache.cocoon.sitemap.objectmodel.ObjectModel;

public class ObjectModelProvider {

    private ObjectModelProvider() {
        throw new AssertionError();
    }

    public static ObjectModel provide(Map<String, Object> parameters) {
        ObjectModel objectModel = new ObjectModel(parameters);
        Map<String, Object> cocoon = objectModel.getCocoonObject();

        HttpServletRequest request = HttpContextHelper.getRequest(parameters);
        cocoon.put("request", new ObjectModelRequest(request));
        cocoon.put("response", HttpContextHelper.getResponse(parameters));
        cocoon.put("context", HttpContextHelper.getServletContext(parameters));
        cocoon.put("settings", SettingsHelper.getSettings(parameters));
        Map<String, Object> controllerContext = ControllerContextHelper.getContext(parameters);
        if (controllerContext != null) {
            cocoon.put("controller", controllerContext);
        }

        return objectModel;
    }

    /**
     * A wrapper that can be used by expression languages to provide shortcuts to the request parameters and provides
     * Cocoon specific information about the request.
     */
    public static class ObjectModelRequest extends HttpServletRequestWrapper {

        public ObjectModelRequest(HttpServletRequest request) {
            super(request);
        }

        public String get(String key) {
            return this.getParameter(key);
        }

        public String getEmulatedMethod() {
            String alternativeMethod = this.getParameter("_method");

            if (alternativeMethod != null && !alternativeMethod.equals("")) {
                return alternativeMethod.toUpperCase();
            }

            return this.getMethod();
        }

        public boolean isSsf() {
            if(this.getRequest() instanceof ServletServiceRequest) {
                return true;
            }

            return false;
        }
    }
}
