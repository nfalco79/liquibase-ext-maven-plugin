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

import com.github.nfalco79.maven.liquibase.plugin.rule.EqualsRule;
import com.github.nfalco79.maven.liquibase.plugin.rule.RequiredRule;
import com.github.nfalco79.maven.liquibase.plugin.rule.RuleEngine;

import liquibase.change.Change;
import liquibase.change.core.CreateSequenceChange;

@Validator(name = "sequence")
public class SequenceValidator implements IChangeValidator {

    @Override
    public Collection<ValidationError> validate(Change change) {
        CreateSequenceChange seq = (CreateSequenceChange) change;

        RuleEngine ruleEngine = new RuleEngine();

        ruleEngine.add(new RequiredRule(), new ValidationContext(change, "startValue"));
        ruleEngine.add(new RequiredRule(), new ValidationContext(change, "incrementBy"));
        ruleEngine.add(new EqualsRule("50"), new ValidationContext(change, "startValue", seq.getStartValue()));
        ruleEngine.add(new EqualsRule("50"), new ValidationContext(change, "incrementBy", seq.getIncrementBy()));

        return ruleEngine.execute();
    }

}
