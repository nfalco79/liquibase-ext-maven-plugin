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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;

public class AddUniqueConstraintValidatorTest {

    private ValidatorFactory factory;

    @Before
    public void setup() {
        factory = new ValidatorFactory();
    }

    @Test
    public void verify_correct_add_unique_constraint() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "/unique_constraint/add_unique_constraint.xml");
        Collection<ValidationError> violations = new ArrayList<>();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            for (Change change : changeSet.getChanges()) {
                violations.addAll(factory.newValidator(change).validate(change));
            }
        }
        Assertions.assertThat(violations).isEmpty();
    }

    @Test
    public void verify_validation_error_in_add_unique_constraint() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "/unique_constraint/add_unique_constraint_fails.xml");
        Collection<ValidationError> violations = new ArrayList<>();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            for (Change change : changeSet.getChanges()) {
                violations.addAll(factory.newValidator(change).validate(change));
            }
        }

        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(containsString("Unique constraint can not include nullable columns col2 for table 't1'")));
    }

    @Test
    public void verify_no_spaces_in_add_unique_constraint_columnnames() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "/unique_constraint/add_unique_constraint_no_spaces.xml");
        Collection<ValidationError> violations = new ArrayList<>();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            for (Change change : changeSet.getChanges()) {
                violations.addAll(factory.newValidator(change).validate(change));
            }
        }

        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(containsString("Unique constraint can not include nullable columns col1 for table 't1'")));
    }
}
