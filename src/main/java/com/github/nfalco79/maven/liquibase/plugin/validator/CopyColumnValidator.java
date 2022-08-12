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

import com.github.nfalco79.maven.liquibase.plugin.rule.DataTypeRule;
import com.github.nfalco79.maven.liquibase.plugin.rule.RuleEngine;
import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;
import com.github.nfalco79.maven.liquibase.plugin.util.StringUtil;

import liquibase.change.Change;
import liquibase.ext.nfalco79.CopyColumnChange;

/**
 * Check that attribute values are consistent with copy column operation.
 */
@Validator(name = "copyColumn")
public class CopyColumnValidator implements IChangeValidator {

    private static final List<String> ORACLE_TO_CLOB_DATATYPE = Arrays.asList("CHAR", "VARCHAR", "NCHAR", "NVARCHAR", "CLOB", "NCLOB", "NUMBER");
    private static final List<String> ORACLE_TO_BLOB_DATATYPE = Arrays.asList("BLOB");
    private static final List<String> ORACLE_TO_NCLOB_DATATYPE = Arrays.asList("NCLOB");

    @Override
    public Collection<ValidationError> validate(Change change) {
        Collection<ValidationError> issues = new LinkedList<>();

        RuleEngine ruleEngine = new RuleEngine();
        ruleEngine.add(new DataTypeRule(), new ValidationContext(change, "toType"));
        ruleEngine.add(new DataTypeRule(), new ValidationContext(change, "fromType"));

        issues.addAll(ruleEngine.execute());

        if (change instanceof CopyColumnChange) {
            CopyColumnChange copyChange = (CopyColumnChange) change;
            String toType = StringUtil.removeParam(copyChange.getToType()).toUpperCase();
            String fromType = StringUtil.removeParam(copyChange.getFromType()).toUpperCase();

            if ("CLOB".equals(toType) && !ORACLE_TO_CLOB_DATATYPE.contains(fromType)) {
                String message = fromType + " isn't one of types permitted by Oracle TO_CLOB function. Use one of these: "
                        + ORACLE_TO_CLOB_DATATYPE;
                issues.add(LiquibaseUtil.createIssue(change, "fromType", message));
            } else if ("BLOB".equals(toType) && !ORACLE_TO_BLOB_DATATYPE.contains(fromType)) {
                String message = "Copy column cannot convert " + fromType + " into " + toType + ".";
                issues.add(LiquibaseUtil.createIssue(change, "toType", message));
            } else if ("NCLOB".equals(toType) && !ORACLE_TO_NCLOB_DATATYPE.contains(fromType)) {
                String message = "Copy column cannot convert " + fromType + " into " + toType + ".";
                issues.add(LiquibaseUtil.createIssue(change, "toType", message));
            }
        }

        return issues;
    }

}
