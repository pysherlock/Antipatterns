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
package org.apache.cocoon.optional.pipeline.components.sax.jaxb;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @version $Id: GenericType.java 1133798 2011-06-09 11:30:49Z simonetripodi $
 */
public abstract class GenericType<T> {

    public static <T> GenericType<T> toGenericType(T t) {
        if (t == null) {
            throw new IllegalArgumentException("Parameter 't' must not be null");
        }
        return new GenericType<T>(t) {};
    }

    private final T object;

    private final Class<?> rawType;

    private final Class<?> type;

    public GenericType(T object) {
        this.object = object;
        this.rawType = object.getClass();

        Type superclass = this.getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        Type type = parameterized.getActualTypeArguments()[0];
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            this.type = (Class<?>) actualTypeArguments[0];
        } else {
            this.type = null;
        }
    }

    public final T getObject() {
        return this.object;
    }

    public final Class<?> getRawType() {
        return this.rawType;
    }

    public final Class<?> getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return "{ object="
                + this.object
                + ", rawType="
                + this.rawType
                + ", type="
                + this.type
                + " }";
    }

}
