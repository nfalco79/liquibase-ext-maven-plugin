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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.internal.DefaultDependencyNode;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;
import org.assertj.core.api.Assertions;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import liquibase.exception.ValidationFailedException;

public class UpdateSQLMojoTest {

    @Rule
    public TemporaryFolder fileRule = new TemporaryFolder();

    private String h2URL;

    @Before
    public void setup() throws Exception {
        h2URL = "jdbc:h2:" + fileRule.newFile("database").getAbsolutePath();
    }

    @Test
    public void test_backward_compatibility() throws Exception {
        MavenProject mavenProject = buildMavenProject("test", "test", "1.1-SNAPSHOT");
        mavenProject.addResource(newResource("update/backwardCompatibility-test1.xml"));

        UpdateSQLMojo mojo = defaultMojo(mavenProject);
        mojo.setIncludeResources(true);
        mojo.setIncludes(new String[] { "db.changelog.xml" });
        mojo.setSkipBackwardCompatibility(false);
        mojo.artifactMetadataSource = buildMetadataSource(new DefaultArtifactVersion("1.0"));

        mojo.execute();

        try (Connection conn = DriverManager.getConnection(h2URL, "sa", null)) {
            conn.prepareCall("INSERT INTO test_table(id, tenant_id, new) VALUES (1, 'something', 5)").execute();
        }
    }

    @Test
    public void fail_when_same_changeset_have_different_checksum() throws Exception {
        MavenProject mavenProject = buildMavenProject("test", "test", "1.1-SNAPSHOT");
        mavenProject.addResource(newResource("update/backwardCompatibility-test2.xml"));

        UpdateSQLMojo mojo = spy(defaultMojo(mavenProject));
        mojo.setSkipBackwardCompatibility(false);
        mojo.artifactMetadataSource = buildMetadataSource(new DefaultArtifactVersion("1.0"));
        DefaultArtifact previousArtifact = buildArtifact("test", "test", "1.0");
        previousArtifact.setFile(buildJar());
        doReturn(Arrays.asList(previousArtifact)).when(mojo).buildDependencyOrder(any(Artifact.class));

        Assertions.assertThatThrownBy(() -> mojo.execute()).getCause() //
                .isInstanceOf(ValidationFailedException.class) //
                .hasMessageContaining("changesets check sum");
    }

    @Test
    public void verify_dependency_filter() throws Exception {
        MavenProject mavenProject = buildMavenProject("test", "test", "1.1-SNAPSHOT");

        UpdateSQLMojo mojo = defaultMojo(mavenProject);
        mojo.setSkipBackwardCompatibility(false);
        mojo.artifactMetadataSource = buildMetadataSource(new DefaultArtifactVersion("1.0"));

        mojo.execute();

        ArgumentCaptor<DependencyRequest> captor = ArgumentCaptor.forClass(DependencyRequest.class);
        verify(mojo.repositorySystem).resolveDependencies(any(RepositorySystemSession.class), captor.capture());
        DependencyFilter filter = captor.getValue().getFilter();

        Dependency dependency = new Dependency(new org.eclipse.aether.artifact.DefaultArtifact("g:a:pom:v"), "runtime", false);
        List<DependencyNode> parents = Collections.emptyList();

        // we accept only jar type
        Assertions.assertThat(filter.accept(new org.eclipse.aether.graph.DefaultDependencyNode(dependency), parents)).isFalse();

        // we do not accept provided or system scope
        dependency = new Dependency(new org.eclipse.aether.artifact.DefaultArtifact("g:a:jar:v"), "provided", false);
        Assertions.assertThat(filter.accept(new org.eclipse.aether.graph.DefaultDependencyNode(dependency), parents)).isFalse();

        // we accept optional
        dependency = new Dependency(new org.eclipse.aether.artifact.DefaultArtifact("g:a:jar:v"), "runtime", true);
        Assertions.assertThat(filter.accept(new org.eclipse.aether.graph.DefaultDependencyNode(dependency), parents)).isTrue();
    }

    @Test
    public void test_latest_version() throws Exception {
        MavenProject mavenProject = buildMavenProject("test", "test", "1.1-SNAPSHOT");

        UpdateSQLMojo mojo = spy(defaultMojo(mavenProject));
        mojo.setSkipBackwardCompatibility(false);
        mojo.artifactMetadataSource = buildMetadataSource(new DefaultArtifactVersion("0.9"), new DefaultArtifactVersion("2.0"), new DefaultArtifactVersion("1.0-SNAPSHOT"));
        mojo.execute();

        ArgumentCaptor<Artifact> captor = ArgumentCaptor.forClass(Artifact.class);
        verify(mojo).buildDependencyOrder(captor.capture());
        Assertions.assertThat(captor.getValue().getVersion()).isEqualTo("0.9");
    }

    @Test
    public void verify_dependency_order() throws Exception {
        Artifact artifact1 = buildArtifact("g1", "a1", "v");
        Artifact artifact2 = buildArtifact("g2", "a1", "v");
        Artifact artifact3 = buildArtifact("g2", "a2", "v");

        MavenProject mavenProject = buildMavenProject("test", "test", "1.1-SNAPSHOT");
        Artifact projectArtifact = buildArtifact("test", "test", "1.0");

        UpdateSQLMojo mojo = defaultMojo(mavenProject);
        mojo.setSkipBackwardCompatibility(false);

        List<ArtifactResult> results = new ArrayList<>();
        when(mojo.repositorySystem.resolveDependencies(any(RepositorySystemSession.class), any(DependencyRequest.class))).thenAnswer(new Answer<DependencyResult>() {
            @Override
            public DependencyResult answer(InvocationOnMock invocation) throws Throwable {
                DependencyRequest request = invocation.getArgument(1);

                org.eclipse.aether.graph.DefaultDependencyNode node1 = new org.eclipse.aether.graph.DefaultDependencyNode(RepositoryUtils.toDependency(artifact1, null));
                org.eclipse.aether.graph.DefaultDependencyNode node2 = new org.eclipse.aether.graph.DefaultDependencyNode(RepositoryUtils.toDependency(artifact2, null));

                org.eclipse.aether.graph.DefaultDependencyNode node3 = new org.eclipse.aether.graph.DefaultDependencyNode(RepositoryUtils.toDependency(artifact3, null));
                node2.setChildren(Arrays.asList(node3));

                org.eclipse.aether.graph.DefaultDependencyNode root = new org.eclipse.aether.graph.DefaultDependencyNode(request.getCollectRequest().getRoot());
                root.setChildren(Arrays.asList(node1, node2));

                DependencyResult result = new DependencyResult(request);
                result.setRoot(root);
                return result;
            }
        });

        // add a couple of dependencies
        Arrays.asList(artifact1, artifact2, artifact3).forEach(a -> {
            ArtifactResult result = mock(ArtifactResult.class);
            when(result.getArtifact()).thenReturn(a);
            results.add(result);
        });

        Assertions.assertThat(mojo.buildDependencyOrder(projectArtifact)).containsExactly(artifact1, artifact3, artifact2, projectArtifact);
    }

    private DefaultArtifact buildArtifact(String groupId, String artifacId, String version) {
        return new DefaultArtifact(groupId, artifacId, version, "runtime", "jar", null, new DefaultArtifactHandler("jar"));
    }

    @Test
    public void test_update() throws Exception {
        MavenProject mavenProject = new MavenProject();
        mavenProject.addResource(newResource("update/db.changelog.xml"));

        UpdateSQLMojo mojo = defaultMojo(mavenProject);
        mojo.execute();

        try (Connection conn = DriverManager.getConnection(h2URL, "sa", null)) {
            conn.prepareCall("INSERT INTO test_table(id, tenant_id) VALUES (1, 'something')").execute();
        }
    }

    private File buildJar() throws IOException {
        File file = fileRule.newFile();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
            zos.putNextEntry(new ZipEntry("db.changelog.xml"));
            try (InputStream is = getClass().getResourceAsStream("update/backwardCompatibility-1.0.xml")) {
                IOUtils.copy(is, zos);
            }
        }
        return file;
    }

    private UpdateSQLMojo defaultMojo(MavenProject mavenProject) throws Exception {
        DatabaseConfiguration dbConf = new DatabaseConfiguration();
        dbConf.setUrl(h2URL);
        dbConf.setDriver("org.h2.Driver");
        dbConf.setUsername("sa");

        UpdateSQLMojo mojo = new UpdateSQLMojo();
        mojo.setDatabase(dbConf);
        mojo.dependencyGraphBuilder = buildGraphBuilder();
        mojo.project = mavenProject;
        mojo.session = buildMavenSession(mavenProject);
        mojo.setIncludeResources(true);
        mojo.setIncludes(new String[] { "db.changelog.xml" });
        mojo.setChangeLogs(new String[] { "**/db.changelog.xml" });
        mojo.setOutputDirectory(fileRule.newFolder()); // target folder
        mojo.setBackwardCompatibilityVersion("(," + mavenProject.getVersion() + ")");
        mojo.artifactHandlerManager = buildArtifactHandlerManager(mavenProject.getPackaging());
        mojo.artifactResolver = buildArtifactResolver();
        mojo.repositorySystem = buildRepositorySystem();

        return mojo;
    }

    private RepositorySystem buildRepositorySystem() throws Exception {
        RepositorySystem repositorySystem = mock(org.eclipse.aether.RepositorySystem.class);
        when(repositorySystem.resolveDependencies(any(RepositorySystemSession.class), any(DependencyRequest.class))).thenAnswer(new Answer<DependencyResult>() {
            @Override
            public DependencyResult answer(InvocationOnMock invocation) throws Throwable {
                DependencyRequest request = invocation.getArgument(1);
                Dependency root = request.getCollectRequest().getRoot();
                DependencyResult result = new DependencyResult(request);
                result.setRoot(new org.eclipse.aether.graph.DefaultDependencyNode(root));
                return result;
            }
        });
        return repositorySystem;
    }

    private MavenProject buildMavenProject(String groupId, String artifactId, String version) {
        MavenProject mavenProject = new MavenProject();
        mavenProject.setGroupId(groupId);
        mavenProject.setArtifactId(artifactId);
        mavenProject.setVersion(version);
        mavenProject.setPackaging("jar");
        return mavenProject;
    }

    private ArtifactResolver buildArtifactResolver() throws Exception {
        ArtifactResolver resolver = mock(ArtifactResolver.class);
        when(resolver.resolveArtifact(any(ProjectBuildingRequest.class), any(Artifact.class))).thenAnswer(invocation -> {
            Artifact artifact = invocation.getArgument(1);
            artifact.setFile(buildJar());
            ArtifactResult result = mock(ArtifactResult.class);
            when(result.getArtifact()).thenReturn(artifact);
            return result;
        });
        return resolver;
    }

    private MavenSession buildMavenSession(MavenProject mavenProject) {
        MavenSession mavenSession = mock(MavenSession.class);
        when(mavenSession.getCurrentProject()).thenReturn(mavenProject);
        when(mavenSession.getProjectBuildingRequest()).thenReturn(mock(ProjectBuildingRequest.class));
        when(mavenSession.getProjects()).thenReturn(Collections.emptyList());
        return mavenSession;
    }

    private DependencyGraphBuilder buildGraphBuilder() throws DependencyGraphBuilderException {
        DependencyGraphBuilder graphBuilder = mock(DependencyGraphBuilder.class);
        DefaultDependencyNode rootNode = new DefaultDependencyNode(null, new DefaultArtifact("org.acme", "core", "1.0", "test", "jar", "x", null), null, null, null);
        rootNode.setChildren(Collections.emptyList());
        when(graphBuilder.buildDependencyGraph(any(ProjectBuildingRequest.class), any(ArtifactFilter.class))) //
                .thenReturn(rootNode);
        return graphBuilder;
    }

    private ArtifactHandlerManager buildArtifactHandlerManager(String... packaging) {
        ArtifactHandlerManager artifactHandlerManager = mock(ArtifactHandlerManager.class);
        for (String type : packaging) {
            when(artifactHandlerManager.getArtifactHandler(type)).thenReturn(new DefaultArtifactHandler(type));
        }
        return artifactHandlerManager;
    }

    @SuppressWarnings("deprecation")
    private org.apache.maven.artifact.metadata.ArtifactMetadataSource buildMetadataSource(ArtifactVersion... versions) throws Exception {
        org.apache.maven.artifact.metadata.ArtifactMetadataSource metadataSource = mock(org.apache.maven.artifact.metadata.ArtifactMetadataSource.class);
        when(metadataSource.retrieveAvailableVersions(any(Artifact.class), any(), any())).thenReturn(Arrays.asList(versions));
        return metadataSource;
    }

    private Resource newResource(String file) throws IOException {
        File resourceFolder = fileRule.newFolder("src");
        File dbChangeLog = new File(resourceFolder, "db.changelog.xml");
        FileUtils.copyURLToFile(getClass().getResource(file), dbChangeLog);

        Resource resource = new Resource();
        resource.setDirectory(resourceFolder.getCanonicalPath());
        return resource;
    }
}
