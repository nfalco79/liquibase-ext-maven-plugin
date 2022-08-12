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
import java.util.StringTokenizer;

import org.codehaus.plexus.util.StringUtils;

import com.github.nfalco79.maven.liquibase.plugin.rule.MaxLenghtRule;
import com.github.nfalco79.maven.liquibase.plugin.rule.MinLenghtRule;
import com.github.nfalco79.maven.liquibase.plugin.rule.RuleEngine;
import com.github.nfalco79.maven.liquibase.plugin.util.ReflectionUtil;

import liquibase.change.Change;

@Validator(name = "range")
public class RangeLengthValidator implements IChangeValidator {

    private final String[] fields;
    private final int min;
    private final int max;

    public RangeLengthValidator(final int min, final int max, String... fieldName) {
        this.min = min;
        this.max = max;
        this.fields = fieldName;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @Override
    public Collection<ValidationError> validate(Change change) {
        MinLenghtRule minRule = new MinLenghtRule(min);
        MaxLenghtRule maxRule = new MaxLenghtRule(max);

        RuleEngine ruleEngine = new RuleEngine();

        for (String field : fields) {
            String fieldValue = (String) ReflectionUtil.getFieldValue(field, change);
            if (fieldValue != null) {
                StringTokenizer st = new StringTokenizer(fieldValue, ",");
                while (st.hasMoreTokens()) {
                    String value = StringUtils.trim(st.nextToken());
                    ruleEngine.add(minRule, new ValidationContext(change, field, value));
                    ruleEngine.add(maxRule, new ValidationContext(change, field, value));
                }
            }
        }

        return ruleEngine.execute();
    }

}