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
package org.apache.cocoon.profiling.data;

import java.io.Serializable;

/**
 * A data class to store the necessary information used for profiling of an object instance.
 */
public class InstanceRepresentation implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Class<?> clazz;
    private final String value;

    public InstanceRepresentation(Object o) {
        this.value = o == null ? "(null)" : o.toString();
        this.clazz = o == null ? null : o.getClass();
    }

    public Class<?> getRepresentedClass() {
        return this.clazz;
    }

    public String getStringRepresentation() {
        return this.value;
    }
}
