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
package org.apache.cocoon.sitemap.node;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cocoon.sitemap.Invocation;
import org.apache.cocoon.sitemap.node.annotations.NodeChild;
import org.apache.cocoon.sitemap.node.annotations.Parameter;
import org.apache.cocoon.sitemap.util.SpringProxyHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractSitemapNode implements SitemapNode {

    private final Log logger = LogFactory.getLog(this.getClass());

    private final List<SitemapNode> children = new LinkedList<SitemapNode>();

    private final Map<String, String> parameters = new HashMap<String, String>();

    private SitemapNode parent;

    public void addChild(SitemapNode child) {
        if (child == null) {
            throw new IllegalArgumentException(("Node of class " + this.getClass().getName() + " received null child."));
        }

        if (child.getType().equals(ParameterNode.class)) {
            // ParameterNode are to be translated into parameters...
            ParameterNode parameterNode = (ParameterNode) SpringProxyHelper.unpackProxy(child);
            this.processParameter(parameterNode.getName(), parameterNode.getValue());
        } else {
            // check whether there is a special field for this child
            Field childField = this.getChildField(SpringProxyHelper.unpackProxy(child));
            if (childField != null) {
                childField.setAccessible(true);
                try {
                    childField.set(this, SpringProxyHelper.unpackProxy(child));
                } catch (IllegalArgumentException e) {
                    this.logger.error("Failed to set child field for child class '" + child.getClass().getName(), e);
                } catch (IllegalAccessException e) {
                    this.logger.error("Failed to set child field for child class '" + child.getClass().getName(), e);
                }
            } else {
                this.children.add(child);
            }
        }

        child.setParent(this);
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public SitemapNode getParent() {
        return this.parent;
    }

    public InvocationResult invoke(Invocation invocation) {
        InvocationResult result = InvocationResult.NONE;

        for (SitemapNode child : this.children) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug(child + ".invoke(" + invocation.getRequestURI() + ")");
            }

            // aggregate the results of child invocations, the overall result is
            // the maximum of all individual results
            // this is done based on the ordering of the enumeration values
            // which is significant to java.lang.Comparable
            InvocationResult currentResult = child.invoke(invocation);
            if (currentResult.compareTo(result) > 0) {
                result = currentResult;
            }

            if (result.isCompleted() || result == InvocationResult.BREAK) {
                break;
            }
        }

        return result;
    }

    public void setParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            // nothing to do
            return;
        }

        // check for special parameter fields
        Map<String, Field> parameterFields = this.getParameterFields();
        for (Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            Field parameterField = parameterFields.get(key);
            if (parameterField != null) {
                parameterField.setAccessible(true);
                try {
                    parameterField.set(this, value);
                } catch (IllegalArgumentException e) {
                    String message = "Failed to set parameter field " + key;
                    this.logger.error(message, e);
                    throw new RuntimeException(message, e);
                } catch (IllegalAccessException e) {
                    String message = "Failed to set parameter field " + key;
                    this.logger.error(message, e);
                    throw new RuntimeException(message, e);
                }
                continue;
            }

            // default parameter processing
            this.processParameter(key, value);
        }
    }

    public void setParent(SitemapNode parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    protected void processParameter(String key, String value) {
        this.parameters.put(key, value);
    }

    private Field getChildField(SitemapNode child) {
        Class<?> currentClass = this.getClass();

        while (currentClass != null) {
            Field[] declaredFields = currentClass.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                if (declaredField.isAnnotationPresent(NodeChild.class)
                        && declaredField.getType().isAssignableFrom(child.getClass())) {
                    return declaredField;
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        return null;
    }

    private Map<String, Field> getParameterFields() {
        Map<String, Field> parameterFields = new HashMap<String, Field>();

        Class<?> currentClass = this.getClass();
        while (currentClass != null) {
            Field[] declaredFields = currentClass.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                if (declaredField.isAnnotationPresent(Parameter.class)) {
                    String fieldName = this.convertCamelCase(declaredField.getName());
                    parameterFields.put(fieldName, declaredField);
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        return parameterFields;
    }

    private String convertCamelCase(String name) {
        Pattern camelCasePattern = Pattern.compile("(.)([A-Z])");
        Matcher matcher = camelCasePattern.matcher(name);

        int lastMatch = 0;
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            result.append(name.substring(lastMatch, matcher.start()));
            result.append(matcher.group(1));
            result.append("-");
            result.append(matcher.group(2).toLowerCase());
            lastMatch = matcher.end();
        }

        result.append(name.substring(lastMatch, name.length()));

        return result.toString();
    }
}
