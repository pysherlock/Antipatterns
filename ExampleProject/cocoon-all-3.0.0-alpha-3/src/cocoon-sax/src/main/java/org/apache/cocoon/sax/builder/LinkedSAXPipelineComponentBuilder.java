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
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.apache.cocoon.pipeline.builder.LinkedPipelineConfigurationBuilder;
import org.apache.cocoon.sax.SAXPipelineComponent;

/**
 * 
 *
 * @version $Id: LinkedSAXPipelineComponentBuilder.java 1044297 2010-12-10 11:24:36Z simonetripodi $
 */
public interface LinkedSAXPipelineComponentBuilder {

    LinkedSAXPipelineComponentBuilder addCleaningTransformer();

    LinkedSAXPipelineComponentBuilder addIncludeTransformer();

    LinkedSAXPipelineComponentBuilder addLogAsXMLTransformer();

    LinkedSAXPipelineComponentBuilder addLogAsXMLTransformer(File logFile);

    LinkedSAXPipelineComponentBuilder addLogTransformer(File logFile) throws IOException;

    LinkedSAXPipelineComponentBuilder addLogTransformer(File logFile, boolean append) throws IOException;

    LinkedSAXPipelineComponentBuilder addLogTransformer(File logFile, boolean append, String datePattern) throws IOException;

    LinkedSAXPipelineComponentBuilder addLogTransformer(File logFile, boolean append, SimpleDateFormat dateFormat) throws IOException;

    LinkedSAXPipelineComponentBuilder addSchemaProcessorTransformer(URL source);

    LinkedSAXPipelineComponentBuilder addXIncludeTransformer();

    LinkedSAXPipelineComponentBuilder addXIncludeTransformer(URL baseUrl);

    LinkedSAXPipelineComponentBuilder addXSLTTransformer(URL source);

    LinkedSAXPipelineComponentBuilder addXSLTTransformer(URL source, Map<String, Object> attributes);

    <SPC extends SAXPipelineComponent> LinkedSAXPipelineComponentBuilder addComponent(SPC pipelineComponent);

    LinkedPipelineConfigurationBuilder<SAXPipelineComponent> addSerializer();

}
