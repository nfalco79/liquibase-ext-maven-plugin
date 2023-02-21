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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.component.annotations.Configuration;

import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;
import com.github.nfalco79.maven.liquibase.plugin.validator.Validator.Scope;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;

/**
 * Check that logicalFilePath attribute is defined and match the artifactId of
 * the project.
 */
@Validator(name = "filePath", scope = Scope.SINGLETON)
public class FilePathValidator implements IChangeLogValidator, IChangeSetValidator {

    public static final char PATH_SEPARATOR = '/';
    public static final char WINDOWS_PATH_SEPARATOR = '\\';

    private ThreadLocal<Set<String>> cache = ThreadLocal.<Set<String>>withInitial(LinkedHashSet::new);

    private String artifactId;
    private final File baseSourceDir;

    @Configuration(value = "suffix")
    private String suffix;

    /**
     * Constructs an instance of this class.
     *
     * @param artifactId
     *            the artifact id of the project
     */
    public FilePathValidator(String artifactId) {
        this.artifactId = artifactId;
        this.baseSourceDir = null;
    }

    /**
     * Constructs an instance of this class.
     *
     * @param baseSourceDir
     *            the liquibase scripts base directory
     */
    public FilePathValidator(File baseSourceDir) {
        this.baseSourceDir = baseSourceDir;
    }

    @Override
    public Collection<ValidationError> validate(DatabaseChangeLog changeLog) {
        Collection<ValidationError> issues = new LinkedList<>();

        String physicalFilePath = changeLog.getPhysicalFilePath();
        if (!cache.get().contains(physicalFilePath)) {
            cache.get().add(physicalFilePath);
            String calculatedArtifactId = artifactId;

            String logicalFilePath = changeLog.getLogicalFilePath();
            if (artifactId == null) {
                calculatedArtifactId = relativize(new File(physicalFilePath).getParentFile()); // NOSONAR
                if (suffix != null) {
                    calculatedArtifactId += '.' + suffix;
                }
            }
            /*
             * using getFilePath avoid any liquibase logic path separator that cause
             * on windows a false positive
             */
            if (StringUtils.isEmpty(logicalFilePath) || changeLog.getFilePath().equals(physicalFilePath)) {
                issues.add(LiquibaseUtil.createIssue(changeLog, "logicalFilePath", "The logicalFilePath attribute is required"));
            } else if (!calculatedArtifactId.equals(logicalFilePath)) {
                issues.add(LiquibaseUtil.createIssue(changeLog, "logicalFilePath", "The logicalFilePath attribute does not match the artifactId of the project"));
            }
        }
        return issues;
    }

    private String relativize(File file) {
        // relative path can be calculated only if both are absolute or not
        if (baseSourceDir.isAbsolute() && !file.isAbsolute()) {
            file = new File("/" + file.getPath());
        }

        if (!file.getPath().startsWith(baseSourceDir.getPath())) {
            file = file.getAbsoluteFile();
        }
        String relativePath = baseSourceDir.toPath().relativize(file.toPath()).toString();
        return StringUtils.removeEnd(relativePath, String.valueOf(PATH_SEPARATOR)).replace(PATH_SEPARATOR, '.').replace(WINDOWS_PATH_SEPARATOR, '.');
    }

    @Override
    public Collection<ValidationError> validate(ChangeSet changeSet) {
        return validate(changeSet.getChangeLog());
    }

}
