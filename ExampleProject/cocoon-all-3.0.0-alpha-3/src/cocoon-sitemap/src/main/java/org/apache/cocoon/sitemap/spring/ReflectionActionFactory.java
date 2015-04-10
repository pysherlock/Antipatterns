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
package org.apache.cocoon.sitemap.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.cocoon.sitemap.action.Action;

public class ReflectionActionFactory implements ActionFactory {

    private final Map<String, Class<? extends Action>> types = new HashMap<String, Class<? extends Action>>();

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.sitemap.spring.ActionFactory#createAction(java.lang.String)
     */
    public Action createAction(String type) {
        Class<? extends Action> actionClass = this.types.get(type);

        if (actionClass == null) {
            throw new IllegalArgumentException("Action type '" + type + "' is not supported.");
        }

        try {
            Action action = actionClass.newInstance();
            return action;
        } catch (Exception e) {
            throw new IllegalArgumentException("An action of type '" + type + "' could not be created.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void setTypes(Map<String, String> types) {
        this.types.clear();

        for (Entry<String, String> entry : types.entrySet()) {
            try {
                Class<? extends Action> actionClass = (Class<? extends Action>) Class.forName(entry.getValue());
                this.types.put(entry.getKey(), actionClass);
            } catch (ClassCastException ccex) {
                throw new IllegalArgumentException("Could not register class " + entry.getValue() + " as type "
                        + entry.getKey(), ccex);
            } catch (ClassNotFoundException cnfex) {
                throw new IllegalArgumentException("Could not register class " + entry.getValue() + " as type "
                        + entry.getKey(), cnfex);
            }
        }
    }
}
