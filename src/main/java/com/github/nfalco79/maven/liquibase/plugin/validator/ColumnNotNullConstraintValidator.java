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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;

import liquibase.change.AddColumnConfig;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.AddNotNullConstraintChange;
import liquibase.change.core.CreateTableChange;
import liquibase.changelog.ChangeSet;

/**
 * Validator to check the existence of a default value for column whether a not
 * null constraint is applied.
 */
@Validator(name = "notNullConstraint")
public class ColumnNotNullConstraintValidator implements IChangeValidator {

    private static final String COLUMN = "column";
    private static final String DEFAULT_NULL_VALUE = "defaultNullValue";

    /**
     * Main method of the class to validate the nullable constraint for a
     * column.
     *
     * @param change instance of change
     * @return the collection of validation errors
     */
    @Override
    public Collection<ValidationError> validate(Change change) { // NOSONAR
        Collection<ValidationError> issues = new LinkedList<>();

        if (change instanceof AddNotNullConstraintChange) {
            AddNotNullConstraintChange addNotNullConstraintChange = (AddNotNullConstraintChange) change;

            if (addNotNullConstraintChange.getDefaultNullValue() == null && !isColumndInChangeset(addNotNullConstraintChange)) {
                String message = "You can not add not nullable constraint without a default value.";
                issues.add(LiquibaseUtil.createIssue(change, LiquibaseUtil.getChangeName(change.getClass()), DEFAULT_NULL_VALUE, message));
            }
        } else if (change instanceof AddColumnChange) {
            AddColumnChange addColumnChange = (AddColumnChange) change;

            for (ColumnConfig column : addColumnChange.getColumns()) {
                ConstraintsConfig constraints = column.getConstraints();
                if (constraints != null && Boolean.FALSE.equals(constraints.isNullable())
                        && column.getDefaultValueObject() == null) {
                    String message = "You can not add not nullable columns without a default value.";
                    issues.add(LiquibaseUtil.createIssue(change, COLUMN, null, message));
                }
            }
        }

        return issues;
    }

    private List<Change> getChangesList(AddNotNullConstraintChange addNotNullConstraintChange) {
        ChangeSet changeset = addNotNullConstraintChange.getChangeSet();
        List<Change> changesList = changeset.getChanges();
        for (int i = 0; i < changesList.size(); i++) {
            Change changeItem = changesList.get(i);
            if (changeItem == addNotNullConstraintChange) {
                return changesList.subList(0, i);
            }
        }
        return Collections.emptyList();
    }

    private boolean isColumndInChangeset(AddNotNullConstraintChange addNotNullConstraintChange) {
        List<Change> changesList = getChangesList(addNotNullConstraintChange);

        for (Change changeItem : changesList) {
            String columnName = addNotNullConstraintChange.getColumnName();
            String tableName = addNotNullConstraintChange.getTableName();

            if (changeItem instanceof AddColumnChange) {
                AddColumnChange addColumn = (AddColumnChange) changeItem;
                if (isColumnInAddColumnChange(addColumn, columnName, tableName)) {
                    return true;
                }
            } else if (changeItem instanceof CreateTableChange) {
                CreateTableChange createTable = (CreateTableChange) changeItem;
                if (isColumnInCreateTableChange(createTable, columnName, tableName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isColumnInAddColumnChange(AddColumnChange addColumn, String columnName, String tableName) {
        List<AddColumnConfig> columns = addColumn.getColumns();

        for (AddColumnConfig column : columns) {
            if (column.getName().equals(columnName) && addColumn.getTableName().equals(tableName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isColumnInCreateTableChange(CreateTableChange createTable, String columnName, String tableName) {
        List<ColumnConfig> columns = createTable.getColumns();

        for (ColumnConfig column : columns) {
            if (column.getName().equals(columnName) && createTable.getTableName().equals(tableName)) {
                return true;
            }
        }
        return false;
    }

}
