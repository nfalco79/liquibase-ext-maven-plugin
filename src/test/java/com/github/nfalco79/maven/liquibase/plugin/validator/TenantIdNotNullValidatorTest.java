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
import java.util.List;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;

public class TenantIdNotNullValidatorTest {

    private static final String EXPECTED_ERROR_MESSAGE = "tenant_id column must be not nullable";

    @Test
    public void verifyAddColumnNotNullConstraintExists() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "tenant_id_validator.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        TenantIdNotNullValidator validator = new TenantIdNotNullValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if ("tenant_id_add_column_not_null".equals(changeSet.getId())) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assertions.assertThat(violations).isEmpty();
    }

    @Test
    public void verifyAddColumnNotNullConstraintDoesNotExist() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "tenant_id_validator.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        TenantIdNotNullValidator validator = new TenantIdNotNullValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if ("tenant_id_add_column_nullable".equals(changeSet.getId())) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assertions.assertThat(violations).isNotEmpty();
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, Matchers.hasItem(Matchers.endsWith(EXPECTED_ERROR_MESSAGE)));
    }

    @Test
    public void verifyCreateTableColumnNotNullConstraintExists() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "tenant_id_validator.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        TenantIdNotNullValidator validator = new TenantIdNotNullValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if ("tenant_id_create_table_column_not_null".equals(changeSet.getId())) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assertions.assertThat(violations).isEmpty();
    }

    @Test
    public void verifyCreateTableColumnNotNullConstraintDoesNotExist() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "tenant_id_validator.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        TenantIdNotNullValidator validator = new TenantIdNotNullValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if ("tenant_id_create_table_column_nullable".equals(changeSet.getId())) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assertions.assertThat(violations).isNotEmpty();
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, Matchers.hasItem(Matchers.endsWith(EXPECTED_ERROR_MESSAGE)));
    }

    @Test
    public void test_tenant_id_is_marked_not_null_using_change_in_the_same_changeset() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "tenant_id_validator.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        TenantIdNotNullValidator validator = new TenantIdNotNullValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            if (changeSet.getId().startsWith("ABC-123")) {
                for (Change change : changeSet.getChanges()) {
                    violations.addAll(validator.validate(change));
                }
            }
        }

        Assertions.assertThat(violations).isEmpty();
    }

}
