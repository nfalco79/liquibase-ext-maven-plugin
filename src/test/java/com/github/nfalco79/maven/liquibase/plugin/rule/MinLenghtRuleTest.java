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

import org.codehaus.plexus.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class MinLenghtRuleTest {

    @Test
    public void longhest_string_is_valid() throws Exception {
        int min = 30;
        IRule rule = new MinLenghtRule(min);

        String stringToValidate = StringUtils.repeat("x", min + 1);
        Assert.assertThat(rule.isValid(stringToValidate), equalTo(true));
    }

    @Test
    public void shorter_string_is_not_valid() throws Exception {
        int min = 30;
        IRule rule = new MinLenghtRule(min);

        String stringToValidate = StringUtils.repeat("x", min - 1);
        Assert.assertThat(rule.isValid(stringToValidate), equalTo(false));
    }

    @Test
    public void string_matches_min_lenght() throws Exception {
        int min = 30;
        IRule rule = new MinLenghtRule(min);

        String stringToValidate = StringUtils.repeat("x", min);
        Assert.assertThat(rule.isValid(stringToValidate), equalTo(true));
    }

    @Test
    public void null_is_valid() throws Exception {
        IRule rule = new MinLenghtRule(1);

        Assert.assertThat(rule.isValid(null), equalTo(true));
    }

    @Test
    public void verify_message() throws Exception {
        IRule rule = new MinLenghtRule(10);

        Assert.assertThat(rule.getMessage("field", "value"), equalTo("value is shorter than 10"));
    }

}