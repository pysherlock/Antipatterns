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
package org.apache.cocoon.rest.jaxrs.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ClassUtils;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * A {@link Servlet} that can be used in the Cocoon Servlet-Service framework. Extending the
 * {@link ServletContainer} provided by Jersey it registers all passed REST resources (
 * {@link #setRestResources(Map)} and {@link #setRestResourcesList(List)}.
 */
public class CocoonJAXRSServlet extends ServletContainer {

    private static final long serialVersionUID = -8658985429213333769L;
    private boolean lazyInit;
    private final Log logger = LogFactory.getLog(this.getClass());
    private ResourceConfig rc;
    private List<Object> restResourcesList;
    private Map<Object, Object> restResourcesMap;
    private WebApplication wa;

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!this.lazyInit) {
            this.lazyInitiate();
        }

        super.service(request, response);
    }

    public void setRestResourcesMap(Map<Object, Object> restResourcesMap) {
        this.restResourcesMap = restResourcesMap;
    }

    public void setRestResourcesList(List<Object> restResourcesList) {
        this.restResourcesList = restResourcesList;
    }

    @Override
    protected ResourceConfig getDefaultResourceConfig(Map<String, Object> props, ServletConfig servletConfig)
            throws ServletException {
        DefaultResourceConfig rc = new DefaultResourceConfig();
        rc.setPropertiesAndFeatures(props);
        return rc;
    }

    @Override
    protected void initiate(ResourceConfig rc, WebApplication wa) {
        this.rc = rc;
        this.wa = wa;
    }

    /*
     * Only initialize this servlet once.
     */
    private synchronized void lazyInitiate() {
        if (this.lazyInit) {
            return;
        }

        List<Object> restResources = new ArrayList<Object>();
        if (this.restResourcesMap != null) {
            restResources.addAll(this.restResourcesMap.values());
        }
        if (this.restResourcesList != null) {
            restResources.addAll(this.restResourcesList);
        }

        for (Object bean : restResources) {
            Class<?> type = ClassUtils.getUserClass(bean);
            if (ResourceConfig.isProviderClass(type)) {
                this.logger.info("Registering Spring bean of type " + type.getName() + " as a provider class");
                // this.rc.getClasses().add(type);
                this.rc.getSingletons().add(bean);
            } else if (ResourceConfig.isRootResourceClass(type)) {
                this.logger.info("Registering Spring bean of type " + type.getName() + " as a root resource class");
                // this.rc.getClasses().add(type);
                this.rc.getSingletons().add(bean);
            }
        }

        this.wa.initiate(this.rc);
        this.lazyInit = true;
    }
}