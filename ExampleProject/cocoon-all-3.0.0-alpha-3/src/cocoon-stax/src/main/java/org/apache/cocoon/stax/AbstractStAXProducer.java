/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.cocoon.stax;

import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.pipeline.component.Consumer;
import org.apache.cocoon.pipeline.component.Producer;

/**
 * StAX specific abstract class implementing the {@link Producer#setConsumer(Consumer)} method
 * checking if a {@link StAXConsumer} is used, storing it and setting itself as the parent of its
 * consumer.
 */
public abstract class AbstractStAXProducer extends AbstractStAXPipelineComponent implements StAXProducer {

    private StAXConsumer consumer;

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.component.Producer#setConsumer(org.apache.cocoon.pipeline.component.Consumer)
     */
    public void setConsumer(Consumer consumer) {
        if (consumer instanceof StAXConsumer) {
            this.consumer = (StAXConsumer) consumer;
            this.consumer.setParent(this);
        } else {
            throw new SetupException("StAXProducer requires an StAXConsumer.");
        }
    }

    /**
     * Returning the {@link StAXConsumer} inserted to the
     * {@link AbstractStAXProducer#setConsumer(Consumer)} method.
     * 
     * @return the {@link StAXConsumer} inserted to the class in the
     *         {@link AbstractStAXProducer#setConsumer(Consumer)} method.
     */
    protected StAXConsumer getConsumer() {
        return this.consumer;
    }
}
