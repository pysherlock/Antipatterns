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
package org.apache.cocoon.sax.util;

import java.util.Map;

import org.apache.cocoon.sax.SAXConsumer;
import org.apache.cocoon.xml.sax.AbstractSAXPipe;
import org.xml.sax.ContentHandler;

public class SAXConsumerAdapter extends AbstractSAXPipe implements SAXConsumer {

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.component.PipelineComponent#finish()
     */
    public void finish() {
    }

    /**
     * Provide access to the {@link ContentHandler} used by the adapter.
     * 
     * @return {@link ContentHandler}
     */
    public ContentHandler getContentHandler() {
        return this.contentHandler;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.component.PipelineComponent#setConfiguration(java.util.Map)
     */
    public void setConfiguration(Map<String, ? extends Object> configuration) {
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.component.PipelineComponent#setup(java.util.Map)
     */
    public void setup(Map<String, Object> parameters) {
    }
}
