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

import org.apache.commons.lang3.ObjectUtils;

import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.InsertDataChange;

/**
 * Check that insert data change manages only columns with defined value.
 */
@Validator(name = "insertColumnsChange")
public class InsertColumnsValidator implements IChangeValidator {

    @Override
    public Collection<ValidationError> validate(Change change) {
        Collection<ValidationError> issues = new ArrayList<>();

        if (change instanceof InsertDataChange) {
            InsertDataChange insertDataChange = (InsertDataChange) change;
            String tableName = insertDataChange.getTableName();
            List<ColumnConfig> columns = insertDataChange.getColumns();
            boolean containsValueClobOrBlob = columns.stream().anyMatch(col -> ObjectUtils.anyNotNull(col.getValueClobFile(), col.getValueBlobFile()));
            if (containsValueClobOrBlob && columns.stream().anyMatch(col -> !ObjectUtils.anyNotNull( //
                    col.getValue(), col.getValueNumeric(), //
                    col.getValueBoolean(), col.getValueDate(), //
                    col.getValueClobFile(), col.getValueBlobFile()))) {
                // Liquibase SQL generation for DB2 doesn't work well whenever in the same insert change there
                // are a column with CLOB or BLOB value and a column without any value attribute defined
                String message = "Insert data does not permit any column of table " + tableName + " to be declared without a value";
                issues.add(LiquibaseUtil.createIssue(change, "value", message));
            }
        }

        return issues;
    }
}
