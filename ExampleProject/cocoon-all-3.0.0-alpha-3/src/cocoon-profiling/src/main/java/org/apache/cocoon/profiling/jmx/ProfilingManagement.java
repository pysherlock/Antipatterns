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
package org.apache.cocoon.profiling.jmx;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * This MBean provides management functionality for cocoon-profiling and offers a possibility to
 * enable/disable cocoon profiling.
 *
 * If you want to test this, simply run cocoon-sample and connect with jconsole.
 */
@ManagedResource(objectName = "org.apache.cocoon:group=Profiling,name=Enable/Disable")
public class ProfilingManagement {

    private boolean enabled = true;

    @ManagedAttribute(description = "Is profiling enabled.")
    public boolean isEnabled() {
        return this.enabled;
    }

    @ManagedOperation(description = "Enable profiling.")
    public void enable() {
        this.enabled = true;
    }

    @ManagedOperation(description = "Disable profiling.")
    public void disable() {
        this.enabled = false;
    }
}
