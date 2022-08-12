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

import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.AddNotNullConstraintChange;
import liquibase.change.core.CreateTableChange;

/**
 * Validator to check if tenant_id column is not nullable.
 */
@Validator(name = "tenantIdNotNull")
public class TenantIdNotNullValidator implements IChangeValidator {

    private static final String TENANT_ID = "tenant_id";
    private static final String COLUMN = "column";
    private static final String MESSAGE = "tenant_id column must be not nullable";

    @Override
    public Collection<ValidationError> validate(Change change) {
        Collection<ValidationError> issues = new ArrayList<>();

        if (change instanceof AddColumnChange) {
            AddColumnChange addColumnChange = (AddColumnChange) change;
            ColumnConfig column = addColumnChange.getColumns().stream().filter(c -> TENANT_ID.equals(c.getName())).findFirst().orElse(null);

            if (column != null && isNullable(column) && !hasNotNullConstraint(change, addColumnChange.getTableName())) {
                issues.add(LiquibaseUtil.createIssue(change, COLUMN, null, MESSAGE));
            }
        } else if (change instanceof CreateTableChange) {
            CreateTableChange createTableChange = (CreateTableChange) change;
            ColumnConfig column = createTableChange.getColumns().stream().filter(c -> TENANT_ID.equals(c.getName())).findFirst().orElse(null);

            if (column != null && isNullable(column) && !hasNotNullConstraint(change, createTableChange.getTableName())) {
                issues.add(LiquibaseUtil.createIssue(change, COLUMN, null, MESSAGE));
            }

        }

        return issues;
    }

    private boolean isNullable(ColumnConfig column) {
        ConstraintsConfig constraints = column.getConstraints();
        return constraints == null || constraints.isNullable() == null || Boolean.TRUE.equals(constraints.isNullable());
    }

    private boolean hasNotNullConstraint(Change change, String tableName) {
        List<Change> changes = change.getChangeSet().getChanges();

        int idx;
        for (idx = 0; idx < changes.size(); idx++) {
            if (changes.get(idx) == change) {
                break;
            }
        }
        if (idx >= changes.size()) {
            return false;
        }

        return changes.stream() //
                .skip(idx + 1l) //
                .filter(AddNotNullConstraintChange.class::isInstance) //
                .map(AddNotNullConstraintChange.class::cast) //
                .anyMatch(c -> TENANT_ID.equals(c.getColumnName()) && c.getTableName().equals(tableName));
    }

}
