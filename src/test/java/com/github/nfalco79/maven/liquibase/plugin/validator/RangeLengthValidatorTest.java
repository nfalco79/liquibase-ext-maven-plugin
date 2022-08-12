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

import org.codehaus.plexus.util.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import liquibase.change.core.AddColumnChange;

public class RangeLengthValidatorTest {

    @Test
    public void test_min_length_of_attribute() throws Exception {
        AddColumnChange change = ValidatorUtil.getAddColumnChange();
        change.setTableName(StringUtils.repeat("x", 6));

        RangeLengthValidator validator = new RangeLengthValidator(10, 15, "tableName");

        Collection<ValidationError> violations = validator.validate(change);
        Assert.assertEquals("unexpected violations", 1, violations.size());
        String message = violations.iterator().next().getMessage();
        Assert.assertThat(message, CoreMatchers.startsWith(change.getTableName() + " is shorter than " + validator.getMin()));
    }

    @Test
    public void test_max_length_of_attribute() throws Exception {
        AddColumnChange change = ValidatorUtil.getAddColumnChange();
        change.setTableName(StringUtils.repeat("x", 18));

        RangeLengthValidator validator = new RangeLengthValidator(10, 15, "tableName");

        Collection<ValidationError> violations = validator.validate(change);
        Assert.assertEquals("unexpected violations", 1, violations.size());
        String message = violations.iterator().next().getMessage();
        Assert.assertThat(message, CoreMatchers.startsWith(change.getTableName() + " exceeds max lenght " + validator.getMax()));
    }

    @Test
    public void verify_no_issues_when_attribute_length_is_correct() throws Exception {
        AddColumnChange change = ValidatorUtil.getAddColumnChange();
        change.setTableName(StringUtils.repeat("x", 18));

        String attributeName = "tableName";
        RangeLengthValidator validator = new RangeLengthValidator(1, 30, attributeName);

        Collection<ValidationError> violations = validator.validate(change);
        Assert.assertTrue("unexpected violations", violations.isEmpty());
    }

}