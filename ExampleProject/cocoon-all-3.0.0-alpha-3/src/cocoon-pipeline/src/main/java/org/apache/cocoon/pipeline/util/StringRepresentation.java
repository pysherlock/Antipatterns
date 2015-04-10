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
package org.apache.cocoon.pipeline.util;

/**
 * Helper class to create the {@link String} for {@link Object#toString()}.
 */
public abstract class StringRepresentation {

    public static String buildString(Object instance, String... additionalOutputStrings) {
        StringBuilder sb = new StringBuilder();
        sb.append(instance.getClass().getSimpleName());
        sb.append("(hashCode=").append(System.identityHashCode(instance));

        if (additionalOutputStrings != null) {
            for (String outputString : additionalOutputStrings) {
                sb.append(" ").append(outputString);
            }
        }
        sb.append(")");

        return sb.toString();
    }

}
