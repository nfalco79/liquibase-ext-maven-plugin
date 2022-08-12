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

public class NotPermittedValidatorTest {

    @Test
    public void test_attribute_is_not_permitted() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "notpermitted.xml");

        NotPermittedValidator validator = new NotPermittedValidator("catalogName", "schemaName", "defaultValueComputed");

        Collection<ValidationError> violations = new ArrayList<>();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            for (Change change : changeSet.getChanges()) {
                violations.addAll(validator.validate(change));
            }
        }

        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(endsWith("catalogName is not allowed because is not portable")));
        Assert.assertThat(messages, hasItems(endsWith("schemaName is not allowed because is not portable")));
        Assert.assertThat(messages, hasItems(endsWith("defaultValueComputed is not allowed because is not portable")));
    }

}