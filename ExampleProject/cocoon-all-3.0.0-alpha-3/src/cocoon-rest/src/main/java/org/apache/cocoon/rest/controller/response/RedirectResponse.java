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
package org.apache.cocoon.rest.controller.response;

import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.servlet.util.HttpContextHelper;

public class RedirectResponse implements RestResponse {

    private final String location;

    public RedirectResponse(String location) {
        super();
        this.location = location;
    }

    public RestResponseMetaData setup(Map<String, Object> inputParameters)
            throws Exception {
        HttpServletResponse response = HttpContextHelper.getResponse(inputParameters);
        response.sendRedirect(this.location);

        return new RestResponseMetaData();
    }

    public void execute(OutputStream outputStream) throws Exception {
    }
}
