/* Copyright (c) 2004, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     * Neither the name of Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.apache.cocoon.stax.converter.util;

import org.xml.sax.Locator;

/**
 * A dummy locator that returns -1 and null from all methods to indicate that location info is not
 * available.
 * 
 * @author Ryan.Shoemaker@Sun.COM
 * 
 * @version from stax-utils stable version stax-utils-20070216
 * @original javanet.staxutils.DummyLocator
 * @modified false
 */
public class DummyLocator implements Locator {

    /**
     * Return the column number where the current document event ends. <p/>
     * <p>
     * <strong>Warning:</strong> The return value from the method is intended only as an
     * approximation for the sake of error reporting; it is not intended to provide sufficient
     * information to edit the character content of the original XML document.
     * </p>
     * <p/>
     * <p>
     * The return value is an approximation of the column number in the document entity or external
     * parsed entity where the markup triggering the event appears.
     * </p>
     * <p/>
     * <p>
     * If possible, the SAX driver should provide the line position of the first character after the
     * text associated with the document event.
     * </p>
     * <p/>
     * <p>
     * If possible, the SAX driver should provide the line position of the first character after the
     * text associated with the document event. The first column in each line is column 1.
     * </p>
     * 
     * @return The column number, or -1 if none is available.
     * @see #getLineNumber
     */
    public int getColumnNumber() {
        return -1;
    }

    /**
     * Return the line number where the current document event ends. <p/>
     * <p>
     * <strong>Warning:</strong> The return value from the method is intended only as an
     * approximation for the sake of error reporting; it is not intended to provide sufficient
     * information to edit the character content of the original XML document.
     * </p>
     * <p/>
     * <p>
     * The return value is an approximation of the line number in the document entity or external
     * parsed entity where the markup triggering the event appears.
     * </p>
     * <p/>
     * <p>
     * If possible, the SAX driver should provide the line position of the first character after the
     * text associated with the document event. The first line in the document is line 1.
     * </p>
     * 
     * @return The line number, or -1 if none is available.
     * @see #getColumnNumber
     */
    public int getLineNumber() {
        return -1;
    }

    /**
     * Return the public identifier for the current document event. <p/>
     * <p>
     * The return value is the public identifier of the document entity or of the external parsed
     * entity in which the markup triggering the event appears.
     * </p>
     * 
     * @return A string containing the public identifier, or null if none is available.
     * @see #getSystemId
     */
    public String getPublicId() {
        return null;
    }

    /**
     * Return the system identifier for the current document event. <p/>
     * <p>
     * The return value is the system identifier of the document entity or of the external parsed
     * entity in which the markup triggering the event appears.
     * </p>
     * <p/>
     * <p>
     * If the system identifier is a URL, the parser must resolve it fully before passing it to the
     * application.
     * </p>
     * 
     * @return A string containing the system identifier, or null if none is available.
     * @see #getPublicId
     */
    public String getSystemId() {
        return null;
    }
}
