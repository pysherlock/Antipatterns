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
package org.apache.cocoon.sitemap.expression;

import org.apache.cocoon.sitemap.objectmodel.ObjectModel;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;

public class JexlLanguageInterpreter implements LanguageInterpreter {

    @SuppressWarnings("unchecked")
    public String resolve(String expression, ObjectModel objectModel) {
        Object result = null;
        try {
            Expression e = ExpressionFactory.createExpression(expression);
            JexlContext jc = JexlHelper.createContext();
            jc.getVars().putAll(objectModel.getParameters());

            result = e.evaluate(jc);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        return result != null ? result.toString() : "";
    }
}
