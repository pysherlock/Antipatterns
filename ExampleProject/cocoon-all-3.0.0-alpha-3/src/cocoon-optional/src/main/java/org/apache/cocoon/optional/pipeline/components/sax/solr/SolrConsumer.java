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
package org.apache.cocoon.optional.pipeline.components.sax.solr;

import java.net.MalformedURLException;

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.sax.AbstractSAXTransformer;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.StrUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class SolrConsumer extends AbstractSAXTransformer {

    private static final String FIELD = "field";

    private static final String NAME = "name";

    private static final String BOOST = "boost";

    private static final String NULL = "null";

    private static final String DOC = "doc";

    private static final String DOCS = "docs";

    private SolrServer solr = null;

    private boolean isNull = false;

    private SolrInputDocument doc = new SolrInputDocument();

    private float boost = 1.0f;

    private String name = null;

    private StringBuilder text = new StringBuilder();

    public SolrConsumer(String url) throws MalformedURLException {
        this.solr = new CommonsHttpSolrServer(url);
    }

    public SolrConsumer(SolrServer server) {
        this.solr = server;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        this.text.append(ch, start, length);
        this.getSAXConsumer().characters(ch, start, length);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (FIELD.equals(localName)) {
            this.text = new StringBuilder();
            this.boost = 1.0f;

            if (atts.getValue(NAME) != null) {
                this.name = atts.getValue(NAME);
            }

            if (atts.getValue(BOOST) != null) {
                this.boost = Float.parseFloat(atts.getValue(BOOST));
            }

            if (atts.getValue(NULL) != null) {
                this.isNull = StrUtils.parseBoolean(atts.getValue(NULL));
            }
        }

        this.getSAXConsumer().startElement(uri, localName, qName, atts);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (DOCS.equals(localName)) {
            try {
                this.solr.commit();
            } catch (Exception e) {
                throw new ProcessingException("Unable to commit the Solr documents.", e);
            }
        } else if (DOC.equals(localName)) {
            try {
                this.solr.add(this.doc);
                this.doc = new SolrInputDocument();
            } catch (Exception e) {
                throw new ProcessingException("Unable to add the Solr document.", e);
            }
        } else if (FIELD.equals(localName)) {
            if (!this.isNull && this.text.length() > 0) {
                this.doc.addField(this.name, this.text.toString(), this.boost);
                this.boost = 1.0f;
            }
        }

        this.getSAXConsumer().endElement(uri, localName, qName);
    }

}
