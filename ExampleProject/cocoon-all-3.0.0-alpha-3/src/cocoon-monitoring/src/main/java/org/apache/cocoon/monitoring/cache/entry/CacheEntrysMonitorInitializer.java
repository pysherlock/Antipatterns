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
package org.apache.cocoon.monitoring.cache.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.cocoon.pipeline.caching.Cache;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(objectName = "org.apache.cocoon:group=Cache,name=CacheEntrysRefresher")
public class CacheEntrysMonitorInitializer {

    private static final String namePrefix = "org.apache.cocoon:group=Cache,subGroup=Entrys,groupName=";
    private final Log logger = LogFactory.getLog(this.getClass());

    private final MBeanServer mBeanServer;
    private final MBeanExporter mBeanExporter;
    private final Map<String, Cache> caches;
    private final List<ObjectName> registerdMBeans;

    private Timer autoRefreshTimer;

    public CacheEntrysMonitorInitializer(Map<String, Cache> caches, MBeanExporter exporter) {
        this(caches, exporter, 0);
    }

    /**
     *
     * @param caches
     * @param exporter
     * @param refreshTimeOut if this value is greater than 0 then would be started an automatic refresh task
     */
    public CacheEntrysMonitorInitializer(Map<String, Cache> caches, MBeanExporter exporter, long refreshTimeOut) {
        this.caches = caches;
        this.mBeanExporter = exporter;
        this.mBeanServer = exporter.getServer();
        this.registerdMBeans = new ArrayList<ObjectName>();

        if (refreshTimeOut > 0) {
            this.enableAutoRefresh(refreshTimeOut);
        }
    }

    /**
     * Forces refresh action on CachesEntrys monitor.
     *
     * @return <code>true</code> if every useless MBean was successfully unregistered, <code>false</code> after first failed MBean to unregister.
     */
    @ManagedOperation(description = "Forces refresh action on CachesEntrys monitor.")
    public final boolean performRefresh() {
        List<ObjectName> toRemove = this.processCaches(this.caches);
        for (ObjectName objectName : toRemove) {
            if (this.mBeanServer.isRegistered(objectName)) {
                try {
                    this.mBeanServer.unregisterMBean(objectName);
                } catch (MBeanRegistrationException e) {
                    this.logger.fatal(e.getMessage(), e);
                    return false;
                } catch (InstanceNotFoundException e) {
                    this.logger.fatal(e.getMessage(), e);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Enables auto refresh action. If there is already refresh action started it would be  stopped and started new refresh action with defined refreshTimeOut parameter.
     *
     * @param refreshTimeOut refresh operation time out in milliseconds
     */
    @ManagedOperation(description = "Enables auto refresh action. If there is already refresh action started it would be  stopped and started new refresh action with defined refreshTimeOut parameter.")
    @ManagedOperationParameters( { @ManagedOperationParameter(name = "refreshTimeOut", description = "Refresh operation time out in milliseconds.") })
    public final void enableAutoRefresh(long refreshTimeOut) {
        // stop auto refresh task if it is already running.
        if (this.autoRefreshTimer != null) {
            this.disableAutoRefresh();
        }

        this.autoRefreshTimer = new Timer(true);
        this.autoRefreshTimer.scheduleAtFixedRate(new RefreshTask(), 0, refreshTimeOut);
    }

    /**
     * Stops refresh action if there is any refresh action already running.
     */
    @ManagedOperation(description = "Stops refresh action if there is any refresh action already running.")
    public final void disableAutoRefresh() {
        if (this.autoRefreshTimer != null) {
            this.autoRefreshTimer.cancel();
            this.autoRefreshTimer.purge();
            this.autoRefreshTimer = null;
        }
    }

    /**
     * Register unregistered MBeans and returns list of removed cache's that should be unregistered from MBean server.
     *
     * @param caches
     * @return list of {@link ObjectName}s that should be unregister form MBean server
     */
    private List<ObjectName> processCaches(Map<String, Cache> caches) {
        List<ObjectName> objectsToBeRemoved = new ArrayList<ObjectName>(this.registerdMBeans);
        this.registerdMBeans.clear();
        for (Cache cache : caches.values()) {
            for (CacheKey cacheKey : cache.keySet()) {
                if (cacheKey.hasJmxGroupName()) {
                    ObjectName objectName;
                    String cacheType = cacheKey.getClass().getSimpleName();
                    String cacheName = cacheKey.toString().replaceAll("[=:,]", "_");
                    String fullObjectName = namePrefix + cacheKey.getJmxGroupName() + ",cacheType=" + cacheType + ",name=" + cacheName;
                    try {
                        objectName = new ObjectName(fullObjectName);
                    } catch (MalformedObjectNameException e) {
                        this.logger.error("Invalid name of manager resource: " + fullObjectName, e);
                        continue;
                    } catch (NullPointerException e) {
                        this.logger.error("Should never happened. Value of name parameter always is different than null.", e);
                        continue;
                    }

                    if (!this.mBeanServer.isRegistered(objectName)) { // register MBean only when it isn't already registered
                        this.mBeanExporter.registerManagedResource(new CacheEntryMonitor(cacheKey, cache.get(cacheKey)), objectName);
                    }

                    this.registerdMBeans.add(objectName);
                    objectsToBeRemoved.remove(objectName);
                }
            }
        }
        return objectsToBeRemoved;
    }

    private class RefreshTask extends TimerTask {
        @Override
        public void run() {
            CacheEntrysMonitorInitializer.this.performRefresh();
        }
    }
}
