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
package org.apache.cocoon.rest.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.cocoon.rest.controller.annotation.RequestHeader;
import org.apache.cocoon.rest.controller.annotation.RequestParameter;
import org.apache.cocoon.rest.controller.util.AnnotationCollector;
import org.junit.Test;

public class AnnotationCollectorTest {

    @Test
    public void testGetAnnotatedFields() {
        AnnotationCollector annotationCollector = new AnnotationCollector();
        Map<Class<? extends Annotation>, List<Field>> annotatedFields = annotationCollector
                .getAnnotatedFields(AnnotatedClass.class);

        List<Field> requestHeaderParameterFields = annotatedFields.get(RequestHeader.class);
        List<Field> requestParameterFields = annotatedFields.get(RequestParameter.class);

        Assert.assertEquals(1, requestHeaderParameterFields.size());
        Assert.assertEquals("userAgent", requestHeaderParameterFields.get(0).getName());
        Assert.assertEquals(3, requestParameterFields.size());
    }
}
