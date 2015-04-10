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
package org.apache.cocoon.profiling;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.profiling.component.ProfilingGenerator;
import org.apache.cocoon.profiling.component.ProfilingPngSerializer;
import org.apache.cocoon.profiling.data.ProfilingData;
import org.apache.cocoon.profiling.data.ProfilingDataHolder;
import org.apache.cocoon.profiling.profiler.PipelineComponentProfiler;
import org.apache.cocoon.sax.component.XMLSerializer;
import org.apache.cocoon.servlet.util.HttpContextHelper;
import org.apache.cocoon.sitemap.ComponentProvider;
import org.apache.cocoon.sitemap.Invocation;
import org.apache.cocoon.sitemap.InvocationImpl;
import org.apache.cocoon.sitemap.SitemapBuilder;
import org.apache.cocoon.sitemap.node.SitemapNode;
import org.apache.cocoon.sitemap.objectmodel.ObjectModel;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ProfilingIntegrationTest {

    private static final String X_COCOON_PROFILING_ID = "X-Cocoon-Profiling-ID";

    SitemapNode sitemap;

    private static ClassPathXmlApplicationContext applicationContext;

    private static ComponentProvider componentProvider;

    private ByteArrayOutputStream output;

    private SitemapServlet servlet;

    @BeforeClass
    public static void beforeClass() {
        applicationContext = getApplicationContext();
        componentProvider = getComponentProvider();
    }

    @Before
    public void setup() throws Exception {
        this.sitemap = this.getSitemap();
        this.servlet = (SitemapServlet) applicationContext.getBean("MockSitemapServlet");
        this.servlet.setTestcase(this);
        this.output = new ByteArrayOutputStream();
    }

    @AfterClass
    public static void shutdownApplicationContext() {
        applicationContext.close();
    }

    private static ClassPathXmlApplicationContext getApplicationContext() {
        return new ClassPathXmlApplicationContext(new String[] {
        "cocoon-sample-test-collector.xml",

        "cocoon-profiling-test-component.xml",

        "cocoon-sample-test-spring-configurator.xml",

        "META-INF/cocoon/spring/cocoon-sitemap.xml",

        "META-INF/cocoon/spring/cocoon-pipeline-action.xml",

        "META-INF/cocoon/spring/cocoon-pipeline-component.xml",

        "META-INF/cocoon/spring/cocoon-pipeline.xml",

        "META-INF/cocoon/spring/cocoon-sitemap-node.xml",

        "META-INF/cocoon/spring/cocoon-expression-language.xml",

        "META-INF/cocoon/spring/cocoon-servlet-component.xml",

        "META-INF/cocoon/spring/cocoon-controller.xml",

        "META-INF/cocoon/spring/cocoon-profiling-component.xml",
        });
    }

    private static ComponentProvider getComponentProvider() {
        return (ComponentProvider) applicationContext.getBean("org.apache.cocoon.sitemap.ComponentProvider");
    }

    private SitemapNode getSitemap() throws Exception {
        URL sitemapURL = this.getClass().getResource("/COB-INF/sitemap.xmap");

        SitemapBuilder sb = (SitemapBuilder) applicationContext.getBean("org.apache.cocoon.sitemap.SitemapBuilder");

        return sb.build(sitemapURL);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSimpleGenerator() throws Exception {
        String id = "42";
        ProfilingData profilingData = new ProfilingData();
        profilingData.setInvocationStartTime(System.nanoTime());
        profilingData.setInvocationEndTime(System.nanoTime() + 1000000000);
        profilingData.addData("foo", "bar");
        profilingData.setProfiler(new PipelineComponentProfiler().getClass().getSimpleName());
        profilingData.setTarget(new ProfilingPngSerializer());
        profilingData.setReturnValue(null);

        ProfilingDataHolder holder = (ProfilingDataHolder) applicationContext
                .getBean("org.apache.cocoon.profiling.data.ProfilingDataHolder");

        holder.store(id, profilingData);

        HashMap<String, Object> configuration = new HashMap<String, Object>();
        configuration.put("id", id);

        ProfilingGenerator profilingGenerator = new ProfilingGenerator();
        profilingGenerator.setProfilingDataHolder(holder);

        Pipeline pipeline = new NonCachingPipeline();
        profilingGenerator.setConfiguration(configuration);

        pipeline.addComponent(profilingGenerator);
        pipeline.addComponent(new XMLSerializer());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipeline.setup(baos, configuration);
        pipeline.execute();

        String s = baos.toString();

        assertTrue(s.contains("<cocoon-profiling id=\"42\">"));
        assertTrue(s.contains("<component"));
        assertTrue(s.contains("<properties><property id=\"foo\">bar</property></properties>"));
    }

    @Test
    public void testServletInvocation() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        MockHttpServletResponse response = this.invokeServlet("/sax-pipeline/simple-xsd", parameters);

        assertEquals("3", response.getHeader("X-Cocoon-Version"));

        String profilingId = response.getHeader(X_COCOON_PROFILING_ID);
        assertNotNull(profilingId);

        ProfilingDataHolder dataHolder = (ProfilingDataHolder) applicationContext
                .getBean("org.apache.cocoon.profiling.data.ProfilingDataHolder");

        ProfilingData profilingData = dataHolder.get(profilingId);

        assertTrue(profilingData.getChildren().size() > 0);

        response = this.invokeServlet("/controller/profiling/" + profilingId, new HashMap<String, Object>());
    }

    @Test
    public void testSitemap() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        MockHttpServletResponse response = this.invokeServlet("/", parameters);

        assertEquals("3", response.getHeader("X-Cocoon-Version"));

        String profilingId = response.getHeader(X_COCOON_PROFILING_ID);
        assertNotNull(profilingId);

        ProfilingDataHolder dataHolder = (ProfilingDataHolder) applicationContext
                .getBean("org.apache.cocoon.profiling.data.ProfilingDataHolder");

        ProfilingData profilingData = dataHolder.get(profilingId);

        assertTrue(this.containsProfilingData("Sitemap", profilingData, null));
    }

    @Test
    public void testMatchEqual() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        MockHttpServletResponse response = this.invokeServlet("/sax-pipeline/simple", parameters);

        assertEquals("3", response.getHeader("X-Cocoon-Version"));

        String profilingId = response.getHeader(X_COCOON_PROFILING_ID);
        assertNotNull(profilingId);

        ProfilingDataHolder dataHolder = (ProfilingDataHolder) applicationContext
                .getBean("org.apache.cocoon.profiling.data.ProfilingDataHolder");

        ProfilingData profilingData = dataHolder.get(profilingId);

        assertTrue(profilingData.getChildren().size() > 0);
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("equals", null);
        assertTrue(this.containsProfilingData("MatchNode", profilingData, attributes));
    }

    @Test
    public void testMatchPattern() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        MockHttpServletResponse response = this.invokeServlet("/sax-pipeline/simple-xhtml", parameters);

        assertEquals("3", response.getHeader("X-Cocoon-Version"));

        String profilingId = response.getHeader(X_COCOON_PROFILING_ID);
        assertNotNull(profilingId);

        ProfilingDataHolder dataHolder = (ProfilingDataHolder) applicationContext
                .getBean("org.apache.cocoon.profiling.data.ProfilingDataHolder");

        ProfilingData profilingData = dataHolder.get(profilingId);

        assertTrue(profilingData.getChildren().size() > 0);
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("pattern", null);
        assertTrue(this.containsProfilingData("MatchNode", profilingData, attributes));
    }

    @Test
    public void testMatchWildcard() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        MockHttpServletResponse response = this.invokeServlet("/advanced-matching-2", parameters);

        assertEquals("3", response.getHeader("X-Cocoon-Version"));

        String profilingId = response.getHeader(X_COCOON_PROFILING_ID);
        assertNotNull(profilingId);

        ProfilingDataHolder dataHolder = (ProfilingDataHolder) applicationContext
                .getBean("org.apache.cocoon.profiling.data.ProfilingDataHolder");

        ProfilingData profilingData = dataHolder.get(profilingId);

        assertTrue(profilingData.getChildren().size() > 0);
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("wildcard", null);
        assertTrue(this.containsProfilingData("MatchNode", profilingData, attributes));

        // check the nesting of profiling data objects
        assertTrue(this.checkProfilingDataStructure(profilingData, 0));

        // check parent and child references
        assertTrue(this.checkProfilingDataReferences(profilingData));
    }

    @Test
    public void testMatchRegExp() throws Exception {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("param1", 29);

        MockHttpServletResponse response = this.invokeServlet("/advanced-matching", parameters);

        assertEquals("3", response.getHeader("X-Cocoon-Version"));

        String profilingId = response.getHeader(X_COCOON_PROFILING_ID);
        assertNotNull(profilingId);

        ProfilingDataHolder dataHolder = (ProfilingDataHolder) applicationContext
                .getBean("org.apache.cocoon.profiling.data.ProfilingDataHolder");

        ProfilingData profilingData = dataHolder.get(profilingId);

        assertTrue(profilingData.getChildren().size() > 0);
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("regexp", null);
        assertTrue(this.containsProfilingData("MatchNode", profilingData, attributes));
    }

    @Test
    public void testProfilingDataStructure() throws Exception {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("param1", 29);

        MockHttpServletResponse response = this.invokeServlet("/advanced-matching", parameters);

        assertEquals("3", response.getHeader("X-Cocoon-Version"));

        String profilingId = response.getHeader(X_COCOON_PROFILING_ID);
        assertNotNull(profilingId);

        ProfilingDataHolder dataHolder = (ProfilingDataHolder) applicationContext
                .getBean("org.apache.cocoon.profiling.data.ProfilingDataHolder");

        ProfilingData profilingData = dataHolder.get(profilingId);

        // check the nesting of profiling data objects
        assertTrue(this.checkProfilingDataStructure(profilingData, 0));

        // check parent and child references
        assertTrue(this.checkProfilingDataReferences(profilingData));

        // check invocation times
        assertTrue(this.checkProfilingDataInvocationTimes(profilingData, null));
    }

    /**
     * checks the order of the PD objects according to invocation time
     */
    private boolean checkProfilingDataInvocationTimes(ProfilingData data, ProfilingData data2) {
        for (ProfilingData d : data.getChildren()) {
            if (d.getInvocationStartTime() < data.getInvocationStartTime()) {
                return false;
            }

            if (data2 != null && d.getInvocationStartTime() > data2.getInvocationStartTime()) {
                return false;
            }
            for (int i = 0; i < data.getChildren().size() - 1; i++) {
                if (!this.checkProfilingDataInvocationTimes(data.getChildren().get(i), data.getChildren().get(i + 1))) {
                    return false;
                }
            }
            if (!this.checkProfilingDataInvocationTimes(data.getChildren().get(data.getChildren().size() - 1), null)) {
                return false;
            }
        }
        return true;
    }

    /**
     * checks the nesting of the ProfilingData objects
     */
    private boolean checkProfilingDataStructure(ProfilingData data, int level) {
        if (data.getInvocationDepth() != level) {
            return false;
        }
        for (ProfilingData d : data.getChildren()) {
            if (!this.checkProfilingDataStructure(d, level + 1)) {
                return false;
            }
        }
        return true;
    }

    /**
     * checks parent and child references
     */
    private boolean checkProfilingDataReferences(ProfilingData data) {
        for (ProfilingData d : data.getChildren()) {
            if (d.getParent() != data) {
                return false;
            }
            if (!this.checkProfilingDataReferences(d)) {
                return false;
            }
        }
        return true;
    }

    private MockHttpServletResponse invokeServlet(String requestURI, Map<String, Object> data) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(data);

        request.setRequestURI(requestURI);

        HttpContextHelper.storeRequest(request, data);
        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpContextHelper.storeResponse(response, data);

        // Invocation invocation = this.buildInvocation(requestURI);

        // HttpContextHelper.storeRequest(request, invocation.getParameters());

        // servlet.invoke(requestURI, data, System.out);
        this.servlet.service(request, response);

        return response;
    }

    Invocation buildInvocation(String request, Map<String, Object> params) {
        InvocationImpl invocation = new InvocationImpl(this.output);

        invocation.setBaseURL(this.getClass().getResource("/COB-INF/"));
        invocation.setRequestURI(request);
        invocation.setComponentProvider(componentProvider);
        invocation.setObjectModel(new ObjectModel(params));

        return invocation;
    }

    private boolean containsProfilingData(String expectedDisplayName, ProfilingData profilingData,
            Map<String, String> attributes) {
        String displayName = profilingData.getDisplayName();
        if (displayName != null && displayName.contains(expectedDisplayName)) {
            // TODO: check attributes
            return true;
        }

        for (ProfilingData pd : profilingData.getChildren()) {
            if (this.containsProfilingData(expectedDisplayName, pd, attributes)) {
                return true;
            }
        }

        return false;
    }
}
