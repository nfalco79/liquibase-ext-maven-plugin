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
import java.util.Objects;

import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;
import com.github.nfalco79.maven.liquibase.plugin.validator.Validator.Scope;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;

/**
 * Validator to check if a changeSet if is duplicated in the same module.
 */
@Validator(name = "duplicatedId", scope = Scope.SINGLETON)
public class DuplicatedIdValidator implements IChangeSetValidator {

    private Map<String, ChangeSetId> store = new HashMap<>();

    @Override
    public Collection<ValidationError> validate(ChangeSet changeSet) {
        Collection<ValidationError> issues = new ArrayList<>();

        ChangeSetId info = new ChangeSetId(changeSet);
        String composedKey = info.toString();

        ChangeSetId stored = store.get(composedKey);
        if (stored == null) {
            store.put(composedKey, info);
        } else {
            // found a duplicate
            DatabaseChangeLog changeLog = changeSet.getChangeLog();
            String changeLogFile = changeLog.getPhysicalFilePath();

            String message = "the changeSet " + info.id + " is already defined in " + stored.file;
            // check file against previous entry
            if (changeLogFile.equals(stored.file)) {
                message = "the changeSet " + changeSet.getId() + " is already defined in the same file";
            }
            issues.add(LiquibaseUtil.createIssue(changeSet, "id", message));
        }

        return issues;
    }

    /**
     * The key used to identify previous occurrences of a changeSet.
     */
    private static final class ChangeSetId {
        private final String file;
        private final String module;
        private final String id;

        /**
         * Default constructor.
         *
         * @param changeSet
         *            the changeSet used to compose the key
         */
        private ChangeSetId(ChangeSet changeSet) {
            DatabaseChangeLog changeLog = changeSet.getChangeLog();
            this.module = changeLog.getLogicalFilePath();
            this.id = changeSet.getId();
            this.file = changeLog.getPhysicalFilePath();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hash(module, id);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ChangeSetId other = (ChangeSetId) obj;
            return Objects.equals(module, other.module) && Objects.equals(id, other.id);
        }

        @Override
        public String toString() {
            return module + "(" + id + ")";
        }

    }

}
