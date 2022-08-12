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

import com.github.nfalco79.maven.liquibase.plugin.rule.DataTypeRule;
import com.github.nfalco79.maven.liquibase.plugin.rule.RuleEngine;

import liquibase.change.Change;

/**
 * Verify that datatype is SQL-99 standard.
 */
@Validator(name = "sql99Datatype")
public class ModifyDataTypeValidator implements IChangeValidator {

    @Override
    public Collection<ValidationError> validate(Change c) {
        RuleEngine ruleEngine = new RuleEngine();
        ruleEngine.add(new DataTypeRule(), new ValidationContext(c, "newDataType"));

        return ruleEngine.execute();
    }

}