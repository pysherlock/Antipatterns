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

import java.net.URL;
import java.util.Map;

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.component.Starter;
import org.apache.cocoon.sax.AbstractSAXProducer;
import org.apache.cocoon.sax.util.XMLUtils;

public class ServletServiceGenerator extends AbstractSAXProducer implements Starter {

    private URL service;

    public void execute() {
        if (this.service == null) {
            throw new IllegalArgumentException("ServletServiceGenerator has no service set.");
        }

        try {
            XMLUtils.toSax(this.service.openConnection(), this.getSAXConsumer());
        } catch (Exception e) {
            throw new ProcessingException("Can't parse " + this.service, e);
        }
    }

    @Override
    public void setConfiguration(Map<String, ? extends Object> configuration) {
        this.service = (URL) configuration.get("service");
    }
}
