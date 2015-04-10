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
package org.apache.cocoon.profiling.profiler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cocoon.callstack.CallStack;
import org.apache.cocoon.profiling.ProfileMethod;
import org.apache.cocoon.profiling.ProfileMethodType;
import org.apache.cocoon.profiling.data.ProfilingData;
import org.apache.cocoon.profiling.data.ProfilingDataManager;
import org.apache.cocoon.profiling.data.ProfilingIdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for all profiling data handlers. A new instance is created for each method invocation
 * and the methods before and after/exception are called.
 *
 * It stores invocation time and end time of the invocation.
 *
 * Implementors of subclasses can provide methods annotated with {@link ProfileMethod}. Depending on
 * the {@link ProfileMethodType} the method has to have a specific signature:
 *
 * <ul>
 * <li>ProfileMethodType.BEFORE_INVOCATION: (ProfilingData data, T target, Object[] args)</li>
 * <li>ProfileMethodType.AFTER_INVOCATION: (ProfilingData data, Object returnValue)</li>
 * <li>ProfileMethodType.ON_EXCEPTION: (ProfilingData data, Exception exception)</li>
 * </ul>
 *
 * If in the {@link ProfileMethod} annotation no method name is specified, the profile method will
 * be installed as default and called for all method invocations.
 */
public abstract class Profiler<T> {

    private static ThreadLocal<Integer> depth = new ThreadLocal<Integer>() {

        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    protected final Log logger = LogFactory.getLog(this.getClass());

    private ProfilingDataManager dataManager;

    private Map<ProfileMethodType, List<Method>> defaultProfileMethods;

    private ProfilingIdGenerator idGenerator;

    private Map<ProfileMethodType, Map<String, List<Method>>> profileMethods;

    private Class<? extends T> targetClass;

    /**
     * @param targetClass class for which this profiler should be registered
     */
    public Profiler(Class<? extends T> targetClass) {
        this.targetClass = targetClass;
        this.profileMethods = new HashMap<ProfileMethodType, Map<String, List<Method>>>();
        this.defaultProfileMethods = new HashMap<ProfileMethodType, List<Method>>();

        for (ProfileMethodType type : ProfileMethodType.values()) {
            this.profileMethods.put(type, new HashMap<String, List<Method>>());
        }

        this.findProfilingMethods();
    }

    /**
     * This method is called after the invocation was successfully finished.
     *
     * @param data the {@link ProfilingData} object used to store the profiling information for this
     *            invocation.
     * @param methodName the name of the method that was intercepted.
     * @param returnValue the return value of the method that was intercepted
     */
    public final void after(ProfilingData data, String methodName, Object returnValue) {
        data.setInvocationEndTime(System.nanoTime());

        this.invokeSpecificMethods(ProfileMethodType.AFTER_INVOCATION, methodName, data, returnValue);
        this.invokeDefaultMethods(ProfileMethodType.AFTER_INVOCATION, data, returnValue);

        data.setReturnValue(returnValue);

        this.postProcessInvocation(data);
    }

    /**
     * This method is called before the invocation takes place on the given target object.
     *
     * @param data the {@link ProfilingData} object used to store the profiling information for this
     *            invocation.
     * @param target the object on which the intercepted invocation is performed.
     * @param methodName the name of the method that has been intercepted.
     * @param args the arguments of the intercepted method.
     */
    public final void before(ProfilingData data, Object target, String methodName, Object[] args) {
        String id = this.idGenerator.getCurrent();

        if (id == null) {
            data.setRoot(true);
            id = this.idGenerator.create();
        }

        data.setProfilingId(id);
        data.setInvocationDepth(depth.get());
        depth.set(depth.get() + 1);
        data.setTarget(target);
        data.setProfiler(this.getClass().getName());
        data.setMethod(methodName);
        data.setArguments(args);
        data.setCallFrameId(System.identityHashCode(CallStack.getCurrentFrame()));

        this.invokeSpecificMethods(ProfileMethodType.BEFORE_INVOCATION, methodName, data, target, args);
        this.invokeDefaultMethods(ProfileMethodType.BEFORE_INVOCATION, data, target, args);

        data.setInvocationStartTime(System.nanoTime());
    }

    /**
     * This method is called after the invocation was successfully finished.
     *
     * @param data the {@link ProfilingData} object used to store the profiling information for this
     *            invocation.
     * @param methodName the name of the method that was intercepted.
     * @param exception the exception that was thrown by the method that was intercepted
     */
    public final void exception(ProfilingData data, String methodName, Exception exception) {
        data.setInvocationEndTime(System.nanoTime());
        data.setException(exception);

        this.invokeSpecificMethods(ProfileMethodType.ON_EXCEPTION, methodName, data, exception);
        this.invokeDefaultMethods(ProfileMethodType.ON_EXCEPTION, data, exception);

        this.postProcessInvocation(data);
    }

    /**
     * @return The {@link Class}, whose instances will be profiled by this profiler.
     */
    public final Class<? extends T> getTargetClass() {
        return this.targetClass;
    }

    public void setProfilingDataManager(ProfilingDataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void setProfilingIdGenerator(ProfilingIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    private void checkProfileMethodSignature(Method method, String name, ProfileMethodType type) {
        Class<?>[] pt = method.getParameterTypes();

        switch (type) {
        case BEFORE_INVOCATION:
            if (pt.length != 3 || pt[0] != ProfilingData.class || !pt[1].isAssignableFrom(this.targetClass)
                    || pt[2] != Object[].class) {
                throw new RuntimeException("Signature of method " + method.getName()
                        + " does not conform to (ProfilingData, " + this.targetClass.getName() + ", Object[])");
            }
            break;

        case AFTER_INVOCATION:
            if (pt.length != 2 || pt[0] != ProfilingData.class || pt[1] != Object.class) {
                throw new RuntimeException("Signature of method " + method.getName()
                        + " does not conform to (ProfilingData, Object)");
            }
            break;

        case ON_EXCEPTION:
            if (pt.length != 2 || pt[0] != ProfilingData.class || pt[1] != Exception.class) {
                throw new RuntimeException("Signature of method " + method.getName()
                        + " does not conform to (ProfilingData, Exception)");
            }
            break;

        default:
            throw new AssertionError("Unknown ProfileMethodType");
        }
    }

    private void findProfilingMethods() {
        Class<?> clazz = this.getClass();
        Method[] methods = clazz.getMethods();

        for (Method method : methods) {
            this.processMethod(method);
        }
    }

    private void installDefaultMethod(Method method, ProfileMethodType type) {
        List<Method> list = this.defaultProfileMethods.get(type);

        if (list == null) {
            list = new ArrayList<Method>();
            this.defaultProfileMethods.put(type, list);
        }

        list.add(method);

        if (this.logger.isInfoEnabled()) {
            this.logger.info(String.format("Installed '%s' as default %s method", method.getName(), type));
        }
    }

    private void installProfileMethod(Method method, String name, ProfileMethodType type) {
        if (name.equals(ProfileMethod.DEFAULT_NAME)) {
            this.installDefaultMethod(method, type);
            return;
        }

        List<Method> list = this.profileMethods.get(type).get(name);
        if (list == null) {
            list = new ArrayList<Method>();
            this.profileMethods.get(type).put(name, list);
        }

        list.add(method);

        if (this.logger.isInfoEnabled()) {
            this.logger.info(String.format("Installed '%s' for '%s'/%s", method.getName(), name, type));
        }
    }

    private void invokeDefaultMethods(ProfileMethodType type, Object... args) {
        this.invokeMethods(this.defaultProfileMethods.get(type), args);
    }

    private void invokeMethods(List<Method> methods, Object... args) {
        if (methods == null) {
            return;
        }

        for (Method m : methods) {
            try {
                m.invoke(this, args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void invokeSpecificMethods(ProfileMethodType type, String methodName, Object... args) {
        this.invokeMethods(this.profileMethods.get(type).get(methodName), args);
    }

    private void postProcessInvocation(ProfilingData data) {
        depth.set(depth.get() - 1);

        this.dataManager.add(data);

        if (data.isRoot()) {
            this.idGenerator.remove();
        }
    }

    private void processMethod(Method method) {
        ProfileMethod profileMethod = method.getAnnotation(ProfileMethod.class);

        if (profileMethod == null) {
            return;
        }

        String name = profileMethod.name();
        ProfileMethodType type = profileMethod.type();

        this.checkProfileMethodSignature(method, name, type);
        this.installProfileMethod(method, name, type);
    }
}
