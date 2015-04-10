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
package org.apache.cocoon.sax.builder;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.apache.cocoon.sax.SAXPipelineComponent;
import org.apache.cocoon.xml.sax.SAXBuffer;
import org.w3c.dom.Node;

/**
 * 
 *
 * @version $Id: LinkedSAXPipelineStarterBuilder.java 1044286 2010-12-10 11:05:47Z simonetripodi $
 */
public interface LinkedSAXPipelineStarterBuilder {

    LinkedSAXPipelineComponentBuilder setByteArrayGenerator(byte[] bytes);

    LinkedSAXPipelineComponentBuilder setByteArrayGenerator(byte[] bytes, String encoding);

    LinkedSAXPipelineComponentBuilder setFileGenerator(File file);

    LinkedSAXPipelineComponentBuilder setInputStreamGenerator(InputStream inputStream);

    LinkedSAXPipelineComponentBuilder setNodeGenerator(Node node);

    LinkedSAXPipelineComponentBuilder setSAXBufferGenerator(SAXBuffer saxBuffer);

    LinkedSAXPipelineComponentBuilder setStringGenerator(String xmlString);

    LinkedSAXPipelineComponentBuilder setURLGenerator(URL url);

    <SPC extends SAXPipelineComponent> LinkedSAXPipelineComponentBuilder setStarter(SPC starter);

}
