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
package org.apache.cocoon.profiling.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.caching.ParameterCacheKey;
import org.apache.cocoon.pipeline.component.CachingPipelineComponent;
import org.apache.cocoon.pipeline.component.PipelineComponent;
import org.apache.cocoon.profiling.data.InstanceRepresentation;
import org.apache.cocoon.profiling.data.ProfilingData;
import org.apache.cocoon.profiling.data.ProfilingDataHolder;
import org.apache.cocoon.sax.AbstractSAXGenerator;
import org.apache.cocoon.sax.SAXConsumer;
import org.apache.cocoon.sitemap.node.InvocationResult;
import org.apache.cocoon.sitemap.node.MatchNode;
import org.apache.cocoon.sitemap.node.SitemapNode;
import org.apache.cocoon.xml.sax.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A cocoon pipeline component to generate an XML representation of a {@link ProfilingData}.
 */
public class ProfilingGenerator extends AbstractSAXGenerator implements CachingPipelineComponent {

    private ProfilingDataHolder dataHolder;

    private ProfilingData profilingData;

    private String id;

    private SAXConsumer consumer;

    private List<String> elements = new LinkedList<String>();

    private Map<String, ComponentTreeElement> treeElements = new HashMap<String, ComponentTreeElement>();

    private boolean showSitemap = true;

    private boolean showAllMatchers = true;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setup(Map<String, Object> parameters) {
        HttpServletRequest req = (HttpServletRequest) parameters.get("javax.servlet.http.HttpServletRequest");
        if (req != null) {
            String sitemap = req.getParameter("sitemap");
            if (sitemap != null) {
                this.showSitemap = Boolean.valueOf(sitemap);
            }

            String matcher = req.getParameter("matcher");
            if (matcher != null) {
                this.showAllMatchers = Boolean.valueOf(matcher);
            }
        }
    }

    public CacheKey constructCacheKey() {
        ParameterCacheKey cacheKey = new ParameterCacheKey("id", this.id);
        cacheKey.addParameter("showSitemap", this.showSitemap);
        cacheKey.addParameter("showAllMatchers", this.showAllMatchers);

        return cacheKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfiguration(Map<String, ? extends Object> configuration) {
        super.setConfiguration(configuration);

        this.id = (String) configuration.get("id");
        this.profilingData = this.dataHolder.get(this.id);

        if (this.profilingData == null) {
            throw new SetupException(String.format("Profiling information for id '%s' not found", this.id));
        }
    }

    /**
     * Creates XML events for the given {@link ProfilingData}.
     */
    public void execute() {
        this.consumer = this.getSAXConsumer();
        try {
            this.consumer.startDocument();

            AttributesImpl attr = new AttributesImpl();
            this.addStringAttribute(attr, "id", this.id);

            this.startElement("cocoon-profiling", attr);
            ComponentTreeElement root = this.buildComponentTree(this.profilingData);
            this.handleTreeElement(root);
            this.endElement();

            this.consumer.endDocument();
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public void setProfilingDataHolder(ProfilingDataHolder dataHolder) {
        this.dataHolder = dataHolder;
    }

    private void handleTreeElement(ComponentTreeElement treeElement) throws SAXException {
        Class<?> target = treeElement.getTargetClass();

        String element = null;

        if (Servlet.class.isAssignableFrom(target)) {
            element = "servlet";
        } else if (SitemapNode.class.isAssignableFrom(target)) {
            element = "node";
        } else if (PipelineComponent.class.isAssignableFrom(target)) {
            element = "component";
        } else if (Pipeline.class.isAssignableFrom(target)) {
            element = "pipeline";
        }

        if (element == null) {
            throw new RuntimeException("ProfilingGenerator can't create element for " + target.getName());
        }

        if (this.showElement(treeElement)) {
            AttributesImpl attr = new AttributesImpl();
            this.addStringAttribute(attr, "name", treeElement.getDisplayName());
            this.addStringAttribute(attr, "executionTime", this.milliString(treeElement.getExecutionMillis()));

            this.startElement(element, attr);

            AttributesImpl attr2 = new AttributesImpl();
            this.addStringAttribute(attr2, "class", treeElement.getProfiler());
            this.addSimple("profiler", attr2);

            this.startElement("invocations");
            for (ProfilingData data : treeElement.getInvocations()) {
                this.handleInvocation(data);
            }
            this.endElement();

            this.handleChildren(treeElement);

            this.endElement();
        } else {
            this.handleChildren(treeElement);
        }
    }

    private String milliString(double executionTime) {
        return String.format(Locale.US, "%.3fms", executionTime);
    }

    private void handleInvocation(ProfilingData data) throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        this.addStringAttribute(attr, "method", data.getMethod());
        this.addStringAttribute(attr, "executionTime", this.milliString(data.getExecutionMillis()));

        this.startElement("invocation", attr);

        this.startElement("properties");
        for (Entry<String, String> entry : data.getData().entrySet()) {
            AttributesImpl attr2 = new AttributesImpl();
            this.addStringAttribute(attr2, "id", entry.getKey());

            this.startElement("property", attr2);
            this.addData(entry.getValue());
            this.endElement();
        }
        this.endElement();

        this.startElement("arguments");
        for (InstanceRepresentation arg : data.getArguments()) {
            this.addArgument(arg);
        }
        this.endElement();

        this.startElement("result");
        if (data.getException() != null) {
            AttributesImpl attr2 = new AttributesImpl();
            this.addStringAttribute(attr2, "class", data.getException().getClass().getName());
            this.addStringAttribute(attr2, "message", data.getException().getMessage());
            this.addSimple("exception", attr2);
        } else {
            String classname;

            if (data.getReturnValue().getRepresentedClass() == null) {
                classname = "";
            } else {
                classname = data.getReturnValue().getRepresentedClass().getName();
            }

            AttributesImpl attr3 = new AttributesImpl();
            this.addStringAttribute(attr3, "class", classname);
            this.startElement("return-value", attr3);
            this.addSimple("value", data.getReturnValue().getStringRepresentation());
            this.endElement();
        }
        this.endElement();

        this.endElement();
    }

    private void handleChildren(ComponentTreeElement treeElement) throws SAXException {
        for (ComponentTreeElement te : treeElement.getChildren()) {
            this.handleTreeElement(te);
        }
    }

    private boolean showElement(ComponentTreeElement element) {
        Class<?> targetClass = element.getTargetClass();
        if (SitemapNode.class.isAssignableFrom(targetClass)) {
            if (!this.showSitemap) {
                return false;
            }

            if (MatchNode.class.isAssignableFrom(targetClass)) {
                return this.showMatchNode(element);
            }
        }

        return true;
    }

    private boolean showMatchNode(ComponentTreeElement element) {
        if (this.showAllMatchers) {
            return true;
        }

        for (ProfilingData invocation : element.getInvocations()) {
            if (invocation.getMethod().equals("invoke")) {
                InstanceRepresentation returnValue = invocation.getReturnValue();
                InvocationResult result = InvocationResult.valueOf(returnValue.getStringRepresentation());
                return result == InvocationResult.COMPLETED || result == InvocationResult.CONTINUE;
            }
        }

        return true;
    }

    private void addSimple(String name, String data) throws SAXException {
        this.startElement(name);
        this.addData(data);
        this.endElement();
    }

    private void addSimple(String name, AttributesImpl attr) throws SAXException {
        this.startElement(name, attr);
        this.endElement();
    }

    private void addArgument(InstanceRepresentation data) throws SAXException {
        AttributesImpl attr = new AttributesImpl();

        Class<?> clazz = data.getRepresentedClass();
        String value = data.getStringRepresentation();

        this.addStringAttribute(attr, "class", clazz == null ? "null" : clazz.getName());
        this.startElement("argument", attr);
        this.addData(value);
        this.endElement();
    }

    private void addStringAttribute(AttributesImpl attr, String key, String value) {
        attr.addAttribute("", key, key, "xsd:string", value == null ? "" : value);
    }

    private void startElement(String name) throws SAXException {
        this.startElement(name, (Attributes) null);
    }

    private void startElement(String name, Attributes attributes) throws SAXException {
        this.consumer.startElement("", name, name, attributes);
        this.elements.add(0, name);
    }

    private void endElement() throws SAXException {
        String name = this.elements.remove(0);
        this.consumer.endElement("", name, name);
    }

    private void addData(String data) throws SAXException {
        this.consumer.characters(data.toCharArray(), 0, data.length());
    }

    private ComponentTreeElement buildComponentTree(ProfilingData data) {
        ComponentTreeElement root = new ComponentTreeElement(data);
        this.treeElements.put(data.getId(), root);

        for (ProfilingData child : data.getChildren()) {
            this.continueComponentTree(root, data, child);
        }

        return root;
    }

    private void continueComponentTree(ComponentTreeElement parentTreeElement, ProfilingData parent, ProfilingData data) {
        ComponentTreeElement treeElement = this.treeElements.get(data.getId());

        if (treeElement == null) {
            treeElement = new ComponentTreeElement(data);
            parentTreeElement.addChild(treeElement);
            this.treeElements.put(data.getId(), treeElement);
        } else {
            treeElement.addInvocation(data);
        }

        for (ProfilingData child : data.getChildren()) {
            this.continueComponentTree(treeElement, data, child);
        }
    }

    private class ComponentTreeElement {

        private List<ComponentTreeElement> children;

        private List<ProfilingData> invocations;

        public ComponentTreeElement(ProfilingData data) {
            this.children = new ArrayList<ComponentTreeElement>();
            this.invocations = new ArrayList<ProfilingData>();
            this.invocations.add(data);
        }

        public Class<?> getTargetClass() {
            InstanceRepresentation target = this.invocations.get(0).getTarget();
            return target == null ? null : target.getRepresentedClass();
        }

        public String getDisplayName() {
            String displayName = this.invocations.get(0).getDisplayName();
            for (ProfilingData pd : this.invocations) {
                if (pd.isDisplayNameSet()) {
                    displayName = pd.getDisplayName();
                    break;
                }
            }
            return displayName;
        }

        public double getExecutionMillis() {
            double t = 0;
            for (ProfilingData data : this.invocations) {
                t += data.getExecutionMillis();
            }
            return t;
        }

        public String getProfiler() {
            return this.invocations.get(0).getProfiler();
        }

        public void addChild(ComponentTreeElement child) {
            this.children.add(child);
        }

        public void addInvocation(ProfilingData profilingData) {
            this.invocations.add(profilingData);
        }

        public List<ComponentTreeElement> getChildren() {
            return Collections.unmodifiableList(this.children);
        }

        public List<ProfilingData> getInvocations() {
            return Collections.unmodifiableList(this.invocations);
        }
    }
}
