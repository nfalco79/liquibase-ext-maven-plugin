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

import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.Assert;
import org.junit.Test;

public class NumericPrecisionRuleTest {

    @Test
    public void verifyNotValidForNoPrecision() throws Exception {
        IRule rule = new NumericPrecisionRule(31);

        String stringToValidate = "NUMERIC";
        Assert.assertThat(rule.isValid(stringToValidate), equalTo(false));
    }

    @Test
    public void verifyNotValidForGreaterPrecision() throws Exception {
        IRule rule = new NumericPrecisionRule(31);

        String stringToValidate = "NUMERIC(38)";
        Assert.assertThat(rule.isValid(stringToValidate), equalTo(false));
    }

    @Test
    public void verifyValidPrecision() throws Exception {
        IRule rule = new NumericPrecisionRule(31);

        String stringToValidate = "NUMERIC(19,5)";
        Assert.assertThat(rule.isValid(stringToValidate), equalTo(true));
    }

    @Test
    public void verifyMissingPrecisionMessage() throws Exception {
        IRule rule = new NumericPrecisionRule(31);

        Assert.assertThat(rule.getMessage("type", "NUMERIC"), equalTo("Column type NUMERIC has no defined precision"));
    }

    @Test
    public void verifyExceededPrecisionMessage() throws Exception {
        IRule rule = new NumericPrecisionRule(31);

        Assert.assertThat(rule.getMessage("type", "NUMERIC(35)"), equalTo("NUMERIC(35) has greater precision than 31 allowed"));
    }
}
