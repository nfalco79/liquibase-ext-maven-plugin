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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.ext.nfalco79.CopyColumnChange;

public class CopyColumnValidatorTest {
    private ValidatorFactory validatorFactory;

    @Before
    public void setup() {
        validatorFactory = new ValidatorFactory();
        validatorFactory.setIgnoreRules(Collections.emptySet());
    }

    @Test
    public void copy_real_to_clob_is_not_supported() {
        ChangeSet changeSet = ValidatorUtil.getChangeSet();

        changeSet.addChange(newCopyColumnChange("real", "clob"));

        Collection<ValidationError> violations = new ArrayList<>();
        for (Change change : changeSet.getChanges()) {
            IChangeValidator validator = validatorFactory.newValidator(change);
            violations.addAll(validator.validate(change));
        }

        Assert.assertFalse("We expect violations for copy column", violations.isEmpty());
        Iterator<ValidationError> issueIterator = violations.iterator();
        Assert.assertThat(issueIterator.next().getMessage(), CoreMatchers.startsWith("REAL isn't one of types permitted by Oracle TO_CLOB function"));
    }

    @Test
    public void copy_nchar_to_clob_is_supported() {
        ChangeSet changeSet = ValidatorUtil.getChangeSet();

        changeSet.addChange(newCopyColumnChange("nchar", "clob"));

        Collection<ValidationError> violations = new ArrayList<>();
        for (Change change : changeSet.getChanges()) {
            IChangeValidator validator = validatorFactory.newValidator(change);
            violations.addAll(validator.validate(change));
        }

        Assert.assertTrue("We expect violations for copy column", violations.isEmpty());
    }

    @Test
    public void copy_varchar_to_bigint_is_supported() {
        ChangeSet changeSet = ValidatorUtil.getChangeSet();

        changeSet.addChange(newCopyColumnChange("VARCHAR(255)", "BIGINT"));

        Collection<ValidationError> violations = new ArrayList<>();
        for (Change change : changeSet.getChanges()) {
            IChangeValidator validator = validatorFactory.newValidator(change);
            violations.addAll(validator.validate(change));
        }

        Assert.assertTrue("We expect violations for copy column", violations.isEmpty());
    }

    @Test
    public void copy_clob_to_clob_is_supported() {
        ChangeSet changeSet = ValidatorUtil.getChangeSet();

        changeSet.addChange(newCopyColumnChange("clob", "clob"));

        Collection<ValidationError> violations = new ArrayList<>();
        for (Change change : changeSet.getChanges()) {
            IChangeValidator validator = validatorFactory.newValidator(change);
            violations.addAll(validator.validate(change));
        }

        Assert.assertTrue("We expect violations for copy column", violations.isEmpty());
    }

    @Test
    public void copy_clob_to_blob_is_not_supported() {
        ChangeSet changeSet = ValidatorUtil.getChangeSet();

        changeSet.addChange(newCopyColumnChange("clob", "blob(5000)"));

        Collection<ValidationError> violations = new ArrayList<>();
        for (Change change : changeSet.getChanges()) {
            IChangeValidator validator = validatorFactory.newValidator(change);
            violations.addAll(validator.validate(change));
        }

        Assert.assertFalse("We expect violations for copy column", violations.isEmpty());
        Iterator<ValidationError> issueIterator = violations.iterator();
        Assert.assertThat(issueIterator.next().getMessage(), CoreMatchers.startsWith("Copy column cannot convert CLOB into BLOB"));
    }

    @Test
    public void copy_blob_to_nclob_is_not_supported() {
        ChangeSet changeSet = ValidatorUtil.getChangeSet();

        changeSet.addChange(newCopyColumnChange("blob(5000)", "nclob(1000000)"));

        Collection<ValidationError> violations = new ArrayList<>();
        for (Change change : changeSet.getChanges()) {
            IChangeValidator validator = validatorFactory.newValidator(change);
            violations.addAll(validator.validate(change));
        }

        Assert.assertFalse("We expect violations for copy column", violations.isEmpty());
        Iterator<ValidationError> issueIterator = violations.iterator();
        Assert.assertThat(issueIterator.next().getMessage(), CoreMatchers.startsWith("Copy column cannot convert BLOB into NCLOB"));
    }

    private CopyColumnChange newCopyColumnChange(String fromType, String toType) {
        CopyColumnChange change = new CopyColumnChange();
        change.setFromType(fromType);
        change.setToType(toType);
        return change;
    }

}
