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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.shared.transfer.dependencies.DefaultDependableCoordinate;
import org.apache.maven.shared.transfer.dependencies.DependableCoordinate;

/**
 * Utility class to map artifact instance to other kind of object.
 *
 * @author Nikolas Falco
 */
public final class ArtifactUtil {

    private ArtifactUtil() {
        // default constructor
    }

    /**
     * Utility method to transform an artifact to a dependable coordinate.
     *
     * @param artifact
     *            to transform
     * @return a new instance of coordinate
     */
    public static DependableCoordinate toCoordinate(Artifact artifact) {
        DefaultDependableCoordinate coordinate = new DefaultDependableCoordinate();
        coordinate.setGroupId(artifact.getGroupId());
        coordinate.setArtifactId(artifact.getArtifactId());
        coordinate.setVersion(artifact.getVersion());
        coordinate.setType(artifact.getType());
        coordinate.setClassifier(artifact.getClassifier());
        return coordinate;
    }

    /**
     * Utility method to transform a dependable coordinate to an artifact.
     *
     * @param coordinate
     *            to transform
     * @return a new artifact instance
     */
    public static Artifact toArtifact(DependableCoordinate coordinate) {
        return new DefaultArtifact(coordinate.getGroupId(), coordinate.getArtifactId(), //
                coordinate.getVersion(), null, coordinate.getType(), //
                coordinate.getClassifier(), new DefaultArtifactHandler(coordinate.getType()));
    }
}
