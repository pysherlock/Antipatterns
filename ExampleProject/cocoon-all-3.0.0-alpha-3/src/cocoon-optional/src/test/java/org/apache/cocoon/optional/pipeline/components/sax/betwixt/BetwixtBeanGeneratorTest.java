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
package org.apache.cocoon.optional.pipeline.components.sax.betwixt;

import static junit.framework.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.pipeline.component.PipelineComponent;
import org.apache.cocoon.sax.SAXPipelineComponent;
import org.apache.cocoon.sax.component.XMLSerializer;
import org.apache.commons.betwixt.BindingConfiguration;
import org.apache.commons.betwixt.IntrospectionConfiguration;
import org.apache.commons.betwixt.XMLIntrospector;
import org.custommonkey.xmlunit.Diff;
import org.junit.Test;

public class BetwixtBeanGeneratorTest {

    @Test
    public void testPipelineWithBeanGenerator() throws Exception {
        Animal animal = new Animal(5, "Dook", "albino", "Mustela putoris furo", "Lector");

        Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
        pipeline.addComponent(new BetwixtBeanGenerator(animal));
        pipeline.addComponent(new XMLSerializer());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipeline.setup(baos);
        pipeline.execute();

        Diff diff = new Diff(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Animal id=\"1\"><age>5</age><call>Dook</call><colour>albino</colour><latinName>Mustela putoris furo</latinName><name>Lector</name></Animal>",
                new String(baos.toByteArray()));
        assertTrue("Bean generation didn't work as expected " + diff, diff.identical());
    }

    @Test
    public void testPipelineWithConfiguredBeanGenerator() throws Exception {
        Animal animal = new Animal(5, "Dook", "albino", "Mustela putoris furo", "Lector");

        BindingConfiguration bindingConfiguration = new BindingConfiguration();
        bindingConfiguration.setMapIDs(false);

        XMLIntrospector xmlIntrospector = new XMLIntrospector();
        IntrospectionConfiguration configuration = xmlIntrospector.getConfiguration();
        configuration.setAttributesForPrimitives(true);

        Pipeline<PipelineComponent> pipeline = new NonCachingPipeline<PipelineComponent>();
        pipeline.addComponent(new BetwixtBeanGenerator(animal, bindingConfiguration, xmlIntrospector));
        pipeline.addComponent(new XMLSerializer());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipeline.setup(baos);
        pipeline.execute();

        Diff diff = new Diff(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Animal age=\"5\" call=\"Dook\" colour=\"albino\" latinName=\"Mustela putoris furo\" name=\"Lector\"/>",
                new String(baos.toByteArray()));
        assertTrue("Bean generation didn't work as expected " + diff, diff.identical());
    }
}
