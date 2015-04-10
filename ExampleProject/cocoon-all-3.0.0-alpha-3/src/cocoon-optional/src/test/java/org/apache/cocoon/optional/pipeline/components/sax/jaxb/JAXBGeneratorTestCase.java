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
package org.apache.cocoon.optional.pipeline.components.sax.jaxb;

import static junit.framework.Assert.assertTrue;
import static org.apache.cocoon.optional.pipeline.components.sax.jaxb.GenericType.toGenericType;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.sax.SAXPipelineComponent;
import org.apache.cocoon.sax.component.XMLSerializer;
import org.custommonkey.xmlunit.Diff;
import org.junit.Test;

/**
 * $Id: JAXBGeneratorTestCase.java 1133801 2011-06-09 11:34:54Z simonetripodi $
 */
public final class JAXBGeneratorTestCase {

    @Test
    public void testPipelineWithBean() throws Exception {
        Animal animal = new Animal();
        animal.setAge(5);
        animal.setCall("Dook");
        animal.setColour("albino");
        animal.setLatinName("Mustela putoris furo");
        animal.setName("Lector");

        this.internalAssert(toGenericType(animal),
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><animal><call>Dook</call><colour>albino</colour><latinName>Mustela putoris furo</latinName><name>Lector</name><age>5</age></animal>");
    }

    @Test
    public void testPipelineWithIrregularNamedBean() throws Exception {
        Alias alias = new Alias();
        alias.setName("Simone");
        alias.setAka("Simo");

        this.internalAssert(toGenericType(new Alias[]{ alias }),
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><aliases><alias><name>Simone</name><aka>Simo</aka></alias></aliases>");
    }

    @Test
    public void testPipelineWithExceptionNamedBean() throws Exception {
        Person person = new Person();
        person.setName("Simone");
        person.setSurname("Tripodi");

        this.internalAssert(toGenericType(new Person[]{ person }),
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><people><person><name>Simone</name><surname>Tripodi</surname></person></people>");
    }

    @Test
    public void testPipelineWithUncountableNamedBean() throws Exception {
        Equipment equipment = new Equipment();

        this.internalAssert(toGenericType(new Equipment[]{ equipment }),
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><equipment><equipment /></equipment>");
    }

    @Test
    public void testPipelineWithBeanArray() throws Exception {
        Animal animal = new Animal();
        animal.setAge(5);
        animal.setCall("Dook");
        animal.setColour("albino");
        animal.setLatinName("Mustela putoris furo");
        animal.setName("Lector");

        this.internalAssert(toGenericType(new Animal[]{ animal, animal }),
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><animals><animal><call>Dook</call><colour>albino</colour><latinName>Mustela putoris furo</latinName><name>Lector</name><age>5</age></animal><animal><call>Dook</call><colour>albino</colour><latinName>Mustela putoris furo</latinName><name>Lector</name><age>5</age></animal></animals>");
    }

    @Test
    public void testPipelineWithBeanList() throws Exception {
        Animal animal = new Animal();
        animal.setAge(5);
        animal.setCall("Dook");
        animal.setColour("albino");
        animal.setLatinName("Mustela putoris furo");
        animal.setName("Lector");

        List<Animal> animals = new ArrayList<Animal>();
        animals.add(animal);
        animals.add(animal);

        this.internalAssert(new GenericType<List<Animal>>(animals){},
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><animals><animal><call>Dook</call><colour>albino</colour><latinName>Mustela putoris furo</latinName><name>Lector</name><age>5</age></animal><animal><call>Dook</call><colour>albino</colour><latinName>Mustela putoris furo</latinName><name>Lector</name><age>5</age></animal></animals>");
    }

    private void internalAssert(GenericType<?> genericType, String expected) throws Exception {
        Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
        pipeline.addComponent(new JAXBGenerator(genericType));
        pipeline.addComponent(new XMLSerializer());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipeline.setup(baos);
        pipeline.execute();

        Diff diff = new Diff(expected, new String(baos.toByteArray()));
        assertTrue("Bean generation didn't work as expected " + diff, diff.identical());
    }

}
