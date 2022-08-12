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

public class ValidationError {

    private String file;
    private String element;
    private String attribute;
    private String message;
    private String changeSetId;
    private String changeSetAuthor;

    public String getFile() {
        return file;
    }

    public ValidationError setFile(String file) {
        this.file = file;
        return this;
    }

    public String getElement() {
        return element;
    }

    public ValidationError setElement(String element) {
        this.element = element;
        return this;
    }

    public String getAttribute() {
        return attribute;
    }

    public ValidationError setAttribute(String attribute) {
        this.attribute = attribute;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ValidationError setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getChangeSetId() {
        return changeSetId;
    }

    public ValidationError setChangeSetId(String changeSetId) {
        this.changeSetId = changeSetId;
        return this;
    }

    public String getChangeSetAuthor() {
        return changeSetAuthor;
    }

    public ValidationError setChangeSetAuthor(String changeSetAuthor) {
        this.changeSetAuthor = changeSetAuthor;
        return this;
    }

    @Override
    public String toString() {
        return file + " changeSet " + changeSetId + " (" + changeSetAuthor + ") has a violation on " //
                + (attribute != null ? "attribute " + attribute + " of " : "") //
                + "element " + element + ", " + message;
    }

}