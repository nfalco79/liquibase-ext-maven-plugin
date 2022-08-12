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
import java.util.regex.Pattern;

import org.codehaus.plexus.util.StringUtils;

import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;

import liquibase.changelog.ChangeSet;

/**
 * Check that changeset id match the issue pattern.
 */
@Validator(name = "issueId")
public class ChangeSetIssueIdValidator implements IChangeSetValidator {

    private static final String PROGESSIVE_PATTERN = "-\\d+$";
    private Pattern issuePattern;
    private Pattern progressivePattern;

    /**
     * Constructs an instance of this class.
     * 
     * @param basePattern
     *            the pattern to match
     */
    public ChangeSetIssueIdValidator(String basePattern) {
        issuePattern = Pattern.compile("^(?:" + basePattern + ")");
        progressivePattern = Pattern.compile(issuePattern.pattern() + PROGESSIVE_PATTERN);
    }

    @Override
    public Collection<ValidationError> validate(ChangeSet changeSet) {
        Collection<ValidationError> issues = new LinkedList<>();

        String changeSetId = changeSet.getId();
        if (!StringUtils.isEmpty(changeSetId)) {
            if (!issuePattern.matcher(changeSetId).find()) {
                issues.add(LiquibaseUtil.createIssue(changeSet, "id", "The changeset id does not matches the issue pattern"));
            } else if (!progressivePattern.matcher(changeSetId).matches()) {
                issues.add(LiquibaseUtil.createIssue(changeSet, "id", "The changeset id does not end with progressive number after the issue id"));
            }
        }

        String changeSetAuthor = changeSet.getAuthor();
        if (StringUtils.isEmpty(changeSetAuthor)) {
            issues.add(LiquibaseUtil.createIssue(changeSet, "author", "The author is required"));
        }

        return issues;
    }

}
