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
import java.util.HashMap;
import java.util.Map;

import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;
import com.github.nfalco79.maven.liquibase.plugin.validator.Validator.Scope;

import liquibase.change.Change;
import liquibase.change.core.InsertDataChange;

/**
 * Check that insert data change manages only tables in their scope.
 */
@Validator(name = "insertDataChange", scope = Scope.SINGLETON)
public class InsertDataChangeValidator implements IChangeValidator {

    private Map<String, String> tablesCache = new HashMap<>();

    /**
     * Returns the list of errors.
     *
     * @param change
     *            the change to validate
     * @return the list of errors
     */
    @Override
    public Collection<ValidationError> validate(Change change) {
        Collection<ValidationError> issues = new ArrayList<>();

        if (change instanceof InsertDataChange) {
            InsertDataChange insertDataChange = (InsertDataChange) change;

            // Fill the map with the name of the table as key and the
            // logicalFilePath as value
            String tableName = insertDataChange.getTableName();
            String logicalFilePath = insertDataChange.getChangeSet().getChangeLog().getLogicalFilePath();
            if (!tablesCache.containsKey(tableName)) {
                tablesCache.put(tableName, logicalFilePath);
            } else {
                String filePath = tablesCache.get(tableName);
                if (!filePath.equals(logicalFilePath)) {
                    String message = "The table name " + tableName + " is already used in " + filePath + " project";
                    issues.add(LiquibaseUtil.createIssue(change, "name", message));
                }
            }
        }

        return issues;
    }

}
