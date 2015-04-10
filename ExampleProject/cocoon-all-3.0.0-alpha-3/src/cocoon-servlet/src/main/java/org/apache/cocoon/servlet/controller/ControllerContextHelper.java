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
package org.apache.cocoon.servlet.controller;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.cocoon.servlet.util.HttpContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class helps with storing and retrieving the controller context.
 * Internally it uses the current servlet request. Thanks to the servlet-service
 * framework, the current request becomes the parent of all sub-requests that
 * use e.g. the 'servlet:' protocol. This means that it is enough to store
 * objects that should be available in a sub-request into the current request.
 */
public class ControllerContextHelper {

    private static final Log LOG = LogFactory.getLog(ControllerContextHelper.class);

    private static final String CONTEXT_OBJECT = ControllerContextHelper.class.getName();

    /**
     * Retrieve the current controller context from the Cocoon parameters.
     * 
     * @param parameters The Cocoon parameters map.
     * @return The controller context map as being available in the current
     *         servlet request.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getContext(Map<String, ? extends Object> parameters) {
        HttpServletRequest request = HttpContextHelper.getRequest(parameters);
        Object controllerContext = request.getAttribute(CONTEXT_OBJECT);

        if (controllerContext instanceof Map) {
            return (Map<String, Object>) controllerContext;
        }

        if (controllerContext != null) {
            LOG.warn("The parameters contain an entry with the key " + CONTEXT_OBJECT + " but it is not a map.");
        }
        return Collections.EMPTY_MAP;
    }

    /**
     * Store the controller context (a {@link Map}) in the Cocoon parameters.
     * 
     * @param controllerContext The objects that should be available in the
     *            child request.
     * @param parameters The Cocoon parameters map.
     */
    public static void storeContext(Map<String, Object> controllerContext, Map<String, Object> parameters) {
        HttpServletRequest request = HttpContextHelper.getRequest(parameters);
        storeContext(controllerContext, request);
    }

    /**
     * Store the controller context (a {@link Map} directly into the
     * {@link HttpServletRequest}.
     * 
     * @param controllerContext The objects that should be available in the
     *            child request.
     * @param request The current request that becomes the parent of the next
     *            request.
     */
    public static void storeContext(Map<String, Object> controllerContext, HttpServletRequest request) {
        request.setAttribute(CONTEXT_OBJECT, controllerContext);
    }
}
