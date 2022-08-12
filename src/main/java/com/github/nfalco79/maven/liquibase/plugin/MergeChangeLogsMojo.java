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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.ScopeArtifactFilter;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.utils.io.DirectoryScanner;
import org.apache.maven.shared.utils.io.MatchPatterns;
import org.apache.maven.shared.utils.xml.PrettyPrintXMLWriter;
import org.apache.maven.shared.utils.xml.Xpp3Dom;
import org.apache.maven.shared.utils.xml.Xpp3DomWriter;

import com.github.nfalco79.maven.artifact.resolver.filter.TypeFiler;
import com.github.nfalco79.maven.dependency.DependencyResolver;
import com.github.nfalco79.maven.dependency.graph.BottomUpDependencyVisitor;

/**
 * Creates a liquibase master changelog that includes a changelog sequence that
 * respects the dependency order.
 * <p>
 * This mojo is able to gather a set of resource that matches a configured
 * patterns from the resources, test resources and dependencies. It creates a
 * master changelog file that includes all gathered resources following the
 * dependency tree order.
 *
 * @author Nikolas Falco
 */
@Mojo(name = "merge-changelogs", requiresDependencyResolution = ResolutionScope.TEST)
public class MergeChangeLogsMojo extends AbstractMojo {
    /**
     * XSD used in the master changelog to validate the grammar.
     */
    private static final String LIQUIBASE_XSD = "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.15.xsd";

    /**
     * The directory containing the extracted liquibase file from the classpath.
     */
    @Parameter(defaultValue = "${project.build.directory}/ext-liquibase")
    private File outputDirectory;

    @Parameter(defaultValue = "db.changelog-master.xml")
    private String outputChangelog = "db.changelog-master.xml";

    /**
     * The Maven session
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

    @Parameter(property = "ext.liquibase.skip", defaultValue = "false")
    private boolean skip = false;

    @Parameter(defaultValue = "true")
    private boolean includeResources = true;

    @Parameter(defaultValue = "true")
    private boolean includeTestResources = true;

    @Parameter(readonly = true, defaultValue = "${project}")
    protected MavenProject project;

    @Parameter
    private String[] includes;

    @Parameter
    private String[] excludes;

    @Parameter
    private String[] resources;

    @Parameter
    private String[] changeLogs;

    @Parameter(property = "ext.liquibase.scope", defaultValue = Artifact.SCOPE_RUNTIME)
    private String includeScope;

    @Parameter(defaultValue = "false")
    private boolean relativeToChangelogFile = false;

    /**
     * Max number of attempts to resolve the dependency tree.
     */
    @Parameter(defaultValue = "3")
    private int maxAttemptsToResolveDependencies = 3;

    /**
     * The dependency tree builder to use.
     */
    @Component
    protected DependencyGraphBuilder dependencyGraphBuilder;

    public MavenProject getProject() {
        return project;
    }

    protected String[] getDefaultIncludes() {
        return replaceSeparator("**/db.changelog*.xml", "**/data.changelog*.xml", "**/*.sql");
    }

    private String[] replaceSeparator(String...path) {
        return Stream.of(path) //
                .map(p -> p.replace('/', File.separatorChar)) //
                .toArray(String[]::new);
    }

    public String[] getIncludes() {
        if (includes == null) {
            includes = getDefaultIncludes();
        }
        return includes; // NOSONAR
    }

    public void setIncludes(String[] includes) {
        this.includes = replaceSeparator(includes);
    }

    public String[] getExcludes() {
        if (excludes == null) {
            excludes = new String[0];
        }
        return excludes; // NOSONAR
    }

    public void setExcludes(String[] excludes) {
        this.excludes = replaceSeparator(excludes);
    }

    public String[] getResources() {
        if (resources == null) {
            resources = getDefaultIncludes();
        }
        return resources; // NOSONAR
    }

    public void setResources(String[] resources) {
        this.resources = replaceSeparator(resources);
    }

    protected String[] getDefaultChangeLogsMaster() {
        return replaceSeparator("**/db.changelog-master.xml", "**/data.changelog-master.xml");
    }

    public String[] getChangeLogs() {
        return changeLogs == null ? getDefaultChangeLogsMaster() : changeLogs;
    }

    public void setChangeLogs(String[] includeMasters) {
        this.changeLogs = replaceSeparator(includeMasters);
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (isSkip()) {
            getLog().info("Skip liquibase update SQL per configuration");
            return;
        }

        try {
            Collection<File> scripts = gatherChangeLogs();
            if (scripts.isEmpty()) {
                getLog().info("Skip liquibase because no script found");
            } else {
                File liquibaseScript = new File(outputDirectory, outputChangelog);
                generateMasterScript(liquibaseScript, scripts);
            }
        } catch (DependencyGraphBuilderException e) {
            throw new MojoExecutionException("Error resolving dependency tree", e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected Collection<File> gatherChangeLogs() throws IOException, DependencyGraphBuilderException {
        Collection<File> scripts = processDependencies();
        if (includeResources) {
            scripts.addAll(processResources(getProject().getResources()));
        }
        if (includeTestResources) {
            scripts.addAll(processResources(getProject().getTestResources()));
        }

        return scripts;
    }

    protected void generateMasterScript(File liquibaseScript, Collection<File> changelogs) throws IOException {
        Xpp3Dom root = new Xpp3Dom("databaseChangeLog");
        root.setAttribute("xmlns", "http://www.liquibase.org/xml/ns/dbchangelog");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xmlns:ext", "http://www.liquibase.org/xml/ns/dbchangelog-ext");
        root.setAttribute("xsi:schemaLocation", "http://www.liquibase.org/xml/ns/dbchangelog"
                + " " + LIQUIBASE_XSD
                + " http://www.liquibase.org/xml/ns/dbchangelog-ext"
                + " http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd");

        Path workDir = liquibaseScript.getParentFile().toPath();

        MatchPatterns masterChangeLogs = MatchPatterns.from(getChangeLogs());
        for (File changelog : changelogs) {
            if (masterChangeLogs.matches(changelog.getCanonicalPath(), false)) {
                Xpp3Dom include = new Xpp3Dom("include");
                if (relativeToChangelogFile) {
                    include.setAttribute("relativeToChangelogFile", "true");
                    include.setAttribute("file", workDir.relativize(changelog.toPath()).toString().replace('\\', '/'));
                } else {
                    include.setAttribute("file", changelog.getCanonicalPath());
                }
                root.addChild(include);
            }
        }

        try (FileWriter writer = new FileWriter(liquibaseScript)) {
            Xpp3DomWriter.write(new PrettyPrintXMLWriter(writer, "    "), root);
        }
    }

    private List<File> processResources(List<Resource> resources) throws IOException {
        List<File> scripts = new LinkedList<>();

        DirectoryScanner ds = new DirectoryScanner();
        ds.setIncludes(ArrayUtils.addAll(getResources(), getChangeLogs()));
        for (Resource resource : resources) {
            File baseDir = new File(resource.getDirectory());
            if (!baseDir.exists()) {
                continue;
            }

            ds.setBasedir(baseDir);
            ds.scan();
            for (String file : ds.getIncludedFiles()) {
                File script = new File(outputDirectory, file); // NOSONAR
                FileUtils.copyFile(new File(ds.getBasedir(), file), script); // NOSONAR
                scripts.add(script);
            }
        }

        return scripts;
    }

    private Collection<File> processDependencies() throws IOException, DependencyGraphBuilderException {
        return extractScripts(buildDependencyOrder(), getOutputDirectory());
    }

    private Collection<Artifact> buildDependencyOrder() throws DependencyGraphBuilderException {
        ArtifactFilter filter = new AndArtifactFilter(Arrays.asList(new ScopeArtifactFilter(includeScope) //
                .setIncludeSystemScope(false) //
                .setIncludeProvidedScope(false), //
                new TypeFiler("jar")));
        DependencyResolver resolver = new DependencyResolver(session, project, dependencyGraphBuilder, filter, getLog());
        DependencyNode rootNode = resolver.resolveDependencies(getMaxAttemptsToResolveDependencies());

        BottomUpDependencyVisitor visitor = new BottomUpDependencyVisitor();
        rootNode.accept(visitor);

        return visitor.getNodes().stream() //
                .filter(a -> a != rootNode.getArtifact()) // remove project artifact
                .collect(Collectors.toList());
    }

    protected Collection<File> extractScripts(Collection<Artifact> artifacts, File outputDirectory) throws IOException {
        Set<File> scripts = new LinkedHashSet<>();
        MatchPatterns includesPatterns = MatchPatterns.from(ArrayUtils.addAll(getIncludes(), getChangeLogs()));
        MatchPatterns excludesPatterns = MatchPatterns.from(getExcludes());
        Log log = getLog();

        for (Iterator<Artifact> iterator = artifacts.iterator(); iterator.hasNext();) { // NOSONAR
            Artifact artifact = iterator.next();
            File artifactFile = artifact.getFile();
            if (artifactFile == null) {
                // skip project itself or artifact without a physical file
                log.warn("Skip artifact without a physical file " + artifact.toString());
                continue;
            }

            log.debug("Processing artifact " + artifact.toString());
            if (artifactFile.isFile()) {
                scripts.addAll(scanArchive(outputDirectory, includesPatterns, excludesPatterns, artifact));
            } else {
                scripts.addAll(scanFolder(outputDirectory, artifact));
            }
        }

        return scripts;
    }

    private Collection<File> scanFolder(File outputDirectory, Artifact artifact) throws IOException, ZipException {
        Set<File> scripts = new LinkedHashSet<>();
        Log log = getLog();
        File baseDir = artifact.getFile();

        DirectoryScanner ds = new DirectoryScanner();
        ds.setIncludes(ArrayUtils.addAll(getIncludes(), getChangeLogs()));
        ds.setExcludes(getExcludes());
        ds.setBasedir(baseDir);
        ds.scan();
        for (String file : ds.getIncludedFiles()) {
            File script = new File(outputDirectory, file); // NOSONAR
            if (log.isDebugEnabled()) {
                log.debug("Copy " + file + " from " + artifact.toString() + " to " + script.getCanonicalPath());
            }
            FileUtils.copyFile(new File(baseDir, file), script);
            scripts.add(script);
        }
        return scripts;
    }

    private List<File> scanArchive(File outputDirectory,
                             MatchPatterns includesPatterns,
                             MatchPatterns excludesPatterns,
                             Artifact artifact) throws IOException, ZipException {
        List<File> scripts = new LinkedList<>();
        Log log = getLog();

        try (ZipFile zf = new ZipFile(artifact.getFile())) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String entryName = zipEntry.getName();
                if (!zipEntry.isDirectory()) {
                    entryName = entryName.replace('/', File.separatorChar);
                    if (includesPatterns.matches(entryName, true) && !excludesPatterns.matches(entryName, true)) {
                        try (InputStream zeis = zf.getInputStream(zipEntry)) {
                            File script = new File(outputDirectory, entryName); // NOSONAR
                            if (log.isDebugEnabled()) {
                                log.debug("Extracting " + entryName + " from " + artifact.toString() + " to " + script.getCanonicalPath());
                            }
                            FileUtils.copyInputStreamToFile(zeis, script);
                            scripts.add(script);
                        }
                    }
                }
            }
        }
        return scripts;
    }

    public boolean isIncludeResources() {
        return includeResources;
    }

    public void setIncludeResources(boolean includeResources) {
        this.includeResources = includeResources;
    }

    public boolean isIncludeTestResources() {
        return includeTestResources;
    }

    public void setIncludeTestResources(boolean includeTestResources) {
        this.includeTestResources = includeTestResources;
    }

    public String getOutputChangelog() {
        return outputChangelog;
    }

    public void setOutputChangelog(String outputChangelog) {
        this.outputChangelog = outputChangelog;
    }

    public boolean isSkip() {
        return skip;
    }

    public boolean isRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    public void setRelativeToChangelogFile(boolean relativeToChangelogFile) {
        this.relativeToChangelogFile = relativeToChangelogFile;
    }

    /**
     * Return the max number of attempts to resolve the dependency tree.
     * 
     * @return the max number of attempts
     */
    public int getMaxAttemptsToResolveDependencies() {
        return maxAttemptsToResolveDependencies;
    }

    /**
     * Set the max number of attempts to resolve the dependency tree.
     * 
     * @param maxAttemptsToResolveDependencies the max number of attempts
     */
    public void setMaxAttemptsToResolveDependencies(int maxAttemptsToResolveDependencies) {
        this.maxAttemptsToResolveDependencies = maxAttemptsToResolveDependencies;
    }

}
