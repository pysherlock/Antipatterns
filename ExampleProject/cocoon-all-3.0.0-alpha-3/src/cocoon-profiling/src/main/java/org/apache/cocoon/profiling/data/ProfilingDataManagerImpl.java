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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * This class connects ProfilingData objects according to request id
 */
public class ProfilingDataManagerImpl implements ProfilingDataManager {

    private HashMap<String, LinkedList<ProfilingData>> dataMap = new HashMap<String, LinkedList<ProfilingData>>();

    private ProfilingDataHolder profilingDataHolder;

    /**
     * {@inheritDoc}
     */
    public void add(ProfilingData data) {
        String id = data.getProfilingId();
        LinkedList<ProfilingData> temp = this.dataMap.get(id);

        if (temp != null) {
            temp.add(data);
            if (data.isRoot()) {
                this.finish(id);
            }
        } else {
            // new request
            temp = new LinkedList<ProfilingData>();
            this.dataMap.put(id, temp);
            temp.add(data);
        }
    }

    /**
     * build a tree structure
     *
     * @param list
     *            a list sorted according to invocation timestamp
     * @return the root tree node
     */
    private ProfilingData buildTree(List<ProfilingData> list) {
        ProfilingData root = list.remove(0);
        int maxLevel = 0;

        while (!list.isEmpty()) {
            maxLevel = Math.max(maxLevel, list.get(0).getInvocationDepth());
            this.addNode(root, list);
        }

        for (int i = maxLevel; i > 1; i--) {
            this.correctChildAssignments(root, i);
        }
        return root;
    }

    /**
     * links children at the specified level to the correct parents so the time ordering is correct
     */
    private void correctChildAssignments(ProfilingData root, int level) {
        while (root.getInvocationDepth() < level - 2) {
            root = root.getChild(0);
        }
        List<ProfilingData> children = new ArrayList<ProfilingData>(root.getChild(0).getChildren());
        root.getChild(0).removeAllChildren();
        int size = root.getChildCount();
        ProfilingData temp = null;

        for (int i = 1; i < size; i++) {
            while (!children.isEmpty()
                    && root.getChild(i).getInvocationStartTime() > children.get(0).getInvocationStartTime()) {
                temp = children.remove(0);
                root.getChild(i - 1).addChild(temp);
            }
        }
        while (!children.isEmpty()) {
            temp = children.remove(0);
            root.getChild(size - 1).addChild(temp);
        }
    }

    /**
     * Insert data into tree structure
     *
     * @param root
     *            the root node of the tree structure
     */
    private void addNode(ProfilingData root, List<ProfilingData> list) {
        ProfilingData data = list.remove(0);

        // iterate to nearest tree node
        while (root.getInvocationDepth() < data.getInvocationDepth() && root.hasChildren()) {
            root = root.getChild(0);
        }
        // add before current node
        if (root.getInvocationDepth() > data.getInvocationDepth()) {
            data.addChildren(root.getParent().getChildren());
            root.getParent().removeAllChildren();
            root.getParent().addChild(data);
            return;
        }
        // add on the same level as current node
        if (root.getInvocationDepth() == data.getInvocationDepth()) {
            root.getParent().addChild(data);
            return;
        }
        // add as child
        if (!root.hasChildren()) {
            root.addChild(data);
            return;
        }
        throw new RuntimeException("Cannot add ProfilingData object to tree structure");
    }

    /**
     * To be called when a request was finished. build a tree and store it in
     * {@link ProfilingDataHolder}
     *
     * @param id the request id
     */
    private void finish(String id) {
        try {
            LinkedList<ProfilingData> list = this.dataMap.get(id);

            // remove useless method calls
            if (list.size() <= 3) {
                return;
            }

            Collections.sort(list, new ProfilingDataComparator());
            ProfilingData data = this.buildTree(list);

            this.profilingDataHolder.store(id, data);
        } finally {
            this.dataMap.remove(id);
        }
    }

    public void setProfilingDataHolder(ProfilingDataHolder profilingDataHolder) {
        this.profilingDataHolder = profilingDataHolder;
    }
}
