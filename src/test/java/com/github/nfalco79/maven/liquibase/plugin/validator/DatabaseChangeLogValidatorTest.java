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

import java.io.File;
import java.util.Collection;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import liquibase.changelog.DatabaseChangeLog;

public class DatabaseChangeLogValidatorTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public static final String ARTIFACT_ID = "com.acme.foo";

    @Test
    public void logicalfilepath_must_matches_artifact_id() {
        DatabaseChangeLog changeLog = new DatabaseChangeLog("/tmp/changelog.xml");
        changeLog.setLogicalFilePath("myfilepath");

        IChangeLogValidator validator = new FilePathValidator(ARTIFACT_ID);

        Collection<ValidationError> violations = validator.validate(changeLog);
        Assert.assertEquals("unexpected violations", 1, violations.size());
        Assert.assertThat(violations.iterator().next().getMessage(), CoreMatchers.endsWith("The logicalFilePath attribute does not match the artifactId of the project"));
    }

    @Test
    public void logicalfilepath_is_required() {
        DatabaseChangeLog changeLog = new DatabaseChangeLog("/tmp/changelog.xml");

        IChangeLogValidator validator = new FilePathValidator(ARTIFACT_ID);

        Collection<ValidationError> violations = validator.validate(changeLog);
        Assert.assertEquals("unexpected violations", 1, violations.size());
        Assert.assertThat(violations.iterator().next().getMessage(), CoreMatchers.endsWith("The logicalFilePath attribute is required"));
    }

    @Test
    public void logicalfilepath_matches_physical_file_path() throws Exception {
        String fileName = "logicalfilepath_match_changelog_file_path.xml";
        DatabaseChangeLog changeLog = ValidatorUtil.load(getClass(), fileName);

        IChangeLogValidator validator = new FilePathValidator(new File("com/github/nfalco79/maven/liquibase"));

        Collection<ValidationError> violations = validator.validate(changeLog);
        Assert.assertEquals("unexpected violations", 0, violations.size());
    }

    @Test
    public void logicalfilepath_does_not_match_physical_file_path() throws Exception {
        String fileName = "logicalfilepath_not_match_changelog_file_path.xml";
        DatabaseChangeLog changeLog = ValidatorUtil.load(getClass(), fileName);

        IChangeLogValidator validator = new FilePathValidator(new File("com/github/nfalco79/maven/liquibase"));

        Collection<ValidationError> violations = validator.validate(changeLog);
        Assert.assertEquals("unexpected violations", 1, violations.size());
        Assert.assertThat(violations.iterator().next().getMessage(), CoreMatchers.equalToObject("The logicalFilePath attribute does not match the artifactId of the project"));
    }

}