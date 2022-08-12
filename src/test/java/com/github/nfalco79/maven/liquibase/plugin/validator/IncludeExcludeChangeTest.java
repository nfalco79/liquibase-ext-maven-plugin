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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import liquibase.change.Change;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.RenameTableChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;

public class IncludeExcludeChangeTest {

    @Test
    public void test_include_exclude() throws Exception {
        Set<String> includeChanges = new HashSet<String>();
        includeChanges.add("createTable");
        Set<String> excludeChanges = new HashSet<String>();
        excludeChanges.add("renameTable");

        Change change = new RenameTableChange();
        ChangeSet changeSet = mock(ChangeSet.class);
        when(changeSet.getChangeLog()).thenReturn(mock(DatabaseChangeLog.class));
        change.setChangeSet(changeSet);

        IncludeExcludeChange validator = new IncludeExcludeChange(includeChanges, excludeChanges);
        Assert.assertThat(validator.validate(change).size(), Matchers.greaterThan(0));
        Assert.assertThat(validator.validate(new CreateTableChange()).size(), equalTo(0));
    }
}
