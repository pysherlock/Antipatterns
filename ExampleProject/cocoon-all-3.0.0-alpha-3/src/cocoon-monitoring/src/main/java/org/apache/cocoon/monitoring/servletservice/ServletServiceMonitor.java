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
package org.apache.cocoon.monitoring.servletservice;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.cocoon.servletservice.ServletServiceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * JMX MBean class that expose base information about Servlet-Services.
 */
@ManagedResource
public class ServletServiceMonitor {

    private final Log logger = LogFactory.getLog(this.getClass());
    private final Servlet servlet;

    public ServletServiceMonitor(Servlet servlet) {
        this.servlet = servlet;
    }

    /**
     * Returns all ServletService init parameters, excluding null parameters.
     *
     * @return all ServletService init parameters
     */
    @ManagedAttribute(description = "Returns all ServletService init parameters")
    public String[] getServletServiceInitParameters() {
        ServletConfig config = this.servlet.getServletConfig();
        @SuppressWarnings("unchecked")
        Enumeration<String> initParameterNames = config.getInitParameterNames();
        List<String> result = new ArrayList<String>();

        while (initParameterNames.hasMoreElements()) {
            String name = initParameterNames.nextElement();
            String parameter = config.getInitParameter(name);
            if (parameter != null) { // omit null parameters
                result.add(name + " = " + parameter);
            }
        }

        return result.toArray(new String[] {});
    }

    /**
     * Returns list of connections for this Servlet-Service.
     *
     * @param servletServiceName
     * @return <code>String</code> array of connections for this Servlet-Service
     */
    /*
     * TODO This method uses a private accessor to access the connections field of the
     * ServletServiceContext. The servlet-service-fw-impl > 1.2.0 will expose them.
     */
    @SuppressWarnings("unchecked")
    @ManagedAttribute(description = "Returns list of connections for this Servlet-Service")
    public final String[] getServletServiceConnections() {
        ServletContext servletContext = this.servlet.getServletConfig().getServletContext();
        ServletServiceContext servletServiceContext = (ServletServiceContext) servletContext;

        Map<String, String> servletServiceConnections = null;
        try {
            Field connectionsField = servletServiceContext.getClass().getDeclaredField("connectionServiceNames");
            connectionsField.setAccessible(true);
            servletServiceConnections = (Map<String, String>) connectionsField.get(unpackProxy(servletServiceContext));
        } catch (Exception e) {
            this.logger.warn("Can't access the connections field of " + servletContext + ".", e);
        }

        if (servletServiceConnections != null) {
            String connections[] = new String[servletServiceConnections.size()];
            int i = 0;
            for (String key : servletServiceConnections.keySet()) {
                connections[i] = key + " = " + servletServiceConnections.get(key);
                i++;
            }
            return connections;
        } else {
            return new String[] {};
        }
    }

    /**
     * Returns information about this Servlet-Service, such as author, version, and copyright.
     *
     * @param servletServiceName
     * @return information about this Servlet-Service
     */
    @ManagedAttribute(description = "Returns information about this Servlet-Service")
    public final String getServletServiceInfo() {
        return this.servlet.getServletInfo();
    }

    /**
     * Get Servlet-Services month path.
     *
     * @return month path
     */
    @ManagedAttribute(description = "Returns list of registered Servlet-Services with their month paths")
    public final String getServletServiceMountPaths() {
        ServletContext servletContext = this.servlet.getServletConfig().getServletContext();
        ServletServiceContext context = (ServletServiceContext) servletContext;
        return context.getMountPath();
    }

    @SuppressWarnings("unchecked")
    private static <T> T unpackProxy(T proxy) throws Exception {
        if (proxy instanceof Advised) {
            Advised advised = (Advised) proxy;
            return (T) advised.getTargetSource().getTarget();
        }

        return proxy;
    }
}
