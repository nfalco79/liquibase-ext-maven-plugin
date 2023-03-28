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
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.artifact.filter.resolve.AndFilter;
import org.apache.maven.shared.artifact.filter.resolve.PatternInclusionsFilter;
import org.apache.maven.shared.artifact.filter.resolve.ScopeFilter;
import org.apache.maven.shared.artifact.filter.resolve.TransformableFilter;
import org.apache.maven.shared.artifact.filter.resolve.transform.EclipseAetherFilterTransformer;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;

import com.github.nfalco79.maven.dependency.graph.BottomUpDependencyVisitor;
import com.github.nfalco79.maven.dependency.graph.DependencyGraphSession;
import com.github.nfalco79.maven.liquibase.plugin.log.MavenLogService;
import com.github.nfalco79.maven.liquibase.plugin.log.MavenUIService;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.integration.commandline.CommandLineUtils;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.DirectoryResourceAccessor;
import liquibase.resource.ResourceAccessor;

/**
 * Execute the liquibase update command.
 * <p>
 * This mojo is able to gather a set of resource that matches a configured
 * patterns from the resources, test resources and dependencies.
 *
 * @author Nikolas Falco
 */
@Mojo(name = "updateSQL", requiresDependencyResolution = ResolutionScope.TEST, requiresProject = true)
public class UpdateSQLMojo extends MergeChangeLogsMojo {

    @Parameter(defaultValue = "INFO")
    private String logLevel = Level.INFO.getName();

    /**
     * Limits liquibase ChangeSet logger message, if set to {@code true} any SQL
     * or other kind of message are logged as debug. Other warning due to
     * missing optional dependencies are moved to debug.
     */
    @Parameter(defaultValue = "true")
    private boolean limitLog = true;

    @Parameter(name = "database", required = true)
    private DatabaseConfiguration dbConfguration;

    /**
     * Limit to a specific range the previous version to use for backward
     * compatibility verification.
     */
    @Parameter(property = "ext.liquibase.backwardCompatibility.version", defaultValue = "[0, ${project.version})")
    private String backwardCompatibilityVersion = "(, ${project.version})";

    /**
     * Skip tests if new scripts are backward compatible with ones released in
     * the previous release.
     */
    @Parameter(property = "ext.liquibase.backwardCompatibility.skip", defaultValue = "true")
    private boolean skipBackwardCompatibility = true;

    /**
     * Project types which this plugin supports.
     */
    @Parameter
    private List<String> supportedProjectTypes = Arrays.asList("jar", "bundle", "war", "ear");

    @SuppressWarnings("deprecation")
    @Component
    protected org.apache.maven.artifact.metadata.ArtifactMetadataSource artifactMetadataSource; // NOSONAR

    @Component
    protected ArtifactHandlerManager artifactHandlerManager;

    @Inject
    protected RepositorySystem repositorySystem;

    public DatabaseConfiguration getDatabase() {
        return dbConfguration;
    }

    public void setDatabase(DatabaseConfiguration dbConfguration) {
        this.dbConfguration = dbConfguration;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        Level.parse(logLevel);
        this.logLevel = logLevel;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (isSkip()) {
            getLog().info("Skip liquibase update SQL per configuration");
            return;
        }

        String projectType = getProject().getPackaging();
        // ignore unsupported project types, useful when plugin is configured in parent pom
        if (!supportedProjectTypes.contains(projectType)) {
            getLog().debug("Ignoring project type " + projectType + " - supportedProjectTypes = " + supportedProjectTypes);
            return;
        }

        if (!isSkipBackwardCompatibility()) {
            try {
                Artifact previous = getLatestReleaseArtifact();
                if (previous != null) {
                    // resolve transitive dependencies of previous version
                    List<Artifact> resolvedArtifacts = buildDependencyOrder(previous);

                    if (!resolvedArtifacts.isEmpty()) {
                        // gather scripts from previous dependencies
                        File outputDirectory = new File(getOutputDirectory() + "-previous"); // NOSONAR
                        Collection<File> extractScripts = extractScripts(resolvedArtifacts, outputDirectory);
                        if (extractScripts.isEmpty()) {
                            getLog().debug("No scripts in previous version " + previous + " have been found");
                        } else {
                            File liquibaseScript = new File(outputDirectory, getOutputChangelog()); // NOSONAR
                            generateMasterScript(liquibaseScript, extractScripts);
                            runScript(liquibaseScript, Level.WARNING);
                            getLog().info("Database for version " + previous + " created");
                        }
                    }
                }
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }

        try {
            Collection<File> scripts = gatherChangeLogs();
            if (scripts.isEmpty()) {
                getLog().info("Skip liquibase because no script found");
            } else {
                File liquibaseScript = new File(getOutputDirectory(), getOutputChangelog());
                generateMasterScript(liquibaseScript, scripts);
                runScript(liquibaseScript, Level.parse(logLevel));
            }
        } catch (DependencyGraphBuilderException e) {
            throw new MojoExecutionException("Error resolving dependency tree", e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("deprecation")
    private Artifact getLatestReleaseArtifact() throws MojoExecutionException, MojoFailureException {
        VersionRange range;
        try {
            range = VersionRange.createFromVersionSpec(backwardCompatibilityVersion);
        } catch (InvalidVersionSpecificationException e2) {
            throw new MojoFailureException("Wrong backward compatibility range " + backwardCompatibilityVersion, e2);
        }

        String type = project.getPackaging();
        // force retrieve of available versions between released ones
        String version = project.getVersion().replace("-SNAPSHOT", "");
        Artifact artifact = new DefaultArtifact(project.getGroupId(), project.getArtifactId(), version, (String) null, type, (String) null, artifactHandlerManager.getArtifactHandler(type));

        ProjectBuildingRequest buildingRequest = session.getProjectBuildingRequest();
        ArtifactRepository localRepository = buildingRequest.getLocalRepository();
        List<ArtifactRepository> remoteRepositories = buildingRequest.getRemoteRepositories();
        try {
            List<ArtifactVersion> availableVersions = artifactMetadataSource.retrieveAvailableVersions(artifact, localRepository, remoteRepositories);

            // takes closer previous release
            ArtifactVersion previous = availableVersions.stream() //
                    .filter(v -> range.containsVersion(v) && !ArtifactUtils.isSnapshot(v.toString())) //
                    .sorted((v1, v2) -> v2.compareTo(v1)) // descending order
                    .findFirst().orElse(null);

            if (previous != null) {
                artifact.setVersion(previous.toString());
                return artifact;
            }
        } catch (org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException e) { // NOSONAR
            throw new MojoExecutionException("Failure retrieveing available versions for " + artifact.toString(), e);
        }

        return null;
    }

    protected List<Artifact> buildDependencyOrder(Artifact artifact) throws MojoExecutionException {
        // create a filtered list of artifacts a dependency order so that the DLL are executed in a sequence that respect dependencies tree
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
        buildingRequest.setProject(project);
        buildingRequest.setRepositorySession(new DependencyGraphSession(session.getRepositorySession()));

        try {
            TransformableFilter filter = new AndFilter(Arrays.asList( //
                    ScopeFilter.excluding("system", "provided"), //
                    new PatternInclusionsFilter(Arrays.asList("*:*:jar:*"))));

            Dependency aetherRoot = RepositoryUtils.toDependency(artifact, Collections.emptyList());
            CollectRequest request = new CollectRequest(aetherRoot, RepositoryUtils.toRepos(buildingRequest.getRemoteRepositories()));
            DependencyFilter depFilter = filter.transform(new EclipseAetherFilterTransformer());
            DependencyRequest depRequest = new DependencyRequest(request, depFilter);
            DependencyResult result = repositorySystem.resolveDependencies(buildingRequest.getRepositorySession(), depRequest);

            DependencyNode rootNode = result.getRoot();
            BottomUpDependencyVisitor visitor = new BottomUpDependencyVisitor();
            rootNode.accept(visitor);

            return new ArrayList<>(visitor.getNodes());
        } catch (DependencyResolutionException e) {
            throw new MojoExecutionException("Failure resolving transitive dependencies of previous version " + artifact.toString(), e);
        }
    }

    private void runScript(File liquibaseScript, Level level) throws MojoExecutionException {
        // setup loggers
        initLogService(level);

        // discover database based on the user settings
        try (Database database = CommandLineUtils.createDatabaseObject(new ClassLoaderResourceAccessor(getClass().getClassLoader()), //
                    dbConfguration.getUrl(), //
                    dbConfguration.getUsername(), //
                    dbConfguration.getPassword(), //
                    dbConfguration.getDriver(), //
                    dbConfguration.getDefaultCatalogName(), //
                    dbConfguration.getDefaultSchemaName(), //
                    false, false, //
                    null, null, null, null, null, null, null)) {

            // liquibase update run
            File root = liquibaseScript.getParentFile();
            while (root.getParentFile() != null) {
                root = root.getParentFile();
            }
            File rootFolder = liquibaseScript.getParentFile().getAbsoluteFile();
            Liquibase liquibase = new Liquibase(liquibaseScript.getName(), buildResourceAccessor(rootFolder, root), database);
            liquibase.update(new Contexts());
        } catch (IOException | LiquibaseException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private ResourceAccessor buildResourceAccessor(File ... roots) throws IOException {
        CompositeResourceAccessor resourceAccessor = new CompositeResourceAccessor();
        for (File root : roots) {
            resourceAccessor.addResourceAccessor(new DirectoryResourceAccessor(root));
            resourceAccessor.addResourceAccessor(new ClassLoaderResourceAccessor(new URLClassLoader(new URL[] { root.toURI().toURL() })));
        }

        return resourceAccessor;
    }

    private void initLogService(Level level) throws MojoExecutionException {
        MavenLogService logService = new MavenLogService(this.getLog(), level);
        logService.setLimitLog(limitLog);
        Map<String, Object> map = new HashMap<>();
        map.put("logService", logService);
        map.put("ui", new MavenUIService());
        try {
            Scope.enter(map);
        } catch (Exception e) {
            throw new MojoExecutionException("Log initialisation failure", e);
        }
    }

    public boolean isLimitLog() {
        return limitLog;
    }

    public void setLimitLog(boolean limitLog) {
        this.limitLog = limitLog;
    }

    public boolean isSkipBackwardCompatibility() {
        return skipBackwardCompatibility;
    }

    public void setSkipBackwardCompatibility(boolean skipBackwardCompatibility) {
        this.skipBackwardCompatibility = skipBackwardCompatibility;
    }

    public String getBackwardCompatibilityVersion() {
        return backwardCompatibilityVersion;
    }

    public void setBackwardCompatibilityVersion(String backwardCompatibilityVersion) {
        this.backwardCompatibilityVersion = backwardCompatibilityVersion;
    }

    public List<String> getSupportedProjectTypes() {
        return supportedProjectTypes;
    }

    public void setSupportedProjectTypes(List<String> supportedProjectTypes) {
        this.supportedProjectTypes = CollectionUtils.isEmpty(supportedProjectTypes) ? Collections.emptyList() : supportedProjectTypes;
    }

}
