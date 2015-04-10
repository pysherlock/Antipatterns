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

import javax.xml.stream.events.XMLEvent;

import org.apache.cocoon.pipeline.component.PipelineComponent;

/**
 * This is a component used in a StAX pipeline.<br>
 * A pipeline component consuming and/or producing {@link XMLEvent}s should implement this marker
 * interface.
 */
public interface StAXPipelineComponent extends PipelineComponent {

}
