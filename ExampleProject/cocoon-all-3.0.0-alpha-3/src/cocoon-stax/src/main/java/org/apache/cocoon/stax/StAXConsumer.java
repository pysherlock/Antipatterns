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

import org.apache.cocoon.pipeline.component.Consumer;
import org.apache.cocoon.pipeline.component.Finisher;
import org.apache.cocoon.pipeline.component.Producer;
import org.apache.cocoon.pipeline.component.Starter;

/**
 * The specific interface for StAX consumer implementation of the cocoon
 * {@link Consumer} interface. This interface extends the {@link Consumer} with
 * two methods. One, the {@link StAXConsumer#initiatePullProcessing()} to start
 * the pull processing and the {@link StAXConsumer#setParent(StAXProducer)} to
 * set the {@link Producer} for the consumer as parent directly to the
 * component.
 */
public interface StAXConsumer extends Consumer, StAXPipelineComponent {

    /**
     * Since the workflow in a pull pipeline is completely inverted compared to
     * for example a SAX pipeline, this method is required to push the
     * {@link Starter#execute()} to the {@link Finisher} which should start
     * processing.
     */
    void initiatePullProcessing();

    /**
     * Used to connect a consumer to it's producer. As to the nature of the
     * control inversion and pulling it is required to specify a parent, which
     * provides the consumer with events to consume. This function is called
     * when the pipeline sets it's consumers.
     */
    void setParent(StAXProducer parent);
}
