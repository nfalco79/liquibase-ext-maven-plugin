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

import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;
import com.github.nfalco79.maven.liquibase.plugin.util.ReflectionUtil;
import com.github.nfalco79.maven.liquibase.plugin.util.StringUtil;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;

/**
 * Specific validator for Oracle 11g database.
 */
@Validator(name = "oracleCLOB")
public class Oracle11gCLOBValidator implements IChangeValidator {

    private static final String COLUMNS = "columns";
    private static final String VARCHAR_TYPE = "varchar";
    private static final String NEW_DATA_TYPE = "newDataType";
    private static final int LENGTH = 4000;
    private static final String ISSUE_MESSAGE = "Oracle 11g not allows column of type VARCHAR greater than " + LENGTH + ", a CLOB must be used instead of that";

    @Override
    public Collection<ValidationError> validate(Change change) {
        List<ValidationError> issues = new LinkedList<>();
        @SuppressWarnings("unchecked")
        Collection<ColumnConfig> columns = (Collection<ColumnConfig>) ReflectionUtil.getFieldValue(COLUMNS, change);
        if (columns != null) {
            for (ColumnConfig column : columns) {
                String type = StringUtil.removeParam(column.getType());
                String typeLength = StringUtil.getParam(column.getType());
                if (VARCHAR_TYPE.equalsIgnoreCase(type) && Integer.parseInt(typeLength) > LENGTH) {
                    issues.add(LiquibaseUtil.createIssue(change, "column", "value", ISSUE_MESSAGE));
                }
            }
        }

        String fieldValue = (String) ReflectionUtil.getFieldValue(NEW_DATA_TYPE, change);
        if (VARCHAR_TYPE.equalsIgnoreCase(StringUtil.removeParam(fieldValue)) && Integer.parseInt(StringUtil.getParam(fieldValue)) > LENGTH) {
            issues.add(LiquibaseUtil.createIssue(change, NEW_DATA_TYPE, ISSUE_MESSAGE));
        }

        return issues;
    }
}