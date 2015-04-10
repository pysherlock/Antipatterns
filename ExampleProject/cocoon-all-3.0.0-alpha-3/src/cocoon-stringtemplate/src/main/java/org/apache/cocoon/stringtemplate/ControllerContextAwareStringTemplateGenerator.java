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
package org.apache.cocoon.stringtemplate;

import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.cocoon.servlet.controller.ControllerContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ControllerContextAwareStringTemplateGenerator extends StringTemplateGenerator {

    private final Log logger = LogFactory.getLog(this.getClass());

    public ControllerContextAwareStringTemplateGenerator() {
        super();
    }

    @Override
    protected void addTemplateAttributes(StringTemplate stringTemplate) {
        super.addTemplateAttributes(stringTemplate);

        // put all objects that are passed by the controller context
        Map<String, Object> controllerContext = ControllerContextHelper.getContext(this.parameters);
        for (Entry<String, Object> eachEntry : controllerContext.entrySet()) {
            String key = eachEntry.getKey().replace(".", "_");

            // remove already set parameters (otherwise there's some strange behavior in some cases
            // at least with StringTempalte 3.0
            Object attribute = stringTemplate.getAttribute(key);
            if (attribute != null) {
                stringTemplate.removeAttribute(key);
            }

            stringTemplate.setAttribute(key, eachEntry.getValue());

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Passing controller context parameter as attribute: key=" + eachEntry.getKey()
                        + ", value=" + eachEntry.getValue());
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.component.AbstractSAXProducer#setConfiguration(java.util.Map)
     */
    @Override
    public void setConfiguration(Map<String, ? extends Object> parameters) {
        super.setSource((URL) parameters.get("source"));
    }
}
