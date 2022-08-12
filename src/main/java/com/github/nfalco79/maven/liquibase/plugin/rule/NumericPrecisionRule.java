/*
 * Copyright 2022 Laura Cameran
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.nfalco79.maven.liquibase.plugin.rule;

import com.github.nfalco79.maven.liquibase.plugin.util.StringUtil;

/**
 * Rule to check if the numeric type accuracy.
 *
 * @author Laura Cameran
 */
public class NumericPrecisionRule implements IRule {

    private int maxPrecision;

    /**
     * Constructs an instance of this rule.
     *
     * @param max the max precision allowed for NUMERIC type
     */
    public NumericPrecisionRule(int max) {
        this.maxPrecision = max;
    }

    @Override
    public boolean isValid(final String fieldValue) {
        if ("numeric".equalsIgnoreCase(StringUtil.removeParam(fieldValue))) {
            String numericValue = StringUtil.getParam(fieldValue);
            if (numericValue.isEmpty() || StringUtil.getPrecision(numericValue) > maxPrecision) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getMessage(String field, String fieldValue) {
        return StringUtil.getParam(fieldValue).isEmpty()
                ? "Column " + field + " NUMERIC has no defined precision"
                : fieldValue + " has greater precision than " + maxPrecision + " allowed";
    }

}
