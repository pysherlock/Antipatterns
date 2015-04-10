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

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.pipeline.component.Finisher;
import org.apache.cocoon.pipeline.component.PipelineComponent;
import org.apache.cocoon.sitemap.action.Action;
import org.apache.cocoon.sitemap.expression.LanguageInterpreter;
import org.apache.cocoon.sitemap.objectmodel.ObjectModel;
import org.apache.cocoon.sitemap.util.ExceptionHandler;
import org.apache.cocoon.sitemap.util.ParameterHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InvocationImpl implements Invocation {

    private final Log errorLogger = LogFactory.getLog(this.getClass().getName() + "/handle-errors");

    private static final Pattern PARAMETER_PATTERN = Pattern.compile("\\{([a-zA-Z\\-]+):([^\\{]*)\\}");

    private List<Action> actions = new LinkedList<Action>();

    private ComponentProvider componentProvider;

    private OutputStream outputStream;

    private Map<String, Object> parameters = new HashMap<String, Object>();

    private Pipeline<PipelineComponent> pipeline;

    private String requestURI;

    private URL baseURL;

    private ObjectModel objectModel;

    private boolean hasFinisher;

    public InvocationImpl() {
    }

    /**
     * Create a {@link InvocationImpl} object using the given output stream.
     *
     * @param outputStream The {@link OutputStream} where the result is written
     *            to.
     */
    public InvocationImpl(OutputStream outputStream) {
        super();
        this.outputStream = outputStream;
    }

    /**
     * Create a {@link InvocationImpl} object using the given output stream and
     * requestURI.
     *
     * @param outputStream The {@link OutputStream} where the result is written
     *            to.
     * @param requestURI The requested path.
     */
    public InvocationImpl(OutputStream outputStream, String requestURI) {
        super();
        this.outputStream = outputStream;
        this.requestURI = requestURI;
    }

    /**
     * Create a {@link InvocationImpl} object using the given output stream,
     * requestURI and parameters.
     *
     * @param outputStream The {@link OutputStream} where the result is written
     *            to.
     * @param requestURI The requested path.
     * @param parameters A {@link Map} of parameters that are used when the
     *            pipeline is being executed.
     */
    public InvocationImpl(OutputStream outputStream, String requestURI, Map<String, Object> parameters) {
        super();
        this.outputStream = outputStream;
        this.requestURI = requestURI;
        this.parameters = parameters;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.Invocation#reset()
     */
    public void reset() {
        this.actions.clear();
        this.hasFinisher = false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.Invocation#execute()
     */
    public void execute() throws Exception {
        if (this.pipeline == null) {
            throw new IllegalStateException("InvocationImpl has been executed without having a pipeline.");
        }

        // first setup everything
        for (final Action action : this.actions) {
            action.setup(this.parameters);
        }
        this.pipeline.setup(this.outputStream, this.parameters);

        // then execute
        for (final Action action : this.actions) {
            action.execute();
        }
        this.pipeline.execute();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.Invocation#getOutputStream()
     */
    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.Invocation#getParameter(java.lang.String)
     */
    public Object getParameter(String name) {
        if (this.parameters == null) {
            return null;
        }
        return this.parameters.get(name);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.Invocation#getParameters()
     */
    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.Invocation#getRequestURI()
     */
    public String getRequestURI() {
        return this.requestURI;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.Invocation#getThrowable()
     */
    public Throwable getThrowable() {
        return ParameterHelper.getThrowable(this.parameters);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.Invocation#hasCompletePipeline()
     */
    public boolean hasCompletePipeline() {
        return this.hasFinisher;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.Invocation#installAction(java.lang.String)
     */
    public void installAction(String type) {
        if (this.pipeline == null) {
            throw new IllegalStateException("Action cannot be installed without having a pipeline.");
        }

        Action action = this.componentProvider.createAction(type);
        this.actions.add(action);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.Invocation#installComponent(java.lang.String,
     *      java.util.Map)
     */
    public void installComponent(String type, Map<String, ? extends Object> componentParameters) {
        if (this.pipeline == null) {
            throw new IllegalStateException("Pipeline component cannot be installed without having a pipeline.");
        }

        PipelineComponent component = this.componentProvider.createComponent(type);
        Map<String, ? extends Object> resolvedParameters = this.resolveParameters(componentParameters);
        component.setConfiguration(resolvedParameters);
        this.pipeline.addComponent(component);

        if (component instanceof Finisher) {
            this.hasFinisher = true;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.Invocation#installPipeline(java.lang.String,
     *      java.util.Map)
     */
    public void installPipeline(String type, Map<String, ? extends Object> componentParameters) {
        this.pipeline = this.componentProvider.createPipeline(type);
        Map<String, ? extends Object> resolvedParameters = this.resolveParameters(componentParameters);
        this.pipeline.setConfiguration(resolvedParameters);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.Invocation#isErrorInvocation()
     */
    public boolean isErrorInvocation() {
        return this.getThrowable() != null;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.Invocation#popSitemapParameters()
     */
    public void popSitemapParameters() {
        this.objectModel.getSitemapParameters().popParameters();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.Invocation#pushSitemapParameters(java.lang.String,
     *      java.util.Map)
     */
    public void pushSitemapParameters(String nodeName, Map<String, ? extends Object> sitemapParameters) {
        this.objectModel.getSitemapParameters().pushParameters(nodeName, sitemapParameters);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.Invocation#resolve(java.lang.String)
     */
    public URL resolve(String resource) {
        try {
            return new URL(this.baseURL, resource);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setComponentProvider(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.Invocation#setOutputStream(java.io.OutputStream)
     */
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.Invocation#setParameters(java.util.Map)
     */
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.Invocation#setThrowable(java.lang.Throwable)
     */
    public void setThrowable(final Throwable throwable) {
        Throwable cause = ExceptionHandler.getCause(throwable);

        this.objectModel.getCocoonObject().put("exception", cause);
        ParameterHelper.setThrowable(this.parameters, cause);

        String message = "Error while executing the sitemap. [request-uri=" + this.getRequestURI() + "]";
        this.errorLogger.error(message, throwable);
    }

    private LanguageInterpreter getLanguageInterpreter(final String language) {
        return this.componentProvider.getLanguageInterpreter(language);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.sitemap.Invocation#resolveParameter(java.lang.String)
     */
    public String resolveParameter(final String parameter) {
        if (parameter == null) {
            return null;
        }

        final StringBuilder result = new StringBuilder(parameter);
        final Matcher matcher = PARAMETER_PATTERN.matcher(result);

        while (matcher.find()) {
            final String language = matcher.group(1);
            final LanguageInterpreter languageInterpreter = this.getLanguageInterpreter(language);
            if (languageInterpreter == null) {
                throw new UnsupportedExpressionLanguageException("Could not resolve parameter '" + parameter
                        + "'. The language '" + language + "' is not supported.");
            }

            final String variable = matcher.group(2);
            final String replacement = languageInterpreter.resolve(variable, this.objectModel);

            if (replacement != null) {
                result.replace(matcher.start(), matcher.end(), replacement);
            } else {
                throw new VariableNotFoundException("Variable {" + language + ":" + variable
                        + "} not found or is null.");
            }
            matcher.reset();
        }

        return result.toString();
    }

    private Map<String, ? extends Object> resolveParameters(final Map<String, ? extends Object> componentParameters) {
        final Map<String, Object> resolvedParameters = new HashMap<String, Object>();

        for (String key : componentParameters.keySet()) {
            final Object parameter = componentParameters.get(key);

            if (!(parameter instanceof String)) {
                // can only resolve strings
                resolvedParameters.put(key, parameter);
                continue;
            }

            final String resolvedParameter = this.resolveParameter((String) parameter);
            resolvedParameters.put(key, resolvedParameter);
        }

        return resolvedParameters;
    }

    public void setBaseURL(URL baseURL) {
        this.baseURL = baseURL;
    }

    public void setObjectModel(ObjectModel objectModel) {
        this.objectModel = objectModel;
    }

    public class UnsupportedExpressionLanguageException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public UnsupportedExpressionLanguageException(String msg) {
            super(msg);
        }
    }

    public class VariableNotFoundException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public VariableNotFoundException(String msg) {
            super(msg);
        }
    }
}