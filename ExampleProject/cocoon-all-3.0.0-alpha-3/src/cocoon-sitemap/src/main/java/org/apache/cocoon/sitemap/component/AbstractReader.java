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
package org.apache.cocoon.sitemap.component;

import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

import org.apache.cocoon.pipeline.component.AbstractPipelineComponent;
import org.apache.cocoon.pipeline.component.Finisher;
import org.apache.cocoon.pipeline.component.Starter;

public abstract class AbstractReader extends AbstractPipelineComponent implements Starter, Finisher {

    protected String mimeType;
    protected OutputStream outputStream;
    protected URL source;

    public AbstractReader() {
        super();
    }

    public AbstractReader(URL src) {
        this.source = src;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.component.PipelineComponent#setConfiguration(java.util.Map)
     */
    @Override
    public void setConfiguration(Map<String, ? extends Object> configuration) {
        this.setSource((URL) configuration.get("source"));
        this.setMimeType((String) configuration.get("mime-type"));
    }

    /**
     * Set the mime-type directly which is useful when this component is used directly.
     * 
     * @param mimeType The mime-type that belongs to the content that is produced by this component.
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.component.Finisher#setOutputStream(java.io.OutputStream)
     */
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Set the source {@link URL} directly when this component is used directly.
     * 
     * @param source A {@link URL} that will be used by this component.
     */
    public void setSource(URL source) {
        this.source = source;
    }
}