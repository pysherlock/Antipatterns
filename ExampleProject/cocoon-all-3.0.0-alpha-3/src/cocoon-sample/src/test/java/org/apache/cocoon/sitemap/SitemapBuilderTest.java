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
package org.apache.cocoon.sitemap;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.cocoon.sample.action.CustomException;
import org.apache.cocoon.servlet.collector.ResponseHeaderCollector;
import org.apache.cocoon.servlet.util.HttpContextHelper;
import org.apache.cocoon.sitemap.node.InvocationResult;
import org.apache.cocoon.sitemap.node.SitemapNode;
import org.apache.cocoon.sitemap.objectmodel.ObjectModel;
import org.apache.commons.io.output.NullOutputStream;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SitemapBuilderTest extends TestCase {

    private SitemapNode sitemap;
    private SitemapBuilder sitemapBuilder;
    private ComponentProvider componentProvider;

    public void testController() throws Exception {
        Invocation invocation = this.buildInvocation("controller/invoke");
        InvocationResult invocationResult = this.sitemap.invoke(invocation);
        assertNotNull(invocationResult);
        assertTrue(invocationResult.isCompleted());
    }

    public void testErrorHandlingPipeline() throws Exception {
        Invocation invocation = this.buildInvocation("error-handling/custom-error-per-pipeline-error-handling");
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        HttpContextHelper.storeResponse(mockHttpServletResponse, invocation.getParameters());

        this.sitemap.invoke(invocation);

        // invocation should be marked as error-invocation
        assertTrue(invocation.isErrorInvocation());

        // the throwable should be our exception
        assertTrue("Expected CustomException but received " + invocation.getThrowable(),
                invocation.getThrowable() instanceof CustomException);

        assertEquals(501, ResponseHeaderCollector.getStatusCode());
    }

    public void testExpressionLanguage() {
        Invocation invocation = this.buildInvocation("expression-language/map-numbers/simple");
        this.sitemap.invoke(invocation);
        // invocation should not be marked as error-invocation
        assertFalse("InvocationImpl is marked as erroneous", invocation.isErrorInvocation());
    }

    public void testExpressionLanguage2() {
        Invocation invocation = this.buildInvocation("expression-language/nested/simple");
        this.sitemap.invoke(invocation);
        // invocation should not be marked as error-invocation
        assertFalse("InvocationImpl is marked as erroneous", invocation.isErrorInvocation());
    }

    public void testGenerator() throws Exception {
        Invocation invocation = this.buildInvocation("sax-pipeline/unauthorized");
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        HttpContextHelper.storeResponse(mockHttpServletResponse, invocation.getParameters());

        InvocationResult invocationResult = this.sitemap.invoke(invocation);
        assertNotNull(invocationResult);
        assertSame(InvocationResult.COMPLETED, invocationResult);

        // invocation should not be marked as error-invocation
        assertFalse(invocation.isErrorInvocation());
        assertEquals(401, ResponseHeaderCollector.getStatusCode());
    }

    public void testNoMatchingPipeline() throws Exception {
        Invocation invocation = this.buildInvocation("unknown");
        InvocationResult invocationResult = this.sitemap.invoke(invocation);

        assertNotNull(invocationResult);
        assertTrue(invocation.isErrorInvocation());
        assertTrue("Expected NoMatchingPipelineException but received " + invocation.getThrowable(), invocation
                .getThrowable() instanceof NoMatchingPipelineException);
    }

    public void testObjectModelPipeline() {
        Invocation invocation = this.buildInvocation("object-model/request-parameters");
        Map<String, String> requestParameters = new HashMap<String, String>();
        requestParameters.put("a", "1");
        requestParameters.put("b", "2");
        requestParameters.put("c", "3");
        HttpContextHelper.storeRequest(new MockHttpServletRequest(requestParameters), invocation.getParameters());
        this.sitemap.invoke(invocation);

        // invocation not should be marked as error-invocation
        assertFalse(invocation.isErrorInvocation());
    }

    public void testReadPipelineExplicit() {
        Invocation invocation = this.buildInvocation("read/javascript-resource-explicit");
        assertTrue(this.sitemap.invoke(invocation).isCompleted());

        // invocation should not be marked as error-invocation
        assertFalse(invocation.isErrorInvocation());

        invocation = this.buildInvocation("read/javascript-resource-explicit");
        assertTrue(this.sitemap.invoke(invocation).isCompleted());

        // invocation should not be marked as error-invocation
        assertFalse(invocation.isErrorInvocation());
    }

    public void testReadPipelineImplicit() {
        Invocation invocation = this.buildInvocation("read/javascript-resource-implicit.js");
        assertTrue(this.sitemap.invoke(invocation).isCompleted());

        // invocation should not be marked as error-invocation
        assertFalse(invocation.isErrorInvocation());
    }

    public void testRedirectPipeline() {
        Invocation invocation = this.buildInvocation("redirect/www.orf.at");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(HttpServletResponse.class.getName(), response);
        invocation.setParameters(parameters);

        assertTrue(this.sitemap.invoke(invocation).isCompleted());

        // invocation should not be marked as error-invocation
        assertFalse("InvocationImpl is marked as erroneous.", invocation.isErrorInvocation());
        assertTrue(response.hasRedirected());
    }

    public void testXSLT() throws Exception {
        Invocation invocation = this.buildInvocation("xslt/main");
        InvocationResult invocationResult = this.sitemap.invoke(invocation);
        assertNotNull(invocationResult);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] {
                "cocoon-sample-test-collector.xml",

                "cocoon-sample-test-spring-configurator.xml",

                "META-INF/cocoon/spring/cocoon-pipeline.xml",

                "META-INF/cocoon/spring/cocoon-pipeline-action.xml",

                "META-INF/cocoon/spring/cocoon-pipeline-component.xml",

                "META-INF/cocoon/spring/cocoon-sitemap.xml",

                "META-INF/cocoon/spring/cocoon-sitemap-node.xml",

                "META-INF/cocoon/spring/cocoon-expression-language.xml",

                "META-INF/cocoon/spring/cocoon-sample-sitemap-components.xml",

                "META-INF/cocoon/spring/cocoon-servlet-component.xml",

                "META-INF/cocoon/spring/cocoon-controller.xml"

        });

        this.componentProvider = (ComponentProvider) applicationContext
        .getBean("org.apache.cocoon.sitemap.ComponentProvider");

        URL sitemapURL = this.getClass().getResource("/COB-INF/sitemap.xmap");
        this.sitemapBuilder = (SitemapBuilder) applicationContext.getBean("org.apache.cocoon.sitemap.SitemapBuilder");
        this.sitemap = this.sitemapBuilder.build(sitemapURL);
    }

    private Invocation buildInvocation(String request) {
        InvocationImpl invocation = new InvocationImpl(new NullOutputStream());

        invocation.setBaseURL(this.getClass().getResource("/COB-INF/"));
        invocation.setRequestURI(request);
        invocation.setComponentProvider(this.componentProvider);
        invocation.setObjectModel(new ObjectModel(new HashMap<String, Object>()));

        return invocation;
    }
}
