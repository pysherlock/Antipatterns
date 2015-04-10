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
package org.apache.cocoon.pipeline.util;

import static junit.framework.Assert.*;

import org.junit.Test;

public class MurmurHashCodeBuilderTest {

    @Test
    public void addBytes() {
        byte[] bytes = new MurmurHashCodeBuilder().append("a".getBytes()).append("b".getBytes()).getBytes();
        assertEquals("ab", new String(bytes));
    }

    @Test
    public void addInteger() {
        byte[] bytes = new MurmurHashCodeBuilder().append(2).getBytes();
        assertEquals(2, byteArrayToInt(bytes));
    }

    @Test
    public void addLong() {
        byte[] bytes = new MurmurHashCodeBuilder().append(2l).getBytes();
        assertEquals(2l, byteArrayToLong(bytes));
    }

    @Test
    public void addMaxInteger() {
        byte[] bytes = new MurmurHashCodeBuilder().append(Integer.MAX_VALUE).getBytes();
        assertEquals(Integer.MAX_VALUE, byteArrayToInt(bytes));
    }

    @Test
    public void addMinInteger() {
        byte[] bytes = new MurmurHashCodeBuilder().append(Integer.MIN_VALUE).getBytes();
        assertEquals(Integer.MIN_VALUE, byteArrayToInt(bytes));
    }

    @Test
    public void addNullString() {
        byte[] bytes = new MurmurHashCodeBuilder().append("a").append("b").append((String) null).getBytes();
        assertEquals("ab", new String(bytes));
    }

    @Test
    public void addString() {
        byte[] bytes = new MurmurHashCodeBuilder().append("a").append("b").getBytes();
        assertEquals("ab", new String(bytes));
    }

    @Test
    public void nullInputToHashCode() {
        int hashCode = new MurmurHashCodeBuilder().toHashCode();
        assertEquals(1540447798, hashCode);
    }

    @Test
    public void toHashCode() {
        int hashCode = new MurmurHashCodeBuilder().append("a").append("b").toHashCode();
        assertEquals(-419373624, hashCode);
    }

    private static final int byteArrayToInt(byte[] b) {
        return (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8) + (b[3] & 0xFF);
    }

    private static Object byteArrayToLong(byte[] b) {
        return ((long) b[0] << 56) + ((long) (b[1] & 255) << 48) + ((long) (b[2] & 255) << 40)
                + ((long) (b[3] & 255) << 32) + ((long) (b[4] & 255) << 24) + ((b[5] & 255) << 16)
                + ((b[6] & 255) << 8) + ((b[7] & 255) << 0);
    }
}
