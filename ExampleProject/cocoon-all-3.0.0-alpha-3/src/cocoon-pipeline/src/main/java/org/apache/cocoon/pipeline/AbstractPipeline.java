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
package org.apache.cocoon.pipeline;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import org.apache.cocoon.pipeline.component.Consumer;
import org.apache.cocoon.pipeline.component.Finisher;
import org.apache.cocoon.pipeline.component.PipelineComponent;
import org.apache.cocoon.pipeline.component.Producer;
import org.apache.cocoon.pipeline.component.Starter;
import org.apache.cocoon.pipeline.util.StringRepresentation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Basic pipeline implementation that collects the {@link PipelineComponent}s
 * and connects them with each other.
 */
public abstract class AbstractPipeline<T extends PipelineComponent> implements Pipeline<T> {

    private final LinkedList<T> components = new LinkedList<T>();

    private final Log logger = LogFactory.getLog(this.getClass());

    private boolean setupDone;

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.Pipeline#addComponent(org.apache.cocoon.pipeline.component.PipelineComponent)
     */
    public void addComponent(T pipelineComponent) {
        if (this.setupDone) {
            throw new SetupException(new IllegalStateException(
                    "Pass all pipeline components to the pipeline before calling this method."));
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Adding component " + pipelineComponent + " to pipeline [" + this + "].");
        }

        this.components.add(pipelineComponent);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.Pipeline#execute()
     */
    public void execute() throws Exception {
        if (!this.setupDone) {
            throw new ProcessingException(new IllegalStateException(
                    "The pipeline wasn't setup correctly. Call #setup() first."));
        }

        this.invokeStarter();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.Pipeline#getContentType()
     */
    public String getContentType() {
        return this.getFinisher().getContentType();
    }

    public long getLastModified() {
        return -1;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.Pipeline#setConfiguration(java.util.Map)
     */
    public void setConfiguration(Map<String, ? extends Object> parameters) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.Pipeline#setup(java.io.OutputStream,
     *      java.util.Map)
     */
    public void setup(OutputStream outputStream) {
        this.setup(outputStream, null);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.Pipeline#setup(java.io.OutputStream,
     *      java.util.Map)
     */
    public void setup(OutputStream outputStream, Map<String, Object> parameters) {
        if (outputStream == null) {
            throw new SetupException("An output stream must be passed.");
        }

        this.setupComponents(outputStream, parameters);
        this.setupDone = true;
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "components=" + this.getComponents());
    }

    protected LinkedList<T> getComponents() {
        return this.components;
    }

    protected Finisher getFinisher() {
        return (Finisher) this.components.getLast();
    }

    protected void invokeStarter() {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Invoking first component of " + this);
        }

        try {
            Starter starter = (Starter) this.components.getFirst();
            starter.execute();
        } finally {
            for (PipelineComponent pipelineComponent : this.getComponents()) {
                pipelineComponent.finish();
            }
        }
    }

    protected void setupComponents(OutputStream outputStream, Map<String, Object> parameters) {
        PipelineComponent first = this.components.getFirst();

        // first component must be a Starter
        if (!(first instanceof Starter)) {
            String msg = "Cannot execute pipeline, first pipeline component is no starter";
            this.logger.error(msg);
            throw new SetupException(new IllegalStateException(msg));
        }

        // last component must be a Finisher
        PipelineComponent last = this.components.getLast();
        if (!(last instanceof Finisher)) {
            String msg = "Cannot execute pipeline, last pipeline component is no finisher";
            this.logger.error(msg);
            throw new SetupException(new IllegalStateException(msg));
        }

        // now try to link the components, always two components at a time
        // start at the first component
        PipelineComponent currentComponent = first;
        first.setup(parameters);

        // next component to link is the second in the list
        for (ListIterator<T> i = this.components.listIterator(1); i.hasNext();) {
            // link the current with the next component
            PipelineComponent nextComponent = i.next();
            this.linkComponents(currentComponent, nextComponent);

            // now advance to the next component
            currentComponent = nextComponent;
            currentComponent.setup(parameters);
        }

        // configure the finisher
        ((Finisher) last).setOutputStream(outputStream);
    }

    private void linkComponents(PipelineComponent firstComponent, PipelineComponent secondComponent) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Going to link the component " + firstComponent + " with " + secondComponent + ".");
        }

        // first component must be a Producer
        if (!(firstComponent instanceof Producer)) {
            String msg = "Cannot link components: First component (" + firstComponent + ") is no producer.";
            throw new SetupException(new IllegalStateException(msg));
        }

        // second component must be a Consumer
        if (!(secondComponent instanceof Consumer)) {
            String msg = "Cannot link components: Second component (" + secondComponent + ") is no consumer.";
            throw new SetupException(new IllegalStateException(msg));
        }

        // let the Producer accept the Consumer (the Producer might reject it)
        ((Producer) firstComponent).setConsumer((Consumer) secondComponent);
    }
}
