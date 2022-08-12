/*
 * Copyright 2022 Nikolas Falco
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
package com.github.nfalco79.maven.liquibase.plugin.validator;

import java.util.Collection;

import com.github.nfalco79.maven.liquibase.plugin.rule.NumericPrecisionRule;
import com.github.nfalco79.maven.liquibase.plugin.rule.RuleEngine;
import com.github.nfalco79.maven.liquibase.plugin.util.ReflectionUtil;

import liquibase.change.Change;

/**
 * Validator to check the numeric type accuracy.
 */
@Validator(name = "numericType")
public class NumericValidator implements IChangeValidator {

    private final String[] fields;
    private final int maxPrecision;

    /**
     * Constructs an instance of this class.
     *
     * @param maxPrecision the max precision allowed for NUMERIC type
     * @param fieldName the fields
     */
    public NumericValidator(final int maxPrecision, final String... fieldName) {
        this.maxPrecision = maxPrecision;
        this.fields = fieldName; // NOSONAR
    }

    /**
     * Main method of the class to validate the NUMERIC column type.
     *
     * @param change instance of change
     * @return the collection of validation errors
     */
    @Override
    public Collection<ValidationError> validate(Change change) {

        NumericPrecisionRule numericPrecisionRule = new NumericPrecisionRule(maxPrecision);

        RuleEngine ruleEngine = new RuleEngine();

        for (String field : fields) {
            String fieldValue = (String) ReflectionUtil.getFieldValue(field, change);
            if (fieldValue != null) {
                ruleEngine.add(numericPrecisionRule, new ValidationContext(change, field, fieldValue));
            }
        }

        return ruleEngine.execute();
    }

}
