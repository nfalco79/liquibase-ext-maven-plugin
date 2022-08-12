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
import java.util.Iterator;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;

public class ColumnNotNullConstraintValidatorTest {

    @Test
    public void verifyMissingDefaultNullValueAttribute() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "column_not_null_constraint.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        ColumnNotNullConstraintValidator validator = new ColumnNotNullConstraintValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("add_not_null_invalid")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertFalse("We expect violations for add column", violations.isEmpty());
        Iterator<ValidationError> issueIterator = violations.iterator();
        Assert.assertThat(issueIterator.next().getMessage(), CoreMatchers.startsWith("You can not add not nullable constraint"));
    }

    @Test
    public void verifyDefaultNullValueAttributeExists() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "column_not_null_constraint.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        ColumnNotNullConstraintValidator validator = new ColumnNotNullConstraintValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("add_not_null_with_default_value")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertTrue("We expect violations for add column", violations.isEmpty());
    }

    @Test
    public void verifyMissingColumnDefaultValueAttribute() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "column_not_null_constraint.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        ColumnNotNullConstraintValidator validator = new ColumnNotNullConstraintValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("add_column_without_default_value")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }
        Assert.assertEquals("unexpected violations", 1, violations.size());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(endsWith("add not nullable columns without a default value.")));
    }

    @Test
    public void verifyNullCostraintWithDefaultValueAttribute() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "column_not_null_constraint.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        ColumnNotNullConstraintValidator validator = new ColumnNotNullConstraintValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("add_column_ok")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertTrue("We expect violations for copy column", violations.isEmpty());
    }

    @Test
    public void verifyNullCostraintWithAddColumnInChangeset() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "column_not_null_constraint.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        ColumnNotNullConstraintValidator validator = new ColumnNotNullConstraintValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("add_not_null_with_add_column")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertTrue("We expect violations for copy column", violations.isEmpty());
    }

    @Test
    public void verifyMissingDefaultValueAndWrongOrder() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "column_not_null_constraint.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        ColumnNotNullConstraintValidator validator = new ColumnNotNullConstraintValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("add_not_null_wrong_order")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertFalse("We expect violations for add column", violations.isEmpty());
        Iterator<ValidationError> issueIterator = violations.iterator();
        Assert.assertThat(issueIterator.next().getMessage(), CoreMatchers.startsWith("You can not add not nullable constraint"));
    }

    @Test
    public void verifyNullCostraintWithCorrectOrder() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "column_not_null_constraint.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        ColumnNotNullConstraintValidator validator = new ColumnNotNullConstraintValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("add_not_null_create_table")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertTrue("We expect violations for copy column", violations.isEmpty());
    }
}
