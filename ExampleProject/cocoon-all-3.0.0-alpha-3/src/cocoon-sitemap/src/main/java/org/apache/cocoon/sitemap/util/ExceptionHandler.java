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
package org.apache.cocoon.sitemap.util;

import org.apache.cocoon.sitemap.InvocationException;

public class ExceptionHandler {

    /**
     * Wraps the given <code>throwable</code> into an {@link InvocationException}, if that is necessary.<br>
     * <br>
     * If the given <code>throwable</code> is already an instance of {@link InvocationException}, the given
     * <code>throwable</code> is returned.
     * 
     * @param throwable The throwable to wrap inside an {@link InvocationException}.
     * @return The (possibly) wrapped <code>throwable</code>.
     */
    public static InvocationException getInvocationException(Throwable throwable) {
        if (throwable instanceof InvocationException) {
            return (InvocationException) throwable;
        }

        return new InvocationException(throwable);
    }

    /**
     * Get the underlying cause of the given <code>exception</code>.<br>
     * <br>
     * Technically this unwraps the given <code>exception</code> until one of the following happens
     * <ul>
     * <li>there is no more underlying cause</li>
     * <li>an Exception other than {@link InvocationException} or one of its subclasses is encountered
     * </ul>
     * <br>
     * This method will return <code>null</code> if and only if the given <code>exception</code> is
     * <code>null</code>.
     * 
     * @param throwable The {@link Throwable} to get the cause for.
     * @return The underlying cause.
     */
    public static Throwable getCause(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        Throwable currentThrowable = throwable;

        while (true) {
            if (currentThrowable instanceof InvocationException && currentThrowable.getCause() != null) {
                currentThrowable = currentThrowable.getCause();
            } else {
                break;
            }
        }

        return currentThrowable;
    }
}
