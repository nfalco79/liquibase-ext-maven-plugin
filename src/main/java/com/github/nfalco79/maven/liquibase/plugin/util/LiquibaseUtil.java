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
package com.github.nfalco79.maven.liquibase.plugin.util;

import com.github.nfalco79.maven.liquibase.plugin.validator.ValidationContext;
import com.github.nfalco79.maven.liquibase.plugin.validator.ValidationError;
import com.github.nfalco79.maven.liquibase.plugin.validator.Validator;

import liquibase.change.Change;
import liquibase.change.DatabaseChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;

public final class LiquibaseUtil {

    private LiquibaseUtil() {
    }

    /**
     * Return the name of the element defined in the annotation.
     *
     * @param class1
     *            Class class
     * @return the name of the change
     */
    public static String getChangeName(Class<?> class1) {
        DatabaseChange annotation = class1.getAnnotation(DatabaseChange.class);
        return annotation != null ? annotation.name() : "no element";
    }

    /**
     * Return the name of the element defined in the annotation.
     *
     * @param class1
     *            Class class
     * @return the name of the validator
     */
    public static String getValidatorName(Class<?> class1) {
        Validator annotation = class1.getAnnotation(Validator.class);
        return annotation != null ? annotation.name() : "no element";
    }

    public static ValidationError createIssue(Change change, String attribute, String message) {
        ChangeSet changeSet = change.getChangeSet();

        ValidationError issue = createIssue(changeSet, attribute, message);
        issue.setElement(getChangeName(change.getClass()));
        issue.setChangeSetId(changeSet.getId());
        issue.setChangeSetAuthor(changeSet.getAuthor());

        return issue;
    }

    public static ValidationError createIssue(ChangeSet changeSet, String attribute, String message) {
        ValidationError issue = createIssue(changeSet.getChangeLog(), attribute, message);
        issue.setChangeSetId(changeSet.getId());
        issue.setChangeSetAuthor(changeSet.getAuthor());

        return issue;
    }

    public static ValidationError createIssueForElement(ChangeSet changeSet, String element, String message) {
        ValidationError issue = createIssue(changeSet.getChangeLog(), null, message);
        issue.setChangeSetId(changeSet.getId());
        issue.setChangeSetAuthor(changeSet.getAuthor());
        issue.setElement(element);

        return issue;
    }

    public static ValidationError createIssue(DatabaseChangeLog changeLog, String attribute, String message) {
        ValidationError issue = new ValidationError();
        issue.setFile(changeLog.getPhysicalFilePath());
        issue.setMessage(message);
        issue.setAttribute(attribute);

        return issue;
    }

    public static ValidationError createIssue(Change change, String elementName, String attribute, String message) {
        ValidationError issue = createIssue(change, attribute, message);
        issue.setElement(elementName);
        return issue;
    }

    public static ValidationError createIssue(ValidationContext context, String message) {
        ValidationContext ctx = context;
        while (ctx != null && !(ctx.getSubject() instanceof Change)) {
            ctx = ctx.getParent();
        }

        if (ctx == null) {
            throw new IllegalStateException("context not valid");
        }

        Object subject = ctx.getSubject();
        @SuppressWarnings("unchecked")
        String elementName = LiquibaseUtil.getChangeName((Class<? extends DatabaseChange>) subject.getClass());
        String attributeName = context.getAttributeName();
        return createIssue((Change) subject, elementName, attributeName, message);
    }

}