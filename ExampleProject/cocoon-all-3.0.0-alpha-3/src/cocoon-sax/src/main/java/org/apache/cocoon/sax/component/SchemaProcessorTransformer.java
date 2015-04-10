/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.sax.component;

import java.net.URL;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.pipeline.util.StringRepresentation;
import org.apache.cocoon.sax.AbstractSAXTransformer;
import org.apache.cocoon.sax.SAXConsumer;
import org.apache.cocoon.sax.util.InMemoryLRUResourceCache;
import org.apache.cocoon.sax.util.SAXConsumerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

public final class SchemaProcessorTransformer extends AbstractSAXTransformer {

    private static final InMemoryLRUResourceCache<Schema> SCHEMA_LRU_CACHE = new InMemoryLRUResourceCache<Schema>();

    private static final SchemaFactory SCHEMA_FACTORY = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    private static final String SOURCE = "source";

    private final Log logger = LogFactory.getLog(this.getClass());

    private Schema schema;

    private URL source;

    public SchemaProcessorTransformer() {
        super();
    }

    public SchemaProcessorTransformer(URL source) {
        super();
        this.init(source);
    }

    @Override
    public void setConfiguration(Map<String, ? extends Object> configuration) {
        this.init((URL) configuration.get(SOURCE));
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "src=" + this.source);
    }

    @Override
    protected void setSAXConsumer(SAXConsumer xmlConsumer) {
        ValidatorHandler validatorHandler = this.schema.newValidatorHandler();
        validatorHandler.setErrorHandler(new SchemaErrorHandler(this.logger, this.source.toExternalForm()));
        validatorHandler.setContentHandler(xmlConsumer);

        SAXConsumerAdapter saxConsumerAdapter = new SAXConsumerAdapter();
        saxConsumerAdapter.setContentHandler(validatorHandler);
        super.setSAXConsumer(saxConsumerAdapter);
    }

    private void init(URL source) {
        if (source == null) {
            throw new IllegalArgumentException("The parameter 'source' mustn't be null.");
        }

        if (SCHEMA_LRU_CACHE.containsKey(source)) {
            this.schema = SCHEMA_LRU_CACHE.get(source);
        } else {
            try {
                this.schema = SCHEMA_FACTORY.newSchema(source);
                SCHEMA_LRU_CACHE.put(source, this.schema);
            } catch (SAXException e) {
                throw new SetupException("Could not initialize xschema source", e);
            }
        }

        this.source = source;
    }
}
