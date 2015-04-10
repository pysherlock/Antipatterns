/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.cocoon.stax.stress.src;

/**
 * Enumeration for different states a generator can have.
 */
public enum GenerationState {

    /** Indicates that the document started. */
    START_DOCUMENT,

    /** Indicates the root xml element start. */
    START_ROOT,

    /** Indicates an intermediate xml element start. */
    START_REPEATING_ELEMENT,

    /** Indicates an intermediate xml element end. */
    END_REPEATING_ELEMENT,

    /** Indicates the root xml element end. */
    END_ROOT,

    /** Indicates that the document ends. */
    END_DOCUMENT,

    /** Indicates that the document was created completely. */
    FINISHED
}
