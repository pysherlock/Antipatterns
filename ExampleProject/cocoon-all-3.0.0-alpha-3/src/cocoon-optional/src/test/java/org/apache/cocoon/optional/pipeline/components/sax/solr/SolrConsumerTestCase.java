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
package org.apache.cocoon.optional.pipeline.components.sax.solr;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.sax.SAXPipelineComponent;
import org.apache.cocoon.sax.component.XMLGenerator;
import org.apache.cocoon.sax.component.XMLSerializer;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.junit.Test;
import org.xml.sax.SAXException;

public final class SolrConsumerTestCase {

    private SolrServer solr;

    public SolrConsumerTestCase() throws ParserConfigurationException, IOException, SAXException {
        SolrResourceLoader loader = new SolrResourceLoader("solr");
        CoreContainer container = new CoreContainer(loader);
        CoreDescriptor descriptor = new CoreDescriptor(container, "cname", ".");
        SolrCore core = container.create(descriptor);
        container.register(core.getName(), core, false);
        this.solr = new EmbeddedSolrServer(container, core.getName());
    }

    @Test
    public void testPipelineWithSolrConsumer() throws Exception {
        Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();

        pipeline.addComponent(new XMLGenerator(this.getClass().getResource("sample.xml")));
        pipeline.addComponent(new SolrConsumer(this.solr));
        pipeline.addComponent(new XMLSerializer());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipeline.setup(baos);
        pipeline.execute();

        SolrQuery query = new SolrQuery();

        query.setQuery("title:title");
        SolrDocumentList results = this.solr.query(query).getResults();
        Iterator<SolrDocument> documents = results.iterator();

        assertEquals(results.size(), 3);
        assertEquals(documents.next().getFieldValue("title"), "title 2");
        assertEquals(documents.next().getFieldValue("title"), "title 3");
        assertEquals(documents.next().getFieldValue("title"), "title 1");
    }
}
