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
import java.util.stream.Collectors;

import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ChangeStorage;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ColumnInfo;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.TableKey;
import com.github.nfalco79.maven.liquibase.plugin.validator.sizextractor.ColumnRowSizeDB2;

import liquibase.change.Change;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.ModifyDataTypeChange;

/**
 * Check that the table involved in the change doesn't exceed the size limit.
 */
@Validator(name = "tableSize")
public class TableSizeValidator implements IChangeValidator {

    private static final String COLUMN = "column";
    private final int rowSizeLimit;
    private IColumnRowSize extractor;

    /**
     * Constructs an instance of this class.
     *
     * @param rowSizeLimit the max row size
     */
    public TableSizeValidator(final int rowSizeLimit) {
        this.rowSizeLimit = rowSizeLimit;
        extractor = new ColumnRowSizeDB2();
    }

    /**
     * Main method of the class to validate that every table size doesn't exceed
     * the row size limit.
     *
     * @param change instance of change
     * @param storage information stored by previous changes
     * @return the collection of validation errors
     */
    @Override
    public Collection<ValidationError> validate(Change change, ChangeStorage storage) {
        Collection<ValidationError> issues = new LinkedList<>();
        String tableName = null;

        if (change instanceof CreateTableChange) {
            CreateTableChange createTable = (CreateTableChange) change;
            tableName = createTable.getTableName();
        } else if (change instanceof AddColumnChange) {
            AddColumnChange addColumn = (AddColumnChange) change;
            tableName = addColumn.getTableName();
        } else if (change instanceof ModifyDataTypeChange) {
            ModifyDataTypeChange modifyType = (ModifyDataTypeChange) change;
            tableName = modifyType.getTableName();
        }

        int tableSize = 0;
        List<ColumnInfo> colInfos = storage.filterBy(ColumnInfo.class, new TableKey(tableName)).collect(Collectors.toList());
        for (ColumnInfo col : colInfos) {
            tableSize += extractor.getRowSize(col);
            if (tableSize > rowSizeLimit) {
                String message = "Table " + tableName + " with column " + col.getName()
                        + " exceeds the table limit of " + rowSizeLimit + ".";
                issues.add(LiquibaseUtil.createIssue(change, COLUMN, message));
                return issues;
            }
        }

        return issues;
    }

    @Override
    public Collection<ValidationError> validate(Change change) {
        return Collections.emptyList();
    }

}
