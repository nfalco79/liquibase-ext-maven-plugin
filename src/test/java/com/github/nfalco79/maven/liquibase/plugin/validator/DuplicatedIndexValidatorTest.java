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

import liquibase.change.AddColumnConfig;
import liquibase.change.Change;
import liquibase.change.core.CreateIndexChange;
import liquibase.change.core.DropIndexChange;
import liquibase.changelog.ChangeSet;

public class DuplicatedIndexValidatorTest {
    private ValidatorFactory validatorFactory;

    @Before
    public void setup() {
        validatorFactory = new ValidatorFactory();
        validatorFactory.setIgnoreRules(Collections.emptySet());
    }

    @Test
    public void indexes_on_same_table_different_order_not_raise_exception() {
        ChangeSet changeSet = ValidatorUtil.getChangeSet();

        changeSet.addChange(newCreateIndexChange("idx1", "t1", "col1", "col2"));
        changeSet.addChange(newCreateIndexChange("idx2", "t1", "col2", "col1"));

        Collection<ValidationError> violations = new ArrayList<>();
        for (Change change : changeSet.getChanges()) {
            IChangeValidator validator = validatorFactory.newValidator(change);
            violations.addAll(validator.validate(change));
        }

        Assert.assertTrue("We expect violations for duplicated index", violations.isEmpty());
    }

    @Test
    public void indexes_on_same_table_and_column_raise_exception() {
        ChangeSet changeSet = ValidatorUtil.getChangeSet();

        changeSet.addChange(newCreateIndexChange("idx1", "t1", "col1", "col2"));
        changeSet.addChange(newCreateIndexChange("idx2", "t1", "col1", "col2"));

        Collection<ValidationError> violations = new ArrayList<>();
        for (Change change : changeSet.getChanges()) {
            IChangeValidator validator = validatorFactory.newValidator(change);
            violations.addAll(validator.validate(change));
        }

        Assert.assertFalse("We expect violations for duplicated index", violations.isEmpty());
        Iterator<ValidationError> issueIterator = violations.iterator();
        Assert.assertThat(issueIterator.next().getMessage(), CoreMatchers.equalTo("The index idx2 is already defined by idx1"));
    }

    @Test
    public void index_created_dropped_created_does_not_raise_exception() {
        ChangeSet changeSet = ValidatorUtil.getChangeSet();

        changeSet.addChange(newCreateIndexChange("idx1", "t1", "col1", "col2"));
        changeSet.addChange(dropIndexChange("idx1", "t1"));
        changeSet.addChange(newCreateIndexChange("idx2", "t1", "col1", "col2"));

        Collection<ValidationError> violations = new ArrayList<>();
        for (Change change : changeSet.getChanges()) {
            IChangeValidator validator = validatorFactory.newValidator(change);
            violations.addAll(validator.validate(change));
        }

        Assert.assertTrue("We do not expect violations for duplicated index here one has been dropped", violations.isEmpty());
    }

    private Change dropIndexChange(String idxName, String tableName) {
        DropIndexChange change = new DropIndexChange();
        change.setIndexName(idxName);
        change.setTableName(tableName);
        return change;
    }

    private CreateIndexChange newCreateIndexChange(String idxName, String tableName, String... columns) {
        CreateIndexChange change = new CreateIndexChange();
        change.setIndexName(idxName);
        change.setTableName(tableName);
        for (String column : columns) {
            change.getColumns().add(newColumn(column));
        }
        return change;
    }

    private AddColumnConfig newColumn(String name) {
        AddColumnConfig column = new AddColumnConfig();
        column.setType("INTEGER");
        column.setName(name);
        return column;
    }

}