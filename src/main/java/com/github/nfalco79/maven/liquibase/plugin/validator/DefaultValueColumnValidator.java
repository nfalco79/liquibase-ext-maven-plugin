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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.InsertDataChange;

/**
 * Check that value and defaultValue attributes are suitable with the value
 * type.
 */
@Validator(name = "value")
public class DefaultValueColumnValidator implements IChangeValidator {

    private static final String TENANT_ID = "tenant_id";
    private static final String LONG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String SHORT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DECIMAL_REGEXP = "[+-]?[0-9]+(\\.([0-9]+))?";

    @Override
    public Collection<ValidationError> validate(Change change) { //NOSONAR
        Collection<ValidationError> issues = new ArrayList<>();
        List<? extends ColumnConfig> columns = new ArrayList<>();
        String tableName = null;
        Pattern pattern = Pattern.compile(DECIMAL_REGEXP); //NOSONAR

        if (change instanceof InsertDataChange) {
            InsertDataChange insertDataChange = (InsertDataChange) change;
            tableName = insertDataChange.getTableName();
            columns = insertDataChange.getColumns();
        } else if (change instanceof CreateTableChange) {
            CreateTableChange createTableChange = (CreateTableChange) change;
            tableName = createTableChange.getTableName();
            columns = createTableChange.getColumns();
        } else if (change instanceof AddColumnChange) {
            AddColumnChange addColumnChange = (AddColumnChange) change;
            tableName = addColumnChange.getTableName();
            columns = addColumnChange.getColumns();
        }

        for (ColumnConfig column : columns) {
            String name = column.getName();
            String value = column.getValue();

            String attributeName = "value";
            if (value == null) {
                value = column.getDefaultValue();
                attributeName = "defaultValue";
            }

            if (value != null && !TENANT_ID.equals(name)) {
                // We get the proper attribute depending on the given value
                // Currently there is no way to infer the column type from whatever change
                if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                    // Doesn't work in case such as "t_value" column, where type is VARCHAR but accepted values are "true" and "false" also
                    String message = "Use the specific " + attributeName + "Boolean attribute for " + tableName + "." + name;
                    issues.add(LiquibaseUtil.createIssue(change, "name", message));
                } else if (isDate(value)) {
                    String message = "Use the specific " + attributeName + "Date attribute for " + tableName + "." + name;
                    issues.add(LiquibaseUtil.createIssue(change, "name", message));
                } else if (pattern.matcher(value).matches()) {
                    // Accepts big integers and decimal
                    // Doesn't work in case such as foreign keys ("*_fk" or "*id"), where type is VARCHAR but accepted values are both strings and numbers
                    String message = "Use the specific " + attributeName + "Numeric attribute for " + tableName + "." + name;
                    issues.add(LiquibaseUtil.createIssue(change, "name", message));
                }
            }
        }

        return issues;
    }

    private boolean isDate(String value) {
        try {
            SimpleDateFormat simpleDateFormat = value.length() > 10
                    ? new SimpleDateFormat(LONG_DATE_FORMAT)
                    : new SimpleDateFormat(SHORT_DATE_FORMAT);
            simpleDateFormat.parse(value);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }
}
