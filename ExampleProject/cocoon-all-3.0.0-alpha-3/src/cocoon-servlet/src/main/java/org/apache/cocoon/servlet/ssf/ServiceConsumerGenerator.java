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
package org.apache.cocoon.servlet.ssf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.cocoon.pipeline.PipelineException;
import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.component.Starter;
import org.apache.cocoon.sax.AbstractSAXProducer;
import org.apache.cocoon.sax.util.XMLUtils;
import org.apache.cocoon.servlet.util.HttpContextHelper;

public class ServiceConsumerGenerator extends AbstractSAXProducer implements Starter {

    private Map<String, ? extends Object> inputParameters;

    public void execute() {
        HttpServletRequest request = HttpContextHelper.getRequest(this.inputParameters);
        if (!"POST".equals(request.getMethod())) {
            throw new ProcessingException("Cannot create consumer source for request that is not POST.");
        }

        InputStream inputStream = null;
        try {
            inputStream = request.getInputStream();
        } catch (IOException e) {
            throw new ProcessingException("Can't open inputStream on request.");
        }

        try {
            XMLUtils.toSax(inputStream, this.getSAXConsumer());
        } catch (PipelineException e) {
            throw e;
        } catch (Exception e) {
            throw new ProcessingException("Can't parse inputStream.", e);
        }
    }

    @Override
    public void setup(Map<String, Object> inputParameters) {
        super.setup(inputParameters);
        this.inputParameters = inputParameters;
    }
}