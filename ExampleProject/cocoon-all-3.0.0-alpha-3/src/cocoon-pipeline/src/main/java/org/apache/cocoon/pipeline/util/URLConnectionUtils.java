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
package org.apache.cocoon.pipeline.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class URLConnectionUtils {

    private static final Log LOG = LogFactory.getLog(URLConnectionUtils.class);

    /**
     * Close a {@link URLConnection} quietly and take care of all the exception handling.
     *
     * @param urlConnection {@link URLConnection} to be closed.
     */
    public static void closeQuietly(URLConnection urlConnection) {
        if (urlConnection == null) {
            return;
        }
    	
        if (urlConnection.getDoInput()) {
            InputStream inputStream = null;
            try {
                inputStream = urlConnection.getInputStream();
            } catch (IOException e) {
                LOG.warn("Can't close input stream from " + urlConnection.getURL(), e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        LOG.warn("Can't close input stream from " + urlConnection.getURL(), e);
                    }
                }
            }
        }

        if (urlConnection.getDoOutput()) {
            OutputStream outputStream = null;
            try {
                outputStream = urlConnection.getOutputStream();
            } catch (IOException e) {
                LOG.warn("Can't close output stream to " + urlConnection.getURL(), e);
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        LOG.warn("Can't close input stream to " + urlConnection.getURL(), e);
                    }
                }
            }
        }
    }
}
