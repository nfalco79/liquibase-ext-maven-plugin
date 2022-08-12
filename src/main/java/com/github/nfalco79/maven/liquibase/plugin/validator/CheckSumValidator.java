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

import liquibase.change.CheckSum;
import liquibase.changelog.ChangeSet;

/**
 * Validator to check if the validCheckSum has not allowed values to skip the check.
 */
@Validator(name = "checksum")
public class CheckSumValidator implements IChangeSetValidator {
    private static final List<String> INVALID_CHECKSUM = Arrays.asList("1:any", "1:all", "1:1:*", "1:*");

    @Override
    public Collection<ValidationError> validate(ChangeSet changeSet) {
        Collection<ValidationError> issues = new LinkedList<>();

        for (CheckSum validCheckSum : changeSet.getValidCheckSums()) {
            String md5 = validCheckSum.toString().toLowerCase();
            if (INVALID_CHECKSUM.contains(md5)) {
                issues.add(LiquibaseUtil.createIssueForElement(changeSet, "validCheckSum", "the " + md5 + " validateCheckSum is not allowed"));
            }
        }

        return issues;
    }


}
