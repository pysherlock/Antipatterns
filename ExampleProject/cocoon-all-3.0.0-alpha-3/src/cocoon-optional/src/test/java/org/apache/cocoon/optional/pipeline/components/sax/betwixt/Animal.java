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

final class Animal {

    private String call;

    private String colour;

    private String latinName;

    private String name;

    int age;

    public Animal(int age, String call, String colour, String latinName, String name) {
        this.age = age;
        this.call = call;
        this.colour = colour;
        this.latinName = latinName;
        this.name = name;
    }

    public int getAge() {
        return this.age;
    }

    public String getCall() {
        return this.call;
    }

    public String getColour() {
        return this.colour;
    }

    public String getLatinName() {
        return this.latinName;
    }

    public String getName() {
        return this.name;
    }
}