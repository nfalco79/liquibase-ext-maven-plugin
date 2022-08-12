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

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.hasItems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;

public class ColumnsValidatorTest {

    @Test
    public void test_attribute_too_short_in_camelcase_and_invalid_type() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "columns.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        ColumnsValidator validator = new ColumnsValidator(5, 30, 31);

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            for (Change change : changeSet.getChanges()) {
                violations.addAll(validator.validate(change));
            }
        }
        Assert.assertEquals("unexpected violations", 3, violations.size());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(endsWith("myPK must be lowercase")));
        Assert.assertThat(messages, hasItems(endsWith("myPK is shorter than 5")));
        Assert.assertThat(messages, hasItems(endsWith("FLOAT8 isn't one of SQL-99 standard types")));
    }

    @Test
    public void verify_value_in_DDL_statement_are_not_allowed() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "columns_value_not_permitted.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        ColumnsValidator validator = new ColumnsValidator(5, 30, 31);

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            for (Change change : changeSet.getChanges()) {
                violations.addAll(validator.validate(change));
            }
        }
        Assert.assertEquals("unexpected violations", 7, violations.size());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(endsWith("value is not allowed because is not portable")));
        Assert.assertThat(messages, hasItems(endsWith("valueNumeric is not allowed because is not portable")));
        Assert.assertThat(messages, hasItems(endsWith("valueDate is not allowed because is not portable")));
        Assert.assertThat(messages, hasItems(endsWith("valueBoolean is not allowed because is not portable")));
        Assert.assertThat(messages, hasItems(endsWith("valueBlobFile is not allowed because is not portable")));
        Assert.assertThat(messages, hasItems(endsWith("valueComputed is not allowed because is not portable")));
        Assert.assertThat(messages, hasItems(endsWith("defaultValueComputed is not allowed because is not portable")));
    }

    @Test
    public void verify_that_based_on_column_type_the_correct_default_value_attribute_is_used() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "use_correct_default_value_based_on_column_type.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        ColumnsValidator validator = new ColumnsValidator(0, 100, 31);

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            for (Change change : changeSet.getChanges()) {
                violations.addAll(validator.validate(change));
            }
        }
        Assert.assertEquals("unexpected violations", 3, violations.size());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(endsWith("Replace it with defaultBooleanValue")));
        Assert.assertThat(messages, hasItems(endsWith("Replace it with defaultNumericValue")));
        Assert.assertThat(messages, hasItems(endsWith("Replace it with defaultDateValue")));
    }
}