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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used to store the gathered profiling information for one intercepted method invocation.
 */
public class ProfilingData implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<InstanceRepresentation> arguments = new ArrayList<InstanceRepresentation>();

    private long callFrameIdentity;

    private List<ProfilingData> children = new ArrayList<ProfilingData>();

    private Map<String, String> data = new HashMap<String, String>();

    private String displayName;

    private Exception exception;

    private transient String id;

    private int invocationDepth;

    private Long invocationEndTime;

    private Long invocationStartTime;

    private String method;

    private ProfilingData parent;

    private String profiler;

    private String profilingId;

    private InstanceRepresentation returnValue;

    private boolean root;

    private InstanceRepresentation target;

    private long targetIdentity;

    /**
     * Add the given {@link ProfilingData} as child and make this object its parent.
     * 
     * @param data
     */
    public void addChild(ProfilingData data) {
        this.children.add(data);
        data.setParent(this);
    }

    /**
     * Add the given list of children and make this object their parent.
     * 
     * @param children
     */
    public void addChildren(List<ProfilingData> children) {
        this.children.addAll(children);
        for (ProfilingData d : children) {
            d.setParent(this);
        }
    }

    public void addData(String key, String value) {
        this.data.put(key, value);
    }

    public List<InstanceRepresentation> getArguments() {
        return this.arguments;
    }

    public ProfilingData getChild(int index) {
        return this.children.get(index);
    }

    public int getChildCount() {
        return this.children.size();
    }

    public List<ProfilingData> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    public Map<String, String> getData() {
        return Collections.unmodifiableMap(this.data);
    }

    public String getData(String key) {
        return this.data.get(key);
    }

    /**
     * @return the display name or the simple name of the target object's class if the displayName
     *         is null, or the String representation of this object, if both the displayName and the
     *         target happen to be null.
     */
    public String getDisplayName() {
        if (this.displayName != null) {
            return this.displayName;
        }
        if (this.target != null) {
            return this.target.getRepresentedClass().getSimpleName();
        }
        return this.toString();
    }

    public Exception getException() {
        return this.exception;
    }

    public double getExecutionMillis() {
        return (this.invocationEndTime - this.invocationStartTime) / 1000000.0;
    }

    /**
     * @return the id, which is built combining the object id of the target and of the callFrame.
     */
    public String getId() {
        if (this.id == null) {
            this.id = String.valueOf(this.targetIdentity << 32 + this.callFrameIdentity);
        }
        return this.id;
    }

    public int getInvocationDepth() {
        return this.invocationDepth;
    }

    /**
     * 
     * @return the invocation end time in nanoseconds
     */
    public Long getInvocationEndTime() {
        return this.invocationEndTime;
    }

    /**
     * 
     * @return the invocation start time in nanoseconds
     */
    public Long getInvocationStartTime() {
        return this.invocationStartTime;
    }

    public String getMethod() {
        return this.method;
    }

    public ProfilingData getParent() {
        return this.parent;
    }

    public String getProfiler() {
        return this.profiler;
    }

    public String getProfilingId() {
        return this.profilingId;
    }

    public InstanceRepresentation getReturnValue() {
        return this.returnValue;
    }

    public InstanceRepresentation getTarget() {
        return this.target;
    }

    public boolean hasChildren() {
        return !this.children.isEmpty();
    }

    public boolean isDisplayNameSet() {
        return this.displayName != null;
    }

    public boolean isRoot() {
        return this.root;
    }

    /**
     * Remove all children and set their parent <code>null</code>.
     */
    public void removeAllChildren() {
        for (ProfilingData d : this.children) {
            d.setParent(null);
        }
        this.children.clear();
    }

    /**
     * Remove the given {@link ProfilingData} child and set its parent to <code>null</code> if
     * removal was successful.
     * 
     * @param data
     */
    public boolean removeChild(ProfilingData data) {
        boolean success = this.children.remove(data);
        if (success) {
            data.setParent(null);
        }
        return success;
    }

    public void setArguments(Object[] arguments) {
        this.arguments.clear();
        for (Object o : arguments) {
            this.arguments.add(new InstanceRepresentation(o));
        }
    }

    public void setCallFrameId(int callFrameId) {
        this.id = null;
        this.callFrameIdentity = callFrameId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public void setInvocationDepth(int depth) {
        this.invocationDepth = depth;
    }

    /**
     * @param invocationEndTime
     *            the invocation end time in nanoseconds
     */
    public void setInvocationEndTime(Long invocationEndTime) {
        this.invocationEndTime = invocationEndTime;
    }

    /**
     * 
     * @param invocationStartTime
     *            the invocation start time in nanoseconds
     */
    public void setInvocationStartTime(Long invocationStartTime) {
        this.invocationStartTime = invocationStartTime;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setProfiler(String profiler) {
        this.profiler = profiler;
    }

    public void setProfilingId(String profilingId) {
        this.profilingId = profilingId;
    }

    public void setReturnValue(Object obj) {
        this.returnValue = new InstanceRepresentation(obj);
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public void setTarget(Object target) {
        this.id = null;
        this.targetIdentity = System.identityHashCode(target);
        this.target = new InstanceRepresentation(target);
    }

    private void setParent(ProfilingData parent) {
        this.parent = parent;
    }
}
