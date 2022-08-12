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
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;

public class DefaultValueColumnValidatorTest {

    @Test
    public void verifyCompliantValueAttribute() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "changeset_value_defaultvalue.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        DefaultValueColumnValidator validator = new DefaultValueColumnValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("compliant_value_attributes")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertTrue("We expect violations", violations.isEmpty());
    }

    @Test
    public void verifyWrongValueBooleanAttribute() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "changeset_value_defaultvalue.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        DefaultValueColumnValidator validator = new DefaultValueColumnValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("wrong_valueboolean")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertFalse("We expect violations", violations.isEmpty());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(containsString("the specific valueBoolean attribute")));
        Assert.assertThat(messages, hasSize(1));
    }

    @Test
    public void verifyWrongValueDateAttribute() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "changeset_value_defaultvalue.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        DefaultValueColumnValidator validator = new DefaultValueColumnValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("wrong_valuedate")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertFalse("We expect violations", violations.isEmpty());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(containsString("the specific valueDate attribute")));
        Assert.assertThat(messages, hasSize(2));
    }

    @Test
    public void verifyWrongValueNumericAttribute() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "changeset_value_defaultvalue.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        DefaultValueColumnValidator validator = new DefaultValueColumnValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("wrong_valuenumeric")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertFalse("We expect violations", violations.isEmpty());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(containsString("the specific valueNumeric attribute")));
        Assert.assertThat(messages, hasSize(1));
    }

    @Test
    public void verifyWrongDefaultValueNumericAttribute() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "changeset_value_defaultvalue.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        DefaultValueColumnValidator validator = new DefaultValueColumnValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("wrong_defaultvaluenumeric")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertFalse("We expect violations", violations.isEmpty());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(containsString("the specific defaultValueNumeric attribute")));
        Assert.assertThat(messages, hasSize(1));
    }

    @Test
    public void verifyAddColumnWrongDefaultValueNumericAttribute() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "changeset_value_defaultvalue.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        DefaultValueColumnValidator validator = new DefaultValueColumnValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("addcolumn_wrong_defaultvaluenumeric")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertFalse("We expect violations", violations.isEmpty());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(containsString("the specific defaultValueNumeric attribute")));
        Assert.assertThat(messages, hasSize(1));
    }

    @Test
    public void verifyCreateTableWrongDefaultValueDateAttribute() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "changeset_value_defaultvalue.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        DefaultValueColumnValidator validator = new DefaultValueColumnValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().equals("createtable_wrong_defaultvaluedate")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assert.assertFalse("We expect violations", violations.isEmpty());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(containsString("the specific defaultValueDate attribute")));
        Assert.assertThat(messages, hasSize(1));
    }
}
