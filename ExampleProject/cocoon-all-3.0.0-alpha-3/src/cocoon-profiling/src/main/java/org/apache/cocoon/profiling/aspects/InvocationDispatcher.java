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
package org.apache.cocoon.profiling.aspects;

import java.util.LinkedList;
import java.util.List;

import org.apache.cocoon.profiling.data.ProfilingData;
import org.apache.cocoon.profiling.profiler.Profiler;
import org.apache.cocoon.profiling.spring.AutomaticProfilerInstaller;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * <p>
 * The dispatcher gets invoked by a profiling aspect. Based on the class of the component that is
 * invoked a specific {@link Profiler} is used to fill a {@link ProfilingData} object.
 * </p>
 * 
 * <p>
 * New profilers can be installed with installProfiler(Profiler). More specific profilers will be
 * chosen first automatically. The recommended way to install a Profiler is to declare it as a
 * 
 * spring-managed bean. The {@link AutomaticProfilerInstaller} will install the Profiler
 * automatically on the correct InvocationDispatcher.
 * </p>
 */
public class InvocationDispatcher {

    protected final Log logger = LogFactory.getLog(this.getClass());

    private List<Profiler<?>> profilers = new LinkedList<Profiler<?>>();

    /**
     * Creates a new ProfilingData object and routes this invocation to a {@link Profiler}.
     *
     * @param pjp the joinpoint
     * @return return value of {@link ProceedingJoinPoint#proceed()}
     * @throws Throwable throwable from {@link ProceedingJoinPoint#proceed()}
     */
    public Object dispatch(ProceedingJoinPoint pjp) throws Throwable {
        Object target = pjp.getTarget();
        ProfilingData data = new ProfilingData();

        Profiler<?> profiler = this.getProfiler(target);
        String method = pjp.getSignature().getName();

        try {
            profiler.before(data, target, method, pjp.getArgs());
            Object returnValue = pjp.proceed(pjp.getArgs());
            profiler.after(data, method, returnValue);
            return returnValue;
        } catch (Exception e) {
            profiler.exception(data, method, e);
            throw e;
        }
    }

    /**
     * Installs a new profiler.
     *
     * @param profiler the profiler to install
     */
    public void installProfiler(Profiler<?> profiler) {
        Class<?> class1 = profiler.getTargetClass();

        // insertion position determined by type hierarchy
        for (int i = 0; i < this.profilers.size(); i++) {
            Class<?> class2 = this.profilers.get(i).getTargetClass();

            if (class2.isAssignableFrom(class1)) {
                if (class1.equals(class2)) {
                    throw new RuntimeException(String.format("You are trying to install a profiler for '%s' "
                            + "but for this class there is already a profiler registered: '%s'", class1.getName(),
                            this.profilers.get(i).getClass().getName()));
                }
                this.profilers.add(i, profiler);
                return;
            }
        }

        this.profilers.add(profiler);
    }

    private Profiler<?> getProfiler(Object target) {
        for (Profiler<?> profiler : this.profilers) {
            if (profiler.getTargetClass().isInstance(target)) {
                return profiler;
            }
        }
        throw new RuntimeException("No profiler found for " + target.getClass() + ".");
    }
}
