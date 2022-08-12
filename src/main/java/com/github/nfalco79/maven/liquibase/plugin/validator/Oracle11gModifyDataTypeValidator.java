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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;
import com.github.nfalco79.maven.liquibase.plugin.util.StringUtil;

import liquibase.change.Change;
import liquibase.change.core.ModifyDataTypeChange;

/**
 * Specific validator for Oracle 11g database. It validate ModifyDataType
 * changes.
 */
@Validator(name = "oracleModifyDataType")
public class Oracle11gModifyDataTypeValidator implements IChangeValidator {

    private static final List<String> NOT_PERMITTED_TYPE = Arrays.asList("CLOB", "BLOB");

    @Override
    public Collection<ValidationError> validate(Change c) {
        List<ValidationError> issues = new LinkedList<>();

        String newDataType = ((ModifyDataTypeChange) c).getNewDataType().toUpperCase();
        String rawDataType = StringUtil.removeParam(newDataType);

        if (NOT_PERMITTED_TYPE.contains(rawDataType)) {
            issues.add(LiquibaseUtil.createIssue(c, "column", "modifyDataType", "Oracle 11g not allows modify data type to " + rawDataType
                    + ". If you are trying to resize the column use resizeDataType instead of modifyDataType. If not, the correct way to proceed is add a new column and then move data to the new column"));
        }

        return issues;
    }

}
