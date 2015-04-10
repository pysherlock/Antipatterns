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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.cocoon.servletservice.ServletServiceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jmx.export.MBeanExporter;

public class ServletServiceMonitorInitializer {

    private final Log logger = LogFactory.getLog(this.getClass());

    public ServletServiceMonitorInitializer(Map<String, Servlet> servlets, MBeanExporter exporter) {
        List<String> servletNames = new ArrayList<String>();
        for (Servlet servlet : servlets.values()) {
            ServletConfig servletConfig = servlet.getServletConfig();

            // get only ServletServices servlets
            ServletContext servletContext = servletConfig.getServletContext();
            String servletName = servletConfig.getServletName();
            if (!(servletContext instanceof ServletServiceContext)) {
                this.logger.info(servletName + " isn't Servlet-Service servlet, it will be ignored.");
                continue;
            }

            ServletServiceContext servletServiceContext = (ServletServiceContext) servletContext;
            ObjectName name = null;
            String stringName = "org.apache.cocoon:group=ServletServices,name=["
                    + this.getMountPath(servletServiceContext) + "] " + servletName;
            try {
                name = new ObjectName(stringName);
            } catch (MalformedObjectNameException e) {
                this.logger.error("The string passed as a parameter does not have the right format.", e);
                continue;
            } catch (NullPointerException e) {
                this.logger.fatal("Should never happened. Value of name parameter always is different than null.", e);
                continue;
            }

            if (name == null) {
                this.logger.error("ObjectName is null, something strange happen. Should never happen.");
                continue;
            }

            ServletServiceMonitor servletServiceMonitor = new ServletServiceMonitor(servlet);
            if (!servletNames.contains(stringName)) {
                exporter.registerManagedResource(servletServiceMonitor, name);
                servletNames.add(stringName);
            }
        }
    }

    private String getMountPath(ServletServiceContext servletServiceContext) {
        String mountPath = servletServiceContext.getMountPath();
        if (mountPath != null && !mountPath.startsWith("/")) {
            return "/" + mountPath;
        }

        if (mountPath == null) {
            return "not mounted";
        }

        return mountPath;
    }
}
