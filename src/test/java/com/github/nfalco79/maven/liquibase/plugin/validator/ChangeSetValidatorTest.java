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

import com.github.nfalco79.maven.liquibase.plugin.ValidateMojo;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;

public class ChangeSetValidatorTest {

    @Test
    public void verify_changeset_id_matches_default_pattern() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "changeset_id_pattern.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        ChangeSetIssueIdValidator validator = new ChangeSetIssueIdValidator(ValidateMojo.ISSUE_PATTERN);

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            violations.addAll(validator.validate(changeSet));
        }
        Assert.assertEquals("unexpected violations", 1, violations.size());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(endsWith("The changeset id does not matches the issue pattern")));
    }

    @Test
    public void verify_raise_issue_on_changeset_id_without_progressive_number() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "changeset_id_progressive_pattern.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        ChangeSetIssueIdValidator validator = new ChangeSetIssueIdValidator(ValidateMojo.ISSUE_PATTERN);

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            violations.addAll(validator.validate(changeSet));
        }

        Assert.assertEquals("unexpected violations", 2, violations.size());
        List<String> messages = ValidatorUtil.extractMessage(violations);

        Assert.assertThat(messages, hasItems(endsWith("The changeset id does not end with progressive number after the issue id")));
        Assert.assertThat(messages, hasItems(endsWith("The changeset id does not end with progressive number after the issue id")));
    }

}
