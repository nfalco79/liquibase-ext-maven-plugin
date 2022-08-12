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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ChangeStorage;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ColumnInfo;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ColumnKey;
import com.github.nfalco79.maven.liquibase.plugin.validator.sizextractor.ColumnRowSizeDB2;

import liquibase.change.AddColumnConfig;
import liquibase.change.Change;
import liquibase.change.core.CreateIndexChange;

/**
 * Check that during create index change the sum of the internal lengths of the
 * key columns doesn't exceed the allowable maximum.
 */
@Validator(name = "createIndex")
public class CreateIndexValidator implements IChangeValidator {

    private static final String COLUMN = "column";
    private final int maxLength;
    private IColumnRowSize extractor;

    /**
     * Constructs an instance of this class.
     *
     * @param maxLength the allowable maximum columns length
     */
    public CreateIndexValidator(final int maxLength) {
        this.maxLength = maxLength;
        extractor = new ColumnRowSizeDB2();
    }

    /**
     * Main method of the class to validate the index creation on a set of
     * columns.
     *
     * @param change instance of change
     * @param storage information stored by previous changes
     * @return the collection of validation errors
     */
    @Override
    public Collection<ValidationError> validate(Change change, ChangeStorage storage) {
        Collection<ValidationError> issues = new LinkedList<>();

        if (change instanceof CreateIndexChange) {
            CreateIndexChange createIndex = (CreateIndexChange) change;

            int internalSumLengths = 0;
            String tableName = createIndex.getTableName();
            List<AddColumnConfig> columns = createIndex.getColumns();

            // Iterate over the columns involved in index creation
            for (AddColumnConfig col : columns) {
                String colName = col.getName();
                Optional<ColumnInfo> colInfo = storage.filterBy(ColumnInfo.class, new ColumnKey(tableName, colName)).findFirst();
                if (colInfo.isPresent()) {
                    internalSumLengths += extractor.getSize(colInfo.get());
                }
            }

            if (internalSumLengths > maxLength) {
                String message = buildValidationMessage(internalSumLengths, tableName, createIndex.getIndexName());
                issues.add(LiquibaseUtil.createIssue(change, COLUMN, null, message));
            }
        }
        return issues;
    }

    private String buildValidationMessage(int internalSumLengths, String tableName, String indexName) {
        String message = "Key columns for index";
        if (indexName != null) {
            message += " '" + indexName + "'";
        }
        message += " of table " + tableName + " exceed the index key size limit " + maxLength + ".";
        return message;
    }

    @Override
    public Collection<ValidationError> validate(Change change) {
        Collection<ValidationError> issues = new LinkedList<>();
        return issues;
    }

}
