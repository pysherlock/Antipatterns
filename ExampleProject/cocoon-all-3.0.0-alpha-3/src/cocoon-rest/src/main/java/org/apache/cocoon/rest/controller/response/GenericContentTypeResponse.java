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
import java.net.HttpURLConnection;
import java.util.Map;

import org.apache.commons.io.IOUtils;

/**
 * Controller response that can response as any content type.
 */
public class GenericContentTypeResponse implements RestResponse {

    private String data, contentType;
    private int statusCode = 200;

    /**
     * @param data the data to return to the client. e.g. dataJson.toString()
     * @param contentType the contentType you want to return. e.g. "application/json"
     */
    public GenericContentTypeResponse(String data, String contentType) {
        this.data = data;
        this.contentType = contentType;
    }

    /**
     * @param data the data to return to the client. e.g. dataJson.toString()
     * @param contentType the contentType you want to return. e.g. "application/json"
     * @param statusCode if you want return any other response code see 
     * {@link HttpURLConnection} for the different available codes.
     */
    public GenericContentTypeResponse(String data, String contentType, int statusCode) {
        this.data = data;
        this.contentType = contentType;
        this.statusCode = statusCode;
    }
    
    /**
     * @return response code see 
     * {@link HttpURLConnection} for the different available codes that
     * we will return to the client
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * You can set the status code as well after creating and instance.
     * @param statusCode if you want return any other response code see 
     * {@link HttpURLConnection} for the different available codes.
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.rest.controller.response.RestResponse#execute(java.io.OutputStream)
     */
    public void execute(OutputStream outputStream) throws Exception {
        IOUtils.write(data, outputStream);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.rest.controller.response.RestResponse#setup(java.util.Map)
     */
    public RestResponseMetaData setup(Map<String, Object> inputParameters)
            throws Exception {
        RestResponseMetaData restResponseMetaData = new RestResponseMetaData();
        restResponseMetaData.setStatusCode(statusCode);
        restResponseMetaData.setContentType(contentType);
        return restResponseMetaData;
    }
}
