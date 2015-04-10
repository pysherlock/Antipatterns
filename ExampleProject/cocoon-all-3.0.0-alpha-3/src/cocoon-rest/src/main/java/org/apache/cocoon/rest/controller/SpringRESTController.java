/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.rest.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.callstack.CallStack;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.controller.Controller;
import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.rest.controller.annotation.BaseURL;
import org.apache.cocoon.rest.controller.annotation.Inject;
import org.apache.cocoon.rest.controller.annotation.RESTController;
import org.apache.cocoon.rest.controller.annotation.RequestHeader;
import org.apache.cocoon.rest.controller.annotation.RequestParameter;
import org.apache.cocoon.rest.controller.annotation.SitemapParameter;
import org.apache.cocoon.rest.controller.method.ConditionalGet;
import org.apache.cocoon.rest.controller.response.RestResponse;
import org.apache.cocoon.rest.controller.response.RestResponseMetaData;
import org.apache.cocoon.rest.controller.util.AnnotationCollector;
import org.apache.cocoon.servlet.collector.ResponseHeaderCollector;
import org.apache.cocoon.servlet.util.HttpContextHelper;
import org.apache.cocoon.servlet.util.SettingsHelper;
import org.apache.cocoon.sitemap.util.ExceptionHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * <p>
 * This controller is responsible for the execution of instances of REST controller beans. Note that
 * each controller implementation must be available as Spring bean. For that purpose you can use the
 * {@link RESTController} annotation and load all beans from a particular package automatically. See
 * http://static.springframework.org/spring/docs/2.5.x/reference/beans.html#beans-classpath-scanning
 * for details.
 * </p>
 */
public class SpringRESTController implements Controller, ApplicationContextAware {

    private static final String REST_RESPONSE_KEY = SpringRESTController.class.getName() + "/rest-response";
    private static final String CACHE_KEY_KEY = SpringRESTController.class.getName() + "/cache-key";

    private AnnotationCollector annotationCollector;
    private ApplicationContext applicationContext;
    private MethodDelegator methodDelegator;

    private static void storeCacheKey(CacheKey cacheKey) {
        CallStack.getCurrentFrame().setAttribute(CACHE_KEY_KEY, cacheKey);
    }

    private static CacheKey readCacheKey() {
        return (CacheKey) CallStack.getCurrentFrame().getAttribute(CACHE_KEY_KEY);
    }

    private static void storeRestResponse(RestResponse restResponse) {
        CallStack.getCurrentFrame().setAttribute(REST_RESPONSE_KEY, restResponse);
    }

    private static RestResponse readRestResponse() {
        return (RestResponse) CallStack.getCurrentFrame().getAttribute(REST_RESPONSE_KEY);
    }

    public CacheKey getCacheKey() {
        return readCacheKey();
    }

    public void setup(String controllerName, Map<String, Object> inputParameters, Map<String, ? extends Object> configuration) {
        if (!this.applicationContext.isPrototype(controllerName)) {
            throw new ProcessingException("A REST controller bean MUST run within the 'prototype' scope.");
        }

        try {
            // get the prepared controller
            Object controller = this.getController(controllerName, inputParameters, configuration);

            // invoke the appropriate method
            HttpServletRequest request = HttpContextHelper.getRequest(inputParameters);
            RestResponse restResponse = this.methodDelegator.delegate(request, controller);

            if (controller instanceof ConditionalGet) {
                storeCacheKey(((ConditionalGet) controller).constructCacheKey());
            }

            // execute the rest response
            RestResponseMetaData restResponseMetaData = restResponse.setup(inputParameters);

            // set the status-code with the result produced by the restResponse execution
            ResponseHeaderCollector.setStatusCode(restResponseMetaData.getStatusCode());

            // set the content type with the result produced by the restResponse execution
            ResponseHeaderCollector.setMimeType(restResponseMetaData.getContentType());

            // store the rest response for execution
            storeRestResponse(restResponse);
        } catch (Exception e) {
            throw ExceptionHandler.getInvocationException(e);
        }

    }

    public void execute(OutputStream outputStream) {
        try {
            readRestResponse().execute(outputStream);
        } catch (Exception e) {
            throw ExceptionHandler.getInvocationException(e);
        }
    }

    public void setAnnotationCollector(AnnotationCollector annotationCollector) {
        this.annotationCollector = annotationCollector;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setMethodDelegator(MethodDelegator methodDelegator) {
        this.methodDelegator = methodDelegator;
    }

    private Object getController(String controllerName, Map<String, Object> inputParameters,
            Map<String, ? extends Object> configuration) throws Exception {
        Object controller = this.applicationContext.getBean(controllerName);
        Object unpackedController = unpackProxy(controller);
        Map<Class<? extends Annotation>, List<Field>> annotatedFields = this.annotationCollector
                .getAnnotatedFields(unpackedController.getClass());

        // populate the annotated fields
        populateInjectFields(inputParameters, unpackedController, annotatedFields);
        populateRequestFields(inputParameters, unpackedController, annotatedFields);
        populateRequestHeaderFields(inputParameters, unpackedController, annotatedFields);
        populateSitemapParameters(configuration, unpackedController, annotatedFields);
        this.populateBaseURL(configuration, unpackedController, annotatedFields);

        return controller;
    }

    private void populateBaseURL(Map<String, ? extends Object> configuration, Object controller,
            Map<Class<? extends Annotation>, List<Field>> annotatedFields) throws IllegalAccessException, Exception {
        List<Field> baseURLFields = annotatedFields.get(BaseURL.class);
        if (baseURLFields == null || baseURLFields.isEmpty()) {
            return;
        }

        for (Field field : baseURLFields) {
            field.setAccessible(true);

            Class<?> fieldType = field.getType();
            if (fieldType == URL.class) {
                field.set(controller, configuration.get("baseUrl"));
            } else {
                throw new Exception("The annotation " + BaseURL.class.getName() + " can only be set on fields of type "
                        + URL.class.getName() + "." + " " + "(field=" + field.getName() + ", type="
                        + fieldType.getName() + ")");
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T unpackProxy(T proxy) throws Exception {
        if (proxy instanceof Advised) {
            Advised advised = (Advised) proxy;
            return (T) advised.getTargetSource().getTarget();
        }

        return proxy;
    }

    private static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(str.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    private static void populateInjectFields(Map<String, ? extends Object> parameters, Object controller,
            Map<Class<? extends Annotation>, List<Field>> annotatedFields) throws IllegalAccessException, IOException,
            Exception {
        List<Field> injectFields = annotatedFields.get(Inject.class);
        if (injectFields == null || injectFields.isEmpty()) {
            return;
        }

        HttpServletRequest request = HttpContextHelper.getRequest(parameters);
        HttpServletResponse response = HttpContextHelper.getResponse(parameters);

        for (Field field : injectFields) {
            field.setAccessible(true);

            Class<?> fieldType = field.getType();
            if (fieldType == HttpServletRequest.class) {
                field.set(controller, request);
            } else if (fieldType == HttpServletResponse.class) {
                field.set(controller, response);
            } else if (fieldType == Log.class) {
                field.set(controller, LogFactory.getLog(controller.getClass()));
            } else if (fieldType == ServletInputStream.class || fieldType == InputStream.class) {
                field.set(controller, request.getInputStream());
            } else if (fieldType == ServletOutputStream.class || fieldType == OutputStream.class) {
                field.set(controller, response.getOutputStream());
            } else if (fieldType == ServletContext.class) {
                ServletContext servletContext = HttpContextHelper.getServletContext(parameters);
                field.set(controller, servletContext);
            } else if (fieldType == Settings.class) {
                Settings settings = SettingsHelper.getSettings(parameters);
                field.set(controller, settings);
            } else {
                throw new Exception("The annotation " + Inject.class.getName()
                        + " doesn't support the injection of type " + fieldType.getName() + "." + " " + "(field="
                        + field.getName() + ", type=" + fieldType.getName() + ")");
            }
        }
    }

    private static void populateRequestFields(Map<String, ? extends Object> parameters, Object controller,
            Map<Class<? extends Annotation>, List<Field>> annotatedFields) throws IllegalAccessException, Exception {
        List<Field> requestFields = annotatedFields.get(RequestParameter.class);
        if (requestFields == null || requestFields.isEmpty()) {
            return;
        }

        HttpServletRequest request = HttpContextHelper.getRequest(parameters);
        for (Field field : requestFields) {
            field.setAccessible(true);

            String requestParameterName = field.getAnnotation(RequestParameter.class).value();
            if (isBlank(requestParameterName)) {
                requestParameterName = field.getName();
            }

            Class<?> fieldType = field.getType();
            if (fieldType == String.class) {
                String parameter = request.getParameter(requestParameterName);
                if (parameter != null) {
                    field.set(controller, parameter);
                }
            } else if (fieldType == int.class) {
                String parameter = request.getParameter(requestParameterName);
                if (parameter != null) {
                    field.set(controller, Integer.parseInt(parameter));
                }
            } else if (fieldType == boolean.class) {
                String parameter = request.getParameter(requestParameterName);
                if (parameter != null) {
                    field.set(controller, Boolean.parseBoolean(parameter));
                }
            } else if (fieldType == String[].class) {
                String[] parameterValues = request.getParameterValues(requestParameterName);
                if (parameterValues != null) {
                    field.set(controller, parameterValues);
                }
            } else {
                throw new Exception("The annotation " + RequestParameter.class.getName()
                        + " can only be set on fields of type " + String.class.getName() + ", "
                        + String[].class.getName() + ", " + int.class.getName() + " or " + boolean.class.getName()
                        + ". (field=" + field.getName() + ", type=" + fieldType.getName() + ")");
            }
        }
    }

    private static void populateRequestHeaderFields(Map<String, ? extends Object> parameters, Object controller,
            Map<Class<? extends Annotation>, List<Field>> annotatedFields) throws IllegalAccessException, Exception {
        List<Field> requestHeaderFields = annotatedFields.get(RequestHeader.class);
        if (requestHeaderFields == null || requestHeaderFields.isEmpty()) {
            return;
        }

        HttpServletRequest request = HttpContextHelper.getRequest(parameters);
        for (Field field : requestHeaderFields) {
            field.setAccessible(true);

            String name = field.getAnnotation(RequestHeader.class).value();
            if (isBlank(name)) {
                name = field.getName();
            }

            Class<?> fieldType = field.getType();
            if (fieldType == String.class) {
                String header = request.getHeader(name);
                if (header != null) {
                    field.set(controller, header);
                }
            } else {
                throw new Exception("The annotation " + RequestHeader.class.getName()
                        + " can only be set on fields of type " + String.class.getName() + "." + " " + "(field="
                        + field.getName() + ", type=" + fieldType.getName() + ")");
            }
        }
    }

    private static void populateSitemapParameters(Map<String, ? extends Object> configuration, Object controller,
            Map<Class<? extends Annotation>, List<Field>> annotatedFields) throws IllegalAccessException, Exception {
        List<Field> sitemapParameterFields = annotatedFields.get(SitemapParameter.class);
        if (sitemapParameterFields == null || sitemapParameterFields.isEmpty()) {
            return;
        }

        for (Field field : sitemapParameterFields) {
            field.setAccessible(true);

            String name = field.getAnnotation(SitemapParameter.class).value();
            if (isBlank(name)) {
                name = field.getName();
            }

            Class<?> fieldType = field.getType();
            if (fieldType == String.class) {
                field.set(controller, configuration.get(name));
            } else {
                throw new Exception("The annotation " + SitemapParameter.class.getName()
                        + " can only be set on fields of type " + String.class.getName() + "." + " " + "(field="
                        + field.getName() + ", type=" + fieldType.getName() + ")");
            }
        }
    }
}
