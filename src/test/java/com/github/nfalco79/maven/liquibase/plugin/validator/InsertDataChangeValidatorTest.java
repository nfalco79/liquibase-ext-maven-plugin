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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import liquibase.change.core.InsertDataChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;

public class InsertDataChangeValidatorTest {

    @Test
    public void the_same_table_cannot_be_used_in_different_projects() throws Exception {
        InsertDataChange change = mock(InsertDataChange.class);
        ChangeSet changeSet = mock(ChangeSet.class);
        DatabaseChangeLog changeLog = mock(DatabaseChangeLog.class);
        when(changeLog.getLogicalFilePath()).thenReturn("x", "x", "y");
        when(changeSet.getChangeLog()).thenReturn(changeLog);
        when(change.getChangeSet()).thenReturn(changeSet);
        when(change.getTableName()).thenReturn("tableA", "tableB", "tableA");

        InsertDataChangeValidator validator = new InsertDataChangeValidator();
        validator.validate(change); // memorise insert into tableA from module x
        validator.validate(change); // memorise insert into tableB from module x

        // this should fails because insert into tableA but from module y
        Collection<ValidationError> issues = validator.validate(change);
        Assert.assertThat(issues.size(), Matchers.greaterThan(0));
        Assert.assertThat(ValidatorUtil.extractMessage(issues), CoreMatchers.hasItems(CoreMatchers.endsWith("The table name tableA is already used in x project")));
    }

}
