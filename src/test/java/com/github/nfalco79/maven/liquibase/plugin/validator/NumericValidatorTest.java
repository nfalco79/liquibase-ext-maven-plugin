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

public class NumericValidatorTest {

    @Test
    public void verifyCompliantPrecisionResizeDataType() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "numeric_type.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        NumericValidator validator = new NumericValidator(31, "newDataType");

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("compliant_numeric_resize")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertTrue("We expect violations", violations.isEmpty());
    }

    @Test
    public void verifyCompliantPrecisionModifyDataType() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "numeric_type.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        NumericValidator validator = new NumericValidator(31, "newDataType");

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("compliant_numeric_modify")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertTrue("We expect violations", violations.isEmpty());
    }

    @Test
    public void verifyCompliantPrecisionAddColumn() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "numeric_type.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        ColumnsValidator validator = new ColumnsValidator(1, 30, 31);

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("compliant_numeric_add")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertTrue("We expect violations", violations.isEmpty());
    }

    @Test
    public void verifyCompliantPrecisionCreateTable() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "numeric_type.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        ColumnsValidator validator = new ColumnsValidator(1, 30, 31);

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("compliant_numeric_create")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertTrue("We expect violations", violations.isEmpty());
    }

    @Test
    public void verifyMissingPrecisionResizeDataType() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "numeric_type.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        NumericValidator validator = new NumericValidator(31, "newDataType");

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("numeric_no_precision_resize")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertFalse("We expect violations", violations.isEmpty());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(endsWith("no defined precision")));
    }

    @Test
    public void verifyMissingPrecisionModifyDataType() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "numeric_type.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        NumericValidator validator = new NumericValidator(31, "newDataType");

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("numeric_no_precision_modify")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertFalse("We expect violations", violations.isEmpty());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(endsWith("no defined precision")));
    }

    @Test
    public void verifyMissingPrecisionAddColumn() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "numeric_type.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        ColumnsValidator validator = new ColumnsValidator(1, 30, 31);

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("numeric_no_precision_add")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertFalse("We expect violations", violations.isEmpty());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(endsWith("no defined precision")));
    }

    @Test
    public void verifyMissingPrecisionCreateTable() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "numeric_type.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        ColumnsValidator validator = new ColumnsValidator(1, 30, 31);

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("numeric_no_precision_create")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertFalse("We expect violations", violations.isEmpty());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(endsWith("no defined precision")));
    }

    @Test
    public void verifyExceededPrecisionResizeDataType() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "numeric_type.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        NumericValidator validator = new NumericValidator(31, "newDataType");

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("exceed_max_numeric_resize")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertFalse("We expect violations", violations.isEmpty());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(endsWith("greater precision than 31 allowed")));
    }

    @Test
    public void verifyExceededPrecisionModifyDataType() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "numeric_type.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        NumericValidator validator = new NumericValidator(31, "newDataType");

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("exceed_max_numeric_modify")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertFalse("We expect violations", violations.isEmpty());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(endsWith("greater precision than 31 allowed")));
    }

    @Test
    public void verifyExceededPrecisionAddColumn() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "numeric_type.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        ColumnsValidator validator = new ColumnsValidator(1, 30, 31);

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("exceed_max_numeric_add")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertFalse("We expect violations", violations.isEmpty());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(endsWith("greater precision than 31 allowed")));
    }

    @Test
    public void verifyExceededPrecisionCreateTable() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "numeric_type.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        ColumnsValidator validator = new ColumnsValidator(1, 30, 31);

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("exceed_max_numeric_create")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertFalse("We expect violations", violations.isEmpty());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(endsWith("greater precision than 31 allowed")));
    }
}
