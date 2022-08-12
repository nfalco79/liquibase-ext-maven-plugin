/*
 * Copyright 2022 Falco Nikolas
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

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

import com.github.nfalco79.maven.liquibase.plugin.validator.ChangeSetIssueIdValidator;
import com.github.nfalco79.maven.liquibase.plugin.validator.FilePathValidator;
import com.github.nfalco79.maven.liquibase.plugin.validator.IChangeSetValidator;
import com.github.nfalco79.maven.liquibase.plugin.validator.IChangeValidator;
import com.github.nfalco79.maven.liquibase.plugin.validator.ValidationError;
import com.github.nfalco79.maven.liquibase.plugin.validator.Validator;
import com.github.nfalco79.maven.liquibase.plugin.validator.ValidatorFactory;
import com.google.common.collect.Sets;

import liquibase.change.Change;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.LiquibaseException;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

@Mojo(name = "validate", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class ValidateMojo extends AbstractMojo {

    /**
     * Define the list of available script file extension.
     */
    public enum Extension {
        xml, yml, sql, json
    }

    public static final String ISSUE_PATTERN = "[A-Z][A-Z0-9]{3,}-\\d+|\\d+{1,6}"; // JIRA + Redmine

    /**
     * This this mojo execution.
     **/
    @Parameter(property = "ext.liquibase.skip", defaultValue = "false")
    private boolean skip;

    @Parameter(defaultValue = "true")
    private boolean failOnError;

    @Parameter(defaultValue = "xml")
    private Extension extension;

    @Parameter(defaultValue = "true")
    private boolean useArtifactId;

    @Parameter
    private String[] includes;

    @Parameter
    private String[] excludes;

    @Parameter
    private String[] includeChanges;

    @Parameter
    private String[] excludeChanges;

    @Parameter
    private List<String> ignoreRules = new LinkedList<>();

    @Parameter
    private List<String> skipChangeSets = new LinkedList<>();

    @Parameter(required = true)
    private File source;

    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter(defaultValue = ISSUE_PATTERN)
    private String issuePattern;

    @Parameter(defaultValue = "FINE")
    private String logLevel = "FINE";

    /**
     * Mojo configuration Map.
     */
    @Parameter
    private Map<String, String> configMap = new HashMap<>();

    private ValidatorFactory validationFactory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (isSkip()) {
            getLog().info("Skip liquibase script validation");
            return;
        }

        if (!source.isDirectory()) {
            getLog().debug("The directory " + source.getAbsolutePath() + " does not exists, skipping validation");
            return;
        }

        Set<String> ignores = Sets.newHashSet(getIgnoreRules());
        // check that includeChanges and excludeChanges does not overlaps
        Set<String> include = Sets.newHashSet(getIncludeChanges());
        Set<String> exclude = Sets.newHashSet(getExcludeChanges());
        if (!Sets.intersection(include, exclude).isEmpty()) {
            throw new MojoFailureException("Some changes are included in both includeChanges/excludeChanges");
        }

        validationFactory = new ValidatorFactory();
        validationFactory.setIncludeChanges(include);
        validationFactory.setExcludeChanges(exclude);
        validationFactory.setIgnoreRules(ignores);

        // add extra configured chageset validator, all these validator are like singleton per factory instance
        validationFactory.addValidator(new ChangeSetIssueIdValidator(issuePattern));
        if (useArtifactId) {
            validationFactory.addValidator((IChangeSetValidator) new FilePathValidator(project.getArtifactId()));
        } else {
            validationFactory.addValidator((IChangeSetValidator) new FilePathValidator(source));
        }

        // setup loggers
//        LogService.setLoggerFactory(new MavenLogFactory(this.getLog(), Level.parse(logLevel)));

        Collection<ValidationError> issues = newIssueContainer();
        Map<String, ChangeLogParser> parsers = new HashMap<>(Extension.values().length);
        try {
            for (String changeLog : getChangeLogs()) {
                String ext = FileUtils.getExtension(changeLog);

                File changeLogFile = new File(source, changeLog); // NOSONAR
                if (!changeLogFile.isFile()) {
                    continue;
                }
                ResourceAccessor resourceAccessor = new FileSystemResourceAccessor(changeLogFile.getParentFile());

                ChangeLogParser parser = parsers.get(ext);
                if (parser == null) {
                    parser = ChangeLogParserFactory.getInstance().getParser(ext, resourceAccessor);
                    parsers.put(ext, parser);
                }

                DatabaseChangeLog dbChangeLog = parser.parse(changeLogFile.getAbsolutePath(), new ChangeLogParameters(), resourceAccessor);
                for (ChangeSet cs : dbChangeLog.getChangeSets()) {
                    if (skipChangeSet(cs)) {
                        getLog().info("Skip changeset " + cs.getId() + " per configuration");
                        continue;
                    }

                    IChangeSetValidator csValidator = newChangeSetValidator(cs);
                    csValidator.configure(configMap);
                    issues.addAll(csValidator.validate(cs));

                    for (Change change : cs.getChanges()) {
                        IChangeValidator validator = newValidator(change);
                        validator.configure(configMap);
                        issues.addAll(validator.validate(change));
                    }
                }

                if (issues.isEmpty()) {
                    getLog().info("No violations found on " + changeLog);
                }
            }
        } catch (LiquibaseException e) {
            throw new MojoExecutionException("Unexpected excetion", e);
        }

        if (!issues.isEmpty()) {
            // gather all violation by file name
            Map<String, List<ValidationError>> issuesMap = buildIssuesByFile(issues);
            for (Entry<String, List<ValidationError>> entry : issuesMap.entrySet()) {
                printViolation("There are violations on changelog " + entry.getKey());
                // print all violations of a file
                for (ValidationError issue : entry.getValue()) {
                    printIssue(issue);
                }

            }
            if (failOnError) {
                throw new MojoFailureException("There are violations on changelogs");
            }
        }
    }

    /*
     * for test purpose.
     */
    /*package*/ List<ValidationError> newIssueContainer() {
        return new LinkedList<>();
    }

    private boolean skipChangeSet(ChangeSet cs) {
        if (getSkipChangeSets().contains(cs.getId())) {
            if (cs.getPreconditions() != null || (cs.getDbmsSet() != null && !cs.getDbmsSet().isEmpty())) {
                return true;
            } else {
                getLog().warn("Changeset " + cs.getId() + " could not been skipped because it is not DB specific");
            }
        }
        return false;
    }

    private void printIssue(ValidationError issue) {
        if (issue.getChangeSetId() != null) {
            if (issue.getElement() != null) {
                printViolation(MessageFormat.format("ChangeSet {0}, element {1} has a violation: {2}", issue.getChangeSetId(), issue.getElement(), issue.getMessage()));
            } else {
                printViolation(MessageFormat.format("ChangeSet {0}, has a violation: {1}", issue.getChangeSetId(), issue.getMessage()));
            }
        } else {
            printViolation(MessageFormat.format("ChangeLog {0} has a violation: {1}", issue.getFile(), issue.getMessage()));
        }
    }

    protected IChangeSetValidator newChangeSetValidator(ChangeSet changeSet) {
        return validationFactory.newChangeSetValidator(changeSet);
    }

    protected IChangeValidator newValidator(Change change) {
        return validationFactory.newValidator(change);
    }

    protected String[] getChangeLogs() {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(source);
        scanner.setIncludes(getIncludes());
        scanner.setExcludes(excludes);
        scanner.scan();
        String[] changeLogs = scanner.getIncludedFiles();
        getLog().debug("There are " + changeLogs.length + " changelogs to validate");
        return changeLogs;
    }

    private void printViolation(String message) {
        if (failOnError) {
            getLog().error(message);
        } else {
            getLog().warn(message);
        }
    }

    private Map<String, List<ValidationError>> buildIssuesByFile(Collection<ValidationError> issues) {
        Map<String, List<ValidationError>> issuesMap = new HashMap<>();
        for (ValidationError issue : issues) {
            String key = issue.getFile();
            if (issuesMap.get(key) == null) {
                List<ValidationError> issuesList = new LinkedList<>();
                issuesList.add(issue);
                issuesMap.put(key, issuesList);
            } else {
                issuesMap.get(key).add(issue);
            }
        }
        return issuesMap;
    }

    protected boolean skip(Class<?> clazz) {
        Validator annotation = clazz.getDeclaredAnnotation(Validator.class);
        String ruleName = annotation != null ? annotation.name() : null;
        return ruleName != null && getIgnoreRules().contains(ruleName);
    }

    private String[] getIncludes() {
        if (includes != null && includes.length > 0) {
            return includes; // NOSONAR
        }
        return new String[] { "**/*." + extension };
    }

    public void setIncludes(String[] includes) {
        this.includes = includes;
    }

    public String[] getExcludes() {
        return excludes;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public Extension getExtension() {
        return extension;
    }

    public void setExtension(Extension extension) {
        this.extension = extension;
    }

    public boolean isUseArtifactId() {
        return useArtifactId;
    }

    public void setUseArtifactId(boolean useArtifactId) {
        this.useArtifactId = useArtifactId;
    }

    public File getSource() {
        return source;
    }

    public void setSource(File source) {
        this.source = source;
    }

    public String getIssuePattern() {
        return issuePattern;
    }

    public void setIssuePattern(String issuePattern) {
        this.issuePattern = issuePattern;
    }

    public Map<String, String> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, String> configMap) {
        this.configMap = configMap;
    }

    public MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public String[] getIncludeChanges() {
        return includeChanges == null ? new String[0] : includeChanges;
    }

    public void setIncludeChanges(String[] includeChanges) {
        this.includeChanges = (includeChanges == null ? new String[0] : includeChanges);
    }

    public String[] getExcludeChanges() {
        return excludeChanges == null ? new String[0] : excludeChanges;
    }

    public void setExcludeChanges(String[] excludeChanges) {
        this.excludeChanges = (excludeChanges == null ? new String[0] : excludeChanges);
    }

    public List<String> getSkipChangeSets() {
        return (skipChangeSets == null ? new ArrayList<String>() : skipChangeSets);
    }

    public void setSkipChangeSets(List<String> skipChangeSets) {
        this.skipChangeSets = (skipChangeSets == null ? new ArrayList<String>() : skipChangeSets);
    }

    public List<String> getIgnoreRules() {
        return ignoreRules; // NOSONAR
    }

    public void setIgnoreRules(List<String> ignoreRules) {
        this.ignoreRules = (ignoreRules == null ? new ArrayList<String>() : ignoreRules);
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

//    public LogLevel getLogLevel() {
//        return logLevel;
//    }
//
//    public void setLogLevel(LogLevel logLevel) {
//        this.logLevel = logLevel;
//    }

}