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
package com.github.nfalco79.maven.liquibase.plugin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.assertj.core.api.Assertions;
import org.codehaus.plexus.util.IOUtil;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import com.github.nfalco79.maven.liquibase.plugin.ValidateMojo.Extension;
import com.github.nfalco79.maven.liquibase.plugin.validator.IChangeValidator;
import com.github.nfalco79.maven.liquibase.plugin.validator.ValidationError;

import liquibase.change.Change;

public class ValidateMojoTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void verify_violations_on_columnNames_attribute() throws Exception {
        SystemStreamLog log = mock(SystemStreamLog.class);

        File changelog = getResource("changelog_columnNames.xml");
        File source = changelog.getParentFile();

        ValidateMojo mojo = spy(getMojo(source));
        mojo.setFailOnError(false);
        doReturn(log).when(mojo).getLog();

        mojo.execute();

        ArgumentCaptor<String> arguments = ArgumentCaptor.forClass(String.class);
        verify(log, atLeast(4 + 1)).warn(arguments.capture());

        String baseViolationMessage = "ChangeSet {0}, element {1} has a violation:";
        Assert.assertThat(arguments.getAllValues(), CoreMatchers.hasItems(CoreMatchers.containsString(MessageFormat.format(baseViolationMessage, "lowecase_columnnames", "addPrimaryKey"))));
        Assert.assertThat(arguments.getAllValues(), CoreMatchers.hasItems(CoreMatchers.containsString(MessageFormat.format(baseViolationMessage, "lowecase_columnnames", "addForeignKeyConstraint"))));
        Assert.assertThat(arguments.getAllValues(), CoreMatchers.hasItems(CoreMatchers.containsString(MessageFormat.format(baseViolationMessage, "length_columnnames", "addPrimaryKey"))));
        Assert.assertThat(arguments.getAllValues(), CoreMatchers.hasItems(CoreMatchers.containsString(MessageFormat.format(baseViolationMessage, "length_columnnames", "addForeignKeyConstraint"))));
    }

    @Test
    public void verify_changelog_and_changeset_progress_number() throws Exception {
        SystemStreamLog log = mock(SystemStreamLog.class);

        File changelog = getResource("changelog_with_issues2.xml");
        File source = changelog.getParentFile();

        ValidateMojo mojo = spy(getMojo(source));
        mojo.setFailOnError(false);
        doReturn(log).when(mojo).getLog();

        mojo.execute();

        ArgumentCaptor<String> arguments = ArgumentCaptor.forClass(String.class);
        verify(log, atLeast(3)).warn(arguments.capture());

        List<String> violationsMessage = arguments.getAllValues();
        Assert.assertThat(violationsMessage, CoreMatchers.hasItems("ChangeSet 123, has a violation: The changeset id does not end with progressive number after the issue id"));
        Assert.assertThat(violationsMessage, CoreMatchers.hasItems("ChangeSet 123, has a violation: The author is required"));
        Assert.assertThat(violationsMessage, CoreMatchers.hasItems("ChangeSet XYZ, has a violation: The changeset id does not end with progressive number after the issue id"));
        Assert.assertThat(violationsMessage, CoreMatchers.hasItems("ChangeSet XYZ, has a violation: The author is required"));
    }

    @Test
    public void verify_execution_is_skipped_when_skip_is_true() throws Exception {
        SystemStreamLog log = mock(SystemStreamLog.class);

        ValidateMojo mojo = spy(new ValidateMojo());
        mojo.setSkip(true);
        doReturn(log).when(mojo).getLog();

        mojo.execute();

        verify(log).info("Skip liquibase script validation");
    }

    @Test
    public void verify_duplicate_indexes() throws Exception {
        SystemStreamLog log = mock(SystemStreamLog.class);

        File changelog = getResource("changelog_duplicated_index.xml");
        File source = changelog.getParentFile();

        ValidateMojo mojo = spy(getMojo(source));
        mojo.setFailOnError(false);
        doReturn(log).when(mojo).getLog();
        List<ValidationError> issues = new LinkedList<>();
        doReturn(issues).when(mojo).newIssueContainer();

        mojo.execute();

        ValidationError expectedIssue = new ValidationError();
        expectedIssue.setMessage("The index i_table_key_idx2 is already defined by i_table_key_idx");
        expectedIssue.setElement("createIndex");
        expectedIssue.setChangeSetId("123-2");

        Assertions.assertThat(issues).usingElementComparatorOnFields("message", "element", "changeSetId").contains(expectedIssue);
    }

    @Test
    public void verify_skip_changeset() throws Exception {
        SystemStreamLog log = mock(SystemStreamLog.class);

        File changelog = getResource("changelog_skip_changeset.xml");
        File source = changelog.getParentFile();

        ValidateMojo mojo = spy(getMojo(source));
        doReturn(log).when(mojo).getLog();

        mojo.setSkipChangeSets(Arrays.asList("123-1", "123-2", "123-3"));
        mojo.execute();

        ArgumentCaptor<String> arguments = ArgumentCaptor.forClass(String.class);
        verify(log).warn(arguments.capture());

        Assertions.assertThat(arguments.getAllValues()) //
                .contains("Changeset 123-1 could not been skipped because it is not DB specific");

        arguments = ArgumentCaptor.forClass(String.class);
        verify(log, atLeast(2)).info(arguments.capture());

        Assertions.assertThat(arguments.getAllValues()) //
                .contains("Skip changeset 123-2 per configuration", "Skip changeset 123-3 per configuration");
    }

    @Test
    public void verify_that_failOnError_does_not_cause_exception() throws Exception {
        SystemStreamLog log = mock(SystemStreamLog.class);

        File changelog = getResource("changelog_with_issues.xml");
        File source = changelog.getParentFile();

        ValidateMojo mojo = spy(getMojo(source));
        mojo.setFailOnError(false);
        doReturn(log).when(mojo).getLog();

        ValidationError issue = new ValidationError();
        issue.setChangeSetId("id1");
        issue.setFile(changelog.getAbsolutePath());
        issue.setElement("element");
        issue.setMessage("message");
        IChangeValidator validator = newMockValidator(issue, issue, issue);
        doReturn(validator).when(mojo).newValidator(any(Change.class));

        mojo.execute();

        verify(log, never()).error(anyString());

        ArgumentCaptor<String> arguments = ArgumentCaptor.forClass(String.class);
        verify(log, atLeast(3 + 1)).warn(arguments.capture());

        Iterator<String> loggedViolations = arguments.getAllValues().listIterator();
        Assert.assertThat(loggedViolations.next(), CoreMatchers.containsString("There are violations on changelog " + issue.getFile()));
        String message = MessageFormat.format("ChangeSet {0}, element {1} has a violation: {2}", issue.getChangeSetId(), issue.getElement(), issue.getMessage());
        while (loggedViolations.hasNext()) {
            Assert.assertThat(loggedViolations.next(), CoreMatchers.containsString(message));
        }
    }

    private IChangeValidator newMockValidator(ValidationError... issue) {
        IChangeValidator validator = mock(IChangeValidator.class);
        when(validator.validate(any(Change.class))).thenReturn(Arrays.asList(issue));
        return validator;
    }

    @Test
    public void verify_that_raise_exception_on_scripts_that_contains_violations() throws Exception {
        File changelog = getResource("changelog_with_issues.xml");
        File source = changelog.getParentFile();

        ValidateMojo mojo = getMojo(source);
        mojo.setFailOnError(true);

        thrown.expect(MojoFailureException.class);
        thrown.expectMessage("There are violations on changelogs");

        mojo.execute();
    }

    @Test
    public void verify_files_inclusion() throws Exception {
        int changelogs = 5;

        File source = folder.newFile().getParentFile();
        for (int i = 0; i < changelogs - 1; i++) {
            folder.newFile();
        }

        ValidateMojo mojo = getMojo(source);
        mojo.setIncludes(new String[] { "**/*" });

        Assert.assertEquals(changelogs, mojo.getChangeLogs().length);
    }

    @Test
    public void verify_files_exclusion() throws Exception {
        int changelogs = 5;

        File source = folder.newFile("exclude1.xml").getParentFile();
        folder.newFile("exclude2.xml");
        for (int i = 0; i < changelogs; i++) {
            folder.newFile(i + ".xml");
        }

        ValidateMojo mojo = getMojo(source);
        mojo.setExcludes(new String[] { "**/exclude*.xml" });

        Assert.assertEquals(changelogs, mojo.getChangeLogs().length);
    }

    @Test
    public void verify_that_fails_if_source_does_not_exists() throws Exception {
        SystemStreamLog log = mock(SystemStreamLog.class);

        File source = new File("test");

        ValidateMojo mojo = spy(getMojo(source));
        doReturn(log).when(mojo).getLog();

        mojo.execute();

        verify(log).debug("The directory " + source.getAbsolutePath() + " does not exists, skipping validation");
    }

    @Test
    public void verify_includeChanges_and_excludeChanges_filter_could_not_contains_common_changes() throws Exception {
        File changelog = getResource("changelog_filter_on_changes.xml");
        File source = changelog.getParentFile();

        ValidateMojo mojo = getMojo(source);
        mojo.setIncludeChanges(new String[] { "createTable", "addForeignKeyConstraint" });
        mojo.setExcludeChanges(new String[] { "addPrimaryKey", "addForeignKeyConstraint" });

        thrown.expect(MojoFailureException.class);
        thrown.expectMessage("Some changes are included in both includeChanges/excludeChanges");

        mojo.execute();
    }

    @Test
    public void verify_includeChanges_works() throws Exception {
        File changelog = getResource("changelog_filter_on_changes.xml");
        File source = changelog.getParentFile();

        ValidateMojo mojo = getMojo(source);
        mojo.setIncludeChanges(new String[] { "createSequence", "createTable" });

        mojo.execute();
    }

    @Test
    public void verify_excludeChanges_works() throws Exception {
        SystemStreamLog log = mock(SystemStreamLog.class);

        File changelog = getResource("changelog_filter_on_changes.xml");
        File source = changelog.getParentFile();

        ValidateMojo mojo = spy(getMojo(source));
        mojo.setExcludeChanges(new String[] { "createTable" });
        doReturn(log).when(mojo).getLog();

        mojo.execute();

        ArgumentCaptor<String> arguments = ArgumentCaptor.forClass(String.class);
        verify(log, atLeastOnce()).warn(arguments.capture());
        List<String> capturedViolations = arguments.getAllValues();
        Assert.assertThat(capturedViolations, CoreMatchers.hasItems(CoreMatchers.endsWith("The change createTable is not allowed by the exclusion filter")));
    }

    @Test
    public void verify_that_changeset_with_violations_are_logged() throws Exception {
        SystemStreamLog log = mock(SystemStreamLog.class);

        File changelog = getResource("changelog_with_issues.xml");
        File source = changelog.getParentFile();

        ValidateMojo mojo = spy(getMojo(source));
        doReturn(log).when(mojo).getLog();

        mojo.execute();

        ArgumentCaptor<String> arguments = ArgumentCaptor.forClass(String.class);
        verify(log, atLeast(5)).warn(arguments.capture());
        List<String> capturedViolations = arguments.getAllValues();
        Assert.assertThat(capturedViolations.get(0), CoreMatchers.containsString("There are violations on changelog "));
        Assert.assertThat(capturedViolations.get(1), CoreMatchers.containsString("ChangeSet 1"));
    }

    @Test
    public void verify_invalid_sql99_standard_datatype() throws Exception {
        SystemStreamLog log = mock(SystemStreamLog.class);

        File changelog = getResource("changelog_datatype.xml");
        File source = changelog.getParentFile();

        ValidateMojo mojo = spy(getMojo(source));
        mojo.setFailOnError(false);
        doReturn(log).when(mojo).getLog();

        mojo.execute();

        ArgumentCaptor<String> arguments = ArgumentCaptor.forClass(String.class);
        verify(log, atLeast(3)).warn(arguments.capture());
        List<String> capturedViolations = arguments.getAllValues();
        Assert.assertThat(capturedViolations, CoreMatchers.hasItems(CoreMatchers.containsString("isn't one of SQL-99 standard types")));
    }

    @Test
    public void verify_valid_sql99_standard_parametric_datatype() throws Exception {
        SystemStreamLog log = mock(SystemStreamLog.class);

        File changelog = getResource("changelog_parametric_datatype.xml");
        File source = changelog.getParentFile();

        ValidateMojo mojo = spy(getMojo(source));
        mojo.setFailOnError(false);
        doReturn(log).when(mojo).getLog();

        mojo.execute();

        ArgumentCaptor<String> arguments = ArgumentCaptor.forClass(String.class);
        verify(log, never()).warn(arguments.capture());
    }

    @Test
    public void verify_ignores() throws Exception {
        SystemStreamLog log = mock(SystemStreamLog.class);

        File changelog = getResource("changelog_with_issues2.xml");
        File source = changelog.getParentFile();

        ValidateMojo mojo = spy(getMojo(source));
        mojo.setFailOnError(false);
        doReturn(log).when(mojo).getLog();
        mojo.setIgnoreRules(Arrays.asList("issueId"));

        mojo.execute();

        ArgumentCaptor<String> arguments = ArgumentCaptor.forClass(String.class);
        verify(log, atLeast(1)).warn(arguments.capture());

        List<String> violationsMessage = arguments.getAllValues();
        Assert.assertThat(violationsMessage, CoreMatchers.not("ChangeSet 123, has a violation: The changeset id does not end with progressive number after the issue id"));
        Assert.assertThat(violationsMessage, CoreMatchers.not("ChangeSet XYZ, has a violation: The changeset id does not end with progressive number after the issue id"));
    }

    @Test
    public void verify_logicalfilepath_with_suffix() throws Exception {
        SystemStreamLog log = mock(SystemStreamLog.class);
        URL res = getClass().getResource("logicalfilepath_with_suffix.xml");
        String file = res.getFile();
        String source = file.substring(0, file.indexOf("liquibase/plugin") - 1);

        ValidateMojo mojo = getMojo(new File(source));
        mojo.setFailOnError(false);
        mojo.setLog(log);
        Map<String, String> configMap = new HashMap<>();
        configMap.put("filePath.suffix", "suff");
        mojo.setConfigMap(configMap);
        mojo.setUseArtifactId(false);
        mojo.setIncludes(new String[] { "**/logicalfilepath_with_suffix.xml"});

        mojo.execute();

        ArgumentCaptor<String> arguments = ArgumentCaptor.forClass(String.class);
        verify(log, never()).warn(arguments.capture());
    }

    private ValidateMojo getMojo(File source) {
        ValidateMojo mojo = new ValidateMojo();
        mojo.setExtension(Extension.xml);
        mojo.setSource(source.getAbsoluteFile());
        mojo.setProject(getMavenProject("com.acme.core"));
        mojo.setIssuePattern(".*");
        mojo.setUseArtifactId(true);
        return mojo;
    }

    private MavenProject getMavenProject(String artifactId) {
        MavenProject project = mock(MavenProject.class);
        when(project.getArtifactId()).thenReturn(artifactId);
        return project;
    }

    private File getResource(String resource) throws IOException {
        File file = folder.newFile(new File(resource).getName());
        IOUtil.copy(getClass().getResourceAsStream(resource), new FileOutputStream(file));
        return file;
    }

}
