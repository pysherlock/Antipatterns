/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.monitoring.reconfiguration;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.cocoon.configuration.MutableSettings;
import org.apache.cocoon.configuration.Settings;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(objectName = "org.apache.cocoon:group=Reconfiguration,name=JmxSpringSettings")
public class JmxSpringSettings {

    private final MutableSettings settings;

    public JmxSpringSettings(Settings settings) {
        this.settings = (MutableSettings) settings;
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getProperty(java.lang.String)
     */
    @ManagedOperation(description = "Get the value of a property.")
    @ManagedOperationParameters(@ManagedOperationParameter(name = "key", description = "The name of the property."))
    public final String getProperty(String key) {
        return this.settings.getProperty(key);
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getPropertyNames(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @ManagedOperation(description = "Return all available property names starting with the prefix.")
    @ManagedOperationParameters(@ManagedOperationParameter(name = "keyPrefix", description = "The prefix each property name must have."))
    public final String[] listPropertys(String keyPrefix) {
        return this.getProperties(this.settings.getPropertyNames(keyPrefix));
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getPropertyNames()
     */
    @SuppressWarnings("unchecked")
    @ManagedOperation(description = "Return all available property names.")
    public final String[] listPropertys() {
        return this.getProperties(this.settings.getPropertyNames());
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getLoadClasses()
     */
    @SuppressWarnings("unchecked")
    @ManagedAttribute(description = "List classes that should be loaded at initialization time of the servlet.")
    public final String[] listLoadClasses() {
        return (String[]) this.settings.getLoadClasses().toArray(new String[] {});
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getFormEncoding()
     */
    @ManagedAttribute(description = "The character set used to decode request parameters.")
    public final String getFormEncoding() {
        return this.settings.getFormEncoding();
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getContainerEncoding()
     */
    @ManagedAttribute(description = "Encoding used by the container.")
    public final String getContainerEncoding() {
        return this.settings.getContainerEncoding();
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getCacheDirectory()
     */
    @ManagedAttribute(description = "Specify where Cocoon should create its page and other objects cache. The path specified can be either absolute or relative to the context path of the servlet.")
    public final String getCacheDirectory() {
        return this.settings.getCacheDirectory();
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getCreationTime()
     */
    @ManagedAttribute(description = "The creation time of the current settings instance.")
    public final String getCreationTime() {
        return new Date(this.settings.getCreationTime()).toString();
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#isReloadingEnabled(java.lang.String)
     */
    @ManagedOperation(description = "This method can be used by components to query if they are configured to check for reloading.")
    @ManagedOperationParameters(@ManagedOperationParameter(name = "type", description = "The type of the component that wants to check for reload."))
    public final boolean isReloadingEnabled(String type) {
        return this.settings.isReloadingEnabled(type);
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getReloadDelay(java.lang.String)
     */
    @ManagedOperation(description = "This method can be used by components to get the configured delay period between checks.")
    public final long getReloadingDelay(String type) {
        return this.settings.getReloadDelay(type);
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getRunningMode()
     */
    @ManagedAttribute(description = "Return the current running mode.")
    public final String getRunningMode() {
        return this.settings.getRunningMode();
    }

    /**
     * Sets new value of property.
     *
     * @param key Property key that value should be changed.
     * @param value New value of property.
     */
    @ManagedOperation(description = "Sets new value of property. (Note: This change is not persistant!)")
    @ManagedOperationParameters( {
            @ManagedOperationParameter(name = "key", description = "Property key that value should be changed."),
            @ManagedOperationParameter(name = "value", description = "New value of property.") })
    public final void setProperty(String key, String value) {
        Properties props = new Properties();
        props.put(key, value);

        this.settings.configure(props);
    }

    /**
     * @see org.apache.cocoon.configuration.MutableSettings#setFormEncoding(java.lang.String)
     */
    @ManagedOperation(description = "Sets form encoding'. (Note: This change is not persistant!)")
    @ManagedOperationParameters(@ManagedOperationParameter(name = "formEncoding", description = "The formEncoding to set."))
    public final void setFormEncoding(String formEncoding) {
        this.settings.setFormEncoding(formEncoding);
    }

    /**
     * @see org.apache.cocoon.configuration.MutableSettings#setContainerEncoding(java.lang.String)
     */
    @ManagedOperation(description = "Set the container encoding.")
    @ManagedOperationParameters(@ManagedOperationParameter(name = "encofing", description = "The new encoding value."))
    public final void setContainerEncoding(String encoding) {
        this.settings.setContainerEncoding(encoding);
    }

    /**
     * @see org.apache.cocoon.configuration.MutableSettings#setReloadingEnabled(boolean)
     */
    @ManagedOperation(description = "Allow reloading. (Note: This change is not persistant!)")
    @ManagedOperationParameters(@ManagedOperationParameter(name = "allowReload", description = "The allowReload to set."))
    public final void setReloadingEnabled(boolean allowReload) {
        this.settings.setReloadingEnabled(allowReload);
    }

    private String[] getProperties(List<String> propertyNames) {
        String[] result = new String[propertyNames.size()];

        for (int i = 0; i < propertyNames.size(); i++) {
            String key = propertyNames.get(i);
            result[i] = key + ": " + this.settings.getProperty(key);
        }

        return result;
    }
}
