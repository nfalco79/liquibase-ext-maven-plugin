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

import static org.hamcrest.CoreMatchers.endsWithIgnoringCase;
import static org.hamcrest.CoreMatchers.hasItems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;

public class DuplicatedIdValidatorTest {

    @Test
    public void test_no_violation_has_been_reported() throws Exception {
        DuplicatedIdValidator validator = new DuplicatedIdValidator();
        Collection<ValidationError> violations = new ArrayList<>();

        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getResource(), "duplicated_id2.xml");
        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            violations.addAll(validator.validate(changeSet));
        }

        Assertions.assertThat(violations).isEmpty();
    }

    private String getResource() {
        return this.getClass().getPackage().getName().replace('.', '/') + "/duplicated_id";
    }

    @Test
    public void test_duplicated_ChangeSet_same_file() throws Exception {
        DuplicatedIdValidator validator = new DuplicatedIdValidator();
        Collection<ValidationError> violations = new ArrayList<>();

        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getResource(), "duplicated_id1.xml");
        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            violations.addAll(validator.validate(changeSet));
        }

        Assertions.assertThat(violations).isNotEmpty();
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(endsWithIgnoringCase("the changeSet ISSUE-100-1 is already defined in the same file")));
    }

    @Test
    public void test_duplicated_ChangeSet_different_files() throws Exception {

        DuplicatedIdValidator validator = new DuplicatedIdValidator();
        Collection<ValidationError> violations = new ArrayList<>();

        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "changeset_duplicated_id.xml");
        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            violations.addAll(validator.validate(changeSet));
        }

        Assertions.assertThat(violations).isNotEmpty();
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(endsWithIgnoringCase("the changeSet ISSUE-49-1 is already defined in com/github/nfalco79/maven/liquibase/plugin/validator/duplicated_id/duplicated_id1.xml")));
    }

}
