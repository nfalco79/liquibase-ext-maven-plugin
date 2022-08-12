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
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;
import com.github.nfalco79.maven.liquibase.plugin.util.StringUtil;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ChangeStorage;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ColumnInfo;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ColumnKey;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ConstraintInfo;

import liquibase.change.Change;
import liquibase.change.core.ModifyDataTypeChange;
import liquibase.change.core.RenameColumnChange;

/**
 * Check column constraints of any change which attempts to alter a column.
 */
@Validator(name = "columnConstraints")
public class ColumnConstraintsValidator implements IChangeValidator {

    /**
     * Main method of the class to validate the absence of dependencies for a
     * column.
     *
     * @param change instance of change
     * @param storage information stored by previous changes
     * @return the collection of validation errors
     */
    @Override
    public Collection<ValidationError> validate(Change change, ChangeStorage storage) {
        Collection<ValidationError> issues = new LinkedList<>();

        if (change instanceof RenameColumnChange) {
            RenameColumnChange renameColumn = (RenameColumnChange) change;
            String tableName = renameColumn.getTableName();
            String newColumnName = renameColumn.getNewColumnName();
            String oldColumnName = renameColumn.getOldColumnName();

            Optional<ColumnInfo> colInfo = storage.filterBy(ColumnInfo.class, new ColumnKey(tableName, newColumnName)).findFirst();
            if (colInfo.isPresent()) {
                List<ConstraintInfo> consInfo = colInfo.get().getConstraints();
                if (!consInfo.isEmpty()) {
                    String message = "Can not rename column " + tableName + "." + oldColumnName
                            + " because is referred by " + StringUtils.join(consInfo, ", ")
                            + ". Remove constraints before rename and then recreate them.";
                    issues.add(LiquibaseUtil.createIssue(change, null, message));
                }
            }
        } else if (change instanceof ModifyDataTypeChange) {
            ModifyDataTypeChange modifyDataType = (ModifyDataTypeChange) change;
            String tableName = modifyDataType.getTableName();
            String columnName = modifyDataType.getColumnName();

            Optional<ColumnInfo> colInfo = storage.filterBy(ColumnInfo.class, new ColumnKey(tableName, columnName)).findFirst();
            if (colInfo.isPresent()) {
                List<ConstraintInfo> consInfo = colInfo.get().getConstraints();
                String colOldType = colInfo.get().getOldType();
                String newDataType = StringUtil.removeParam(modifyDataType.getNewDataType());
                if (!consInfo.isEmpty() && !colOldType.equalsIgnoreCase(newDataType)) {
                    String message = "Can not modify column type " + tableName + "." + columnName
                            + " because is referred by " + StringUtils.join(consInfo, ", ")
                            + ". Remove constraints before modify and then recreate them.";
                    issues.add(LiquibaseUtil.createIssue(change, null, message));
                }
            }
        }
        return issues;
    }

    @Override
    public Collection<ValidationError> validate(Change change) {
        return Collections.emptyList();
    }

}
