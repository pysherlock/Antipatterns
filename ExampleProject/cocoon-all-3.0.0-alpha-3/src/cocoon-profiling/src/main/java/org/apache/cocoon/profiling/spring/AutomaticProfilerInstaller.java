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
package org.apache.cocoon.profiling.spring;

import java.util.Map;

import javax.servlet.Servlet;

import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.pipeline.component.PipelineComponent;
import org.apache.cocoon.profiling.aspects.InvocationDispatcher;
import org.apache.cocoon.profiling.data.ProfilingDataManager;
import org.apache.cocoon.profiling.data.ProfilingIdGenerator;
import org.apache.cocoon.profiling.profiler.Profiler;
import org.apache.cocoon.sitemap.node.SitemapNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is used to install {@link Profiler} to the correct {@link InvocationDispatcher}. The
 * dependencies that all {@link Profiler} share are also managed by this class.
 * <p>
 * This class is especially useful in combination with spring, which can be configured to inject all
 * {@link Profiler} that are declared as Spring-Managed beans into this class. So additional
 * Profilers can be registered simply by defining them as Spring-bean.
 * </p>
 */
public class AutomaticProfilerInstaller {

    private final Log logger = LogFactory.getLog(this.getClass());

    private InvocationDispatcher pipelineComponentInvocationDispatcher;

    private InvocationDispatcher pipelineInvocationDispatcher;

    private Map<String, Profiler<?>> profilers;

    private ProfilingDataManager profilingDataManager;

    private ProfilingIdGenerator profilingIdGenerator;

    private InvocationDispatcher servletInvocationDispatcher;

    private InvocationDispatcher sitemapNodeInvocationDispatcher;

    /**
     * Install all profilers. {@link AutomaticProfilerInstaller#setProfilers(Map)} has to be called
     * first.
     */
    public void installProfilers() {
        for (Profiler<?> p : this.profilers.values()) {
            this.install(p);
        }
    }

    public void setPipelineComponentInvocationDispatcher(InvocationDispatcher pipelineComponentInvocationDispatcher) {
        this.pipelineComponentInvocationDispatcher = pipelineComponentInvocationDispatcher;
    }

    public void setPipelineInvocationDispatcher(InvocationDispatcher pipelineInvocationDispatcher) {
        this.pipelineInvocationDispatcher = pipelineInvocationDispatcher;
    }

    public void setProfilers(Map<String, Profiler<?>> profilers) {
        this.profilers = profilers;
    }

    public void setProfilingDataManager(ProfilingDataManager profilingDataManager) {
        this.profilingDataManager = profilingDataManager;
    }

    public void setProfilingIdGenerator(ProfilingIdGenerator profilingIdGenerator) {
        this.profilingIdGenerator = profilingIdGenerator;
    }

    public void setServletInvocationDispatcher(InvocationDispatcher servletInvocationDispatcher) {
        this.servletInvocationDispatcher = servletInvocationDispatcher;
    }

    public void setSitemapNodeInvocationDispatcher(InvocationDispatcher sitemapNodeInvocationDispatcher) {
        this.sitemapNodeInvocationDispatcher = sitemapNodeInvocationDispatcher;
    }

    private void install(Profiler<?> profiler) {
        profiler.setProfilingDataManager(this.profilingDataManager);
        profiler.setProfilingIdGenerator(this.profilingIdGenerator);

        if (Servlet.class.isAssignableFrom(profiler.getTargetClass())) {
            this.installServletProfiler(profiler);
        }

        if (SitemapNode.class.isAssignableFrom(profiler.getTargetClass())) {
            this.installSitemapNodeProfiler(profiler);
        }

        if (PipelineComponent.class.isAssignableFrom(profiler.getTargetClass())) {
            this.installPipelineComponentProfiler(profiler);
        }

        if (Pipeline.class.isAssignableFrom(profiler.getTargetClass())) {
            this.installPipelineProfiler(profiler);
        }
    }

    private void installPipelineComponentProfiler(Profiler<?> profiler) {
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Installing PipelineComponent Profiler: " + profiler);
        }
        this.pipelineComponentInvocationDispatcher.installProfiler(profiler);
    }

    private void installPipelineProfiler(Profiler<?> profiler) {
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Installing Pipeline Profiler: " + profiler);
        }
        this.pipelineInvocationDispatcher.installProfiler(profiler);
    }

    private void installServletProfiler(Profiler<?> profiler) {
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Installing Servlet Profiler: " + profiler);
        }
        this.servletInvocationDispatcher.installProfiler(profiler);
    }

    private void installSitemapNodeProfiler(Profiler<?> profiler) {
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Installing SitemapNode Profiler: " + profiler);
        }
        this.sitemapNodeInvocationDispatcher.installProfiler(profiler);
    }
}
