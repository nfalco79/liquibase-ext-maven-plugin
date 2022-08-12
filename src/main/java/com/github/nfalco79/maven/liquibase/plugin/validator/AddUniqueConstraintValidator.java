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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ChangeStorage;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ColumnInfo;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ColumnKey;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.IStorageInfo;

import liquibase.change.Change;
import liquibase.change.core.AddUniqueConstraintChange;

/**
 * Check that add unique constraint change include only non nullable columns.
 */
@Validator(name = "addUnique")
public class AddUniqueConstraintValidator implements IChangeValidator {

    private static final String COLUMN_NAMES = "columnNames";

    /**
     * Main method of the class to validate the addUniqueConstraint for a
     * column.
     *
     * @param change instance of change
     * @param storage information stored by previous changes
     * @return the collection of validation errors
     */
    @Override
    public Collection<ValidationError> validate(Change change, ChangeStorage storage) {
        Collection<ValidationError> issues = new LinkedList<>();

        if (change instanceof AddUniqueConstraintChange) {
            AddUniqueConstraintChange addUniqueConstraint = (AddUniqueConstraintChange) change;
            List<String> nullableColumns = new ArrayList<>();

            String tableName = addUniqueConstraint.getTableName();
            String columnsNames = addUniqueConstraint.getColumnNames();
            String[] uniqueConstraintCols = columnsNames.split(",\\s*");

            // Iterate over the columns involved in addUniqueConstraint
            for (String colName : uniqueConstraintCols) {
                IStorageInfo col = storage.get(new ColumnKey(tableName, colName));
                if (col != null && ((ColumnInfo) col.getValue()).isNullable()) {
                    nullableColumns.add(colName);
                }
            }

            if (!nullableColumns.isEmpty()) {
                String message = buildValidationMessage(nullableColumns, tableName, addUniqueConstraint.getConstraintName());
                issues.add(LiquibaseUtil.createIssue(change, COLUMN_NAMES, message));
            }
        }
        return issues;
    }

    private String buildValidationMessage(List<String> nullableColumns, String tableName, String constraintName) {
        String message = "Unique constraint";

        if (constraintName != null) {
            message += " '" + constraintName + "'";
        }
        message += " can not include nullable columns " + StringUtils.join(nullableColumns, ", ");
        return message + " for table '" + tableName + "'.";
    }

    @Override
    public Collection<ValidationError> validate(Change change) {
        return Collections.emptyList();
    }

}
