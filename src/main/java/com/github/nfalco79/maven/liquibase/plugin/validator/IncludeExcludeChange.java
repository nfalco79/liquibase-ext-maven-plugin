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
import java.util.Set;

import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;

import liquibase.change.Change;

/**
 * Check if change is allowed.
 */
@Validator(name = "filterChanges")
public class IncludeExcludeChange implements IChangeValidator {

    private final Set<String> includeChanges;
    private final Set<String> excludeChanges;

    /**
     * Constructs an instance of this class.
     *
     * @param includeChanges
     *            the allowed changes
     * @param excludeChanges
     *            the forbidden changes
     */
    public IncludeExcludeChange(Set<String> includeChanges, Set<String> excludeChanges) {
        Set<String> empty = Collections.<String> emptySet();
        this.includeChanges = includeChanges != null ? includeChanges : empty;
        this.excludeChanges = excludeChanges != null ? excludeChanges : empty;
    }

    @Override
    public Collection<ValidationError> validate(Change change) {
        Collection<ValidationError> issues = new LinkedList<>();

        String changeName = LiquibaseUtil.getChangeName(change.getClass());
        if (excludeChanges.contains(changeName)) {
            issues.add(LiquibaseUtil.createIssue(change, null, "The change " + changeName + " is not allowed by the exclusion filter"));
        } else if (!includeChanges.isEmpty() && !includeChanges.contains(changeName)) {
            issues.add(LiquibaseUtil.createIssue(change, null, "The change " + changeName + " is not allowed by the inclusion filter"));
        }

        return issues;
    }

}
