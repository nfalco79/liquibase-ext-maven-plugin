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
package com.github.nfalco79.maven.liquibase.plugin.rule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;
import com.github.nfalco79.maven.liquibase.plugin.validator.ValidationContext;
import com.github.nfalco79.maven.liquibase.plugin.validator.ValidationError;

public class RuleEngine {
    private MultiValuedMap<IRule, ValidationContext> rules;

    public RuleEngine() {
        rules = new HashSetValuedHashMap<>();
    }

    public void add(IRule rule, ValidationContext... contexts) {
        for (ValidationContext context : contexts) {
            rules.put(rule, context);
        }
    }

    public Collection<ValidationError> execute() {
        List<ValidationError> issues = new LinkedList<>();

        for (Entry<IRule, ValidationContext> entry : rules.entries()) {
            IRule rule = entry.getKey();
            ValidationContext context = entry.getValue();

            String fieldValue = context.getAttributeValue();
            if (!rule.isValid(fieldValue)) {
                String fieldName = context.getAttributeName();
                issues.add(LiquibaseUtil.createIssue(context, rule.getMessage(fieldName, fieldValue)));
            }
        }

        return issues;
    }
}
