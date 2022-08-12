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
import liquibase.change.core.DropColumnChange;

/**
 * Check that drop column change manages only nested columns or columName attribute.
 */
@Validator(name = "dropColumnChange")
public class DropColumnValidator implements IChangeValidator {

    @Override
    public Collection<ValidationError> validate(Change change) {
        Collection<ValidationError> issues = new ArrayList<>();

        if (change instanceof DropColumnChange) {
            DropColumnChange dropColumnChange = (DropColumnChange) change;
            String columnName = dropColumnChange.getColumnName();
            List<ColumnConfig> columns = dropColumnChange.getColumns();
            if (columnName != null && !columns.isEmpty()) {
                String message = "Drop column does not permit columnName attribute and nested columns to be declared at the same time";
                issues.add(LiquibaseUtil.createIssue(change, "columnName", message));
            }
        }

        return issues;
    }
}
