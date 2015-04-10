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

import javax.servlet.Servlet;

import org.apache.cocoon.profiling.jmx.ProfilingManagement;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

/**
 * Aspect to intercept calls to all classes implementing {@link Servlet}. Can be disabled via JMX
 * {@link ProfilingManagement}. Classes starting with "$Proxy" are ignored.
 */
@Aspect
@Order(Integer.MAX_VALUE)
public class ServletProfilingAspect {

    private InvocationDispatcher invocationDispatcher;

    private ProfilingManagement profilingManagement;

    @Around("execution(* service(..)) && target(javax.servlet.Servlet)")
    public Object handleInvocation(ProceedingJoinPoint pjp) throws Throwable {
        if (!this.profilingManagement.isEnabled()) {
            return pjp.proceed(pjp.getArgs());
        }
        if (pjp.getTarget().getClass().getName().startsWith("$Proxy")) {
            // ignore dynamic proxies
            return pjp.proceed(pjp.getArgs());
        }
        return this.invocationDispatcher.dispatch(pjp);
    }

    public void setInvocationDispatcher(InvocationDispatcher invocationDispatcher) {
        this.invocationDispatcher = invocationDispatcher;
    }

    public void setProfilingManagement(ProfilingManagement profilingManagement) {
        this.profilingManagement = profilingManagement;
    }
}
