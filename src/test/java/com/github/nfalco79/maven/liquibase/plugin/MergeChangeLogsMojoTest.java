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

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.internal.DefaultDependencyNode;
import org.apache.maven.shared.utils.xml.Xpp3Dom;
import org.apache.maven.shared.utils.xml.Xpp3DomBuilder;
import org.assertj.core.api.Assertions;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.nfalco79.maven.MavenUtils;

public class MergeChangeLogsMojoTest {

    @Rule
    public TemporaryFolder fileRule = new TemporaryFolder();

    @Test
    public void test_relativeToChangeLogs() throws Exception {
        MavenProject mavenProject = buildMavenProject("g", "a", "1");
        mavenProject.addResource(newResource("merge/db.changelog.xml"));

        MergeChangeLogsMojo mojo = defaultMojo(mavenProject);
        mojo.setRelativeToChangelogFile(true);
        mojo.setChangeLogs(new String[] { "**/db.changelog.xml" });
        mojo.execute();

        Xpp3Dom dom = Xpp3DomBuilder.build(new FileReader(new File(mojo.getOutputDirectory(), mojo.getOutputChangelog())));
        Xpp3Dom[] includes = dom.getChildren("include");
        Assertions.assertThat(includes).hasSize(3);

        for (Xpp3Dom include : includes) {
            Assertions.assertThat(include.getAttribute("relativeToChangelogFile")).isEqualTo("true");
            Assert.assertThat(include.getAttribute("file"), anyOf(is("com/acme/a1/db.changelog.xml"), is("com/acme/a2/db.changelog.xml"), is("merge/db.changelog.xml")));
        }
    }

    @Test
    public void test_includes_different_changelogs_master() throws Exception {
        String master1 = "merge/master.changelog-test1.xml";
        String master2 = "merge/master.changelog-test2.xml";

        MavenProject mavenProject = buildMavenProject("g", "a", "1");
        mavenProject.addResource(newResource("merge/db.changelog.xml"));
        newResource("merge/master.changelog-1.0.xml");
        newResource(master1);
        newResource(master2);

        MergeChangeLogsMojo mojo = defaultMojo(mavenProject);
        mojo.setRelativeToChangelogFile(true);
        mojo.setIncludes(new String[] { "**/master.changelog*.xml", "**/db.changelog*.xml" });
        mojo.setChangeLogs(new String[] {"**/*test1.xml", "**/*test2.xml"});
        mojo.execute();

        Xpp3Dom dom = Xpp3DomBuilder.build(new FileReader(new File(mojo.getOutputDirectory(), mojo.getOutputChangelog())));
        Xpp3Dom[] includes = dom.getChildren("include");
        Assertions.assertThat(includes).hasSize(2);

        for (Xpp3Dom include : includes) {
            Assertions.assertThat(include.getAttribute("relativeToChangelogFile")).isEqualTo("true");
            Assert.assertThat(include.getAttribute("file"), anyOf(is(master1), is(master2)));
        }
    }

    @Test
    public void test_excludes_from_dependencies() throws Exception {
        MavenProject mavenProject = buildMavenProject("g", "a", "1");

        MergeChangeLogsMojo mojo = defaultMojo(mavenProject);
        mojo.setRelativeToChangelogFile(true);
        mojo.setResources(new String[] {});
        mojo.setIncludes(new String[] { "**/*.changelog.xml" });
        mojo.setChangeLogs(new String[] { "**/db.changelog.xml" });
        mojo.setExcludes(new String[] { "**/a1/*.xml" });
        mojo.execute();

        Xpp3Dom dom = Xpp3DomBuilder.build(new FileReader(new File(mojo.getOutputDirectory(), mojo.getOutputChangelog())));
        Xpp3Dom[] includes = dom.getChildren("include");
        Assertions.assertThat(includes).hasSize(1);

        for (Xpp3Dom include : includes) {
            Assert.assertThat(include.getAttribute("file"), is("com/acme/a2/db.changelog.xml"));
        }
    }

    @Test
    public void test_maxAttemptsToResolveDependencies() throws Exception {
        MavenProject mavenProject = buildMavenProject("g", "a", "1");

        MergeChangeLogsMojo mojo = spy(new MergeChangeLogsMojo());
        mojo.dependencyGraphBuilder = buildGraphBuilder();
        mojo.project = mavenProject;
        mojo.session = buildMavenSession(mavenProject);
        mojo.setIncludeResources(true);
        mojo.setOutputDirectory(fileRule.newFolder());;
        mojo.setRelativeToChangelogFile(true);
        mojo.setMaxAttemptsToResolveDependencies(1);

        File artifactFolder = fileRule.newFolder();
        DefaultArtifact artifact = MavenUtils.buildArtifact("com.acme", "a1", "1.0");
        artifact.setFile(File.createTempFile("com.acme", ".jar", artifactFolder));

        DefaultDependencyNode rootNode = new DefaultDependencyNode(null, buildArtifactAndJAR("org.acme", "core", "1.0"), null, null, null);
        rootNode.setChildren(Arrays.asList(new DefaultDependencyNode(rootNode, artifact, null, null, null)));
        rootNode.getChildren().forEach(n -> ((DefaultDependencyNode) n).setChildren(Collections.emptyList()));

        when(mojo.dependencyGraphBuilder.buildDependencyGraph(any(ProjectBuildingRequest.class), any(ArtifactFilter.class))) //
                .thenReturn(rootNode);

        Assertions.assertThatThrownBy(() -> mojo.execute()).hasMessage("Fail to download artifact " + artifact.toString() + ", size is 0");
    }

    private File buildJar(Artifact artifact) throws IOException {
        File file = fileRule.newFile();
        String logicaFilePath = (artifact.getGroupId() + "/" + artifact.getArtifactId()).replace('.', '/');
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
            zos.putNextEntry(new ZipEntry(logicaFilePath + "/db.changelog.xml"));
            try (InputStream is = getClass().getResourceAsStream("merge/changelog_" + artifact.getArtifactId() + ".xml")) {
                if (is != null) {
                    IOUtils.copy(is, zos);
                }
            }
        }
        return file;
    }

    private MergeChangeLogsMojo defaultMojo(MavenProject mavenProject) throws Exception {
        MergeChangeLogsMojo mojo = new MergeChangeLogsMojo();
        mojo.dependencyGraphBuilder = buildGraphBuilder();
        mojo.project = mavenProject;
        mojo.session = buildMavenSession(mavenProject);
        mojo.setIncludeResources(true);
        mojo.setOutputDirectory(fileRule.newFolder());

        return mojo;
    }

    private MavenProject buildMavenProject(String groupId, String artifactId, String version) {
        MavenProject mavenProject = new MavenProject();
        mavenProject.setGroupId(groupId);
        mavenProject.setArtifactId(artifactId);
        mavenProject.setVersion(version);
        mavenProject.setPackaging("jar");
        return mavenProject;
    }

    private MavenSession buildMavenSession(MavenProject mavenProject) {
        MavenSession mavenSession = mock(MavenSession.class);
        when(mavenSession.getCurrentProject()).thenReturn(mavenProject);
        when(mavenSession.getProjectBuildingRequest()).thenReturn(mock(ProjectBuildingRequest.class));
        when(mavenSession.getProjects()).thenReturn(Collections.emptyList());
        return mavenSession;
    }

    private DependencyGraphBuilder buildGraphBuilder() throws Exception {
        DependencyGraphBuilder graphBuilder = mock(DependencyGraphBuilder.class);
        DefaultDependencyNode rootNode = new DefaultDependencyNode(null, buildArtifactAndJAR("org.acme", "core", "1.0"), null, null, null);
        rootNode.setChildren(Arrays.asList( //
                new DefaultDependencyNode(rootNode, buildArtifactAndJAR("com.acme", "a1", "1.0"), null, null, null), //
                new DefaultDependencyNode(rootNode, buildArtifactAndJAR("com.acme", "a2", "1.0"), null, null, null)));
        rootNode.getChildren().forEach(n -> ((DefaultDependencyNode) n).setChildren(Collections.emptyList()));
        when(graphBuilder.buildDependencyGraph(any(ProjectBuildingRequest.class), any(ArtifactFilter.class))) //
            .thenReturn(rootNode);
        return graphBuilder;
    }

    private Artifact buildArtifactAndJAR(String groupId, String artifactId, String version) throws Exception {
        DefaultArtifact artifact = MavenUtils.buildArtifact(groupId, artifactId, version);
        artifact.setFile(buildJar(artifact));
        return artifact;
    }

    private Resource newResource(String file) throws IOException {
        File src = new File(fileRule.getRoot(), "src");
        if (!src.exists()) {
            src.mkdirs();
        }
        FileUtils.copyURLToFile(getClass().getResource(file), new File(src, file));

        Resource resource = new Resource();
        resource.setDirectory(src.getAbsolutePath());
        return resource;
    }
}
