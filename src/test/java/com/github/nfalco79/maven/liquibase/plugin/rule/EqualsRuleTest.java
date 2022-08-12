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

public class EqualsRuleTest {

    @Test
    public void expected_value_does_not_matches() throws Exception {
        IRule rule = new EqualsRule("foo");

        Assert.assertThat(rule.isValid("xyz"), equalTo(false));
    }

    @Test
    public void expected_value_matches() throws Exception {
        IRule rule = new EqualsRule("foo");

        Assert.assertThat(rule.isValid("foo"), equalTo(true));
    }

    @Test
    public void verify_message() throws Exception {
        IRule rule = new EqualsRule("foo");

        Assert.assertThat(rule.getMessage("field", "value"), equalTo("value does not matches the expected value foo"));
    }

}