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

import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;

import liquibase.change.Change;
import liquibase.change.core.CreateViewChange;

/**
 * Check that changeset createView doesn't contain attribute replaceIfExists.
 */
@Validator(name = "replaceView")
public class ReplaceViewValidator implements IChangeValidator {

    @Override
    public Collection<ValidationError> validate(Change change) {
        Collection<ValidationError> issues = new LinkedList<>();

        if (change instanceof CreateViewChange) {
            CreateViewChange createView = (CreateViewChange) change;
            Boolean replaceIfExists = createView.getReplaceIfExists();
            if (replaceIfExists != null) {
                issues.add(LiquibaseUtil.createIssue(change, "replaceIfExists", "Create view cannot contain attribute replaceIfExists"));
            }
        }

        return issues;
    }

}
