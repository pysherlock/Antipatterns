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
package org.apache.cocoon.rest.controller.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.rest.controller.annotation.BaseURL;
import org.apache.cocoon.rest.controller.annotation.Inject;
import org.apache.cocoon.rest.controller.annotation.RequestHeader;
import org.apache.cocoon.rest.controller.annotation.RequestParameter;
import org.apache.cocoon.rest.controller.annotation.SitemapParameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AnnotationCollector {

    private static final Log LOG = LogFactory.getLog(AnnotationCollector.class);

    private Map<Class<?>, Map<Class<? extends Annotation>, List<Field>>> annotatedFieldsCache = new HashMap<Class<?>, Map<Class<? extends Annotation>, List<Field>>>();
    private Set<Class<? extends Annotation>> relevantAnnotations;

    public AnnotationCollector() {
        super();

        this.relevantAnnotations = new HashSet<Class<? extends Annotation>>();
        this.relevantAnnotations.add(BaseURL.class);
        this.relevantAnnotations.add(Inject.class);
        this.relevantAnnotations.add(RequestHeader.class);
        this.relevantAnnotations.add(RequestParameter.class);
        this.relevantAnnotations.add(SitemapParameter.class);
    }

    public synchronized Map<Class<? extends Annotation>, List<Field>> getAnnotatedFields(Class<?> type) {
        Map<Class<? extends Annotation>, List<Field>> result = this.annotatedFieldsCache.get(type);

        if (result == null) {
            result = this.collectAnnotatedFields(type);
            this.annotatedFieldsCache.put(type, result);
        }

        return result;
    }

    private Map<Class<? extends Annotation>, List<Field>> collectAnnotatedFields(Class<?> type) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Collecting annotations of class " + type + ".");
        }

        Map<Class<? extends Annotation>, List<Field>> result = new HashMap<Class<? extends Annotation>, List<Field>>();

        for (Field field : this.getFields(type)) {
            for (Annotation availableAnnotation : field.getAnnotations()) {
                Class<? extends Annotation> annotationType = availableAnnotation.annotationType();
                if (!this.relevantAnnotations.contains(annotationType)) {
                    continue;
                }

                List<Field> annotatedFields = result.get(annotationType);
                if (annotatedFields == null) {
                    annotatedFields = new LinkedList<Field>();
                    result.put(annotationType, annotatedFields);
                }

                annotatedFields.add(field);
            }
        }

        return result;
    }

    private List<Field> getFields(Class<?> type) {
        List<Field> result = new LinkedList<Field>();

        Class<?> currentClass = type;
        while (true) {
            Field[] declaredFields = currentClass.getDeclaredFields();

            for (Field declaredField : declaredFields) {
                if (Modifier.isStatic(declaredField.getModifiers())) {
                    continue;
                }

                result.add(declaredField);
            }

            currentClass = currentClass.getSuperclass();
            if (currentClass == null) {
                break;
            }
        }

        return result;
    }
}
