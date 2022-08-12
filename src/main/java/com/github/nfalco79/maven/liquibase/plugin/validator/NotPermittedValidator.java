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

import com.github.nfalco79.maven.liquibase.plugin.rule.NotPermittedRule;
import com.github.nfalco79.maven.liquibase.plugin.rule.RuleEngine;

import liquibase.change.Change;

@Validator(name = "notPermitted")
public class NotPermittedValidator implements IChangeValidator {

    private final String[] fields;

    public NotPermittedValidator(String... fieldName) {
        this.fields = fieldName;
    }

    @Override
    public Collection<ValidationError> validate(Change change) {
        NotPermittedRule rule = new NotPermittedRule();

        RuleEngine ruleEngine = new RuleEngine();

        for (String field : fields) {
            ruleEngine.add(rule, new ValidationContext(change, field));
        }

        return ruleEngine.execute();
    }

}