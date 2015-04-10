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
package org.apache.cocoon.stax.navigation;

import javax.xml.stream.events.XMLEvent;

/**
 * Core interface for the navigation system in all StAX transformers.
 * <p>
 * Every navigator does the specific task to classify, according to a number of StAX events, if the
 * position in the tree matches specific values and therefore decides if they are true or false and
 * returns this value.
 */
public interface Navigator {

    /**
     * Depending on the {@link XMLEvent} it is returned if the navigator fulfills the criteria the
     * {@link Navigator} is build for.
     * 
     * @param event of the XML analyzed at the moment.
     * @return if the {@link XMLEvent} full fills all criteria of the {@link Navigator}.
     */
    boolean fulfillsCriteria(XMLEvent event);

    /**
     * Every time the {@link Navigator#fulfillsCriteria(XMLEvent)} the value returned by this method
     * is stored internally in the {@link Navigator}. As long as the
     * {@link Navigator#fulfillsCriteria(XMLEvent)} method is not called again this method returns
     * the last returned value of the {@link Navigator#fulfillsCriteria(XMLEvent)} method. As long
     * the {@link Navigator#fulfillsCriteria(XMLEvent)} is not called false should be returned.
     * 
     * @return the actual value returned by the {@link Navigator#fulfillsCriteria(XMLEvent)} method.
     */
    boolean isActive();
}
