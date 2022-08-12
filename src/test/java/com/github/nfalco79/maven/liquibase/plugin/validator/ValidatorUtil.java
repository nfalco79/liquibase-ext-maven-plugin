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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;

import liquibase.change.core.AddColumnChange;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.LiquibaseException;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

public final class ValidatorUtil {

    private ValidatorUtil() {
    }

    public static AddColumnChange getAddColumnChange() {
        DatabaseChangeLog changeLog = mock(DatabaseChangeLog.class);
        when(changeLog.getPhysicalFilePath()).thenReturn("myfilepath");

        ChangeSet changeSet = mock(ChangeSet.class);
        when(changeSet.getChangeLog()).thenReturn(changeLog);
        when(changeSet.getId()).thenReturn("id");
        when(changeSet.getAuthor()).thenReturn("author");

        AddColumnChange change = spy(new AddColumnChange());
        doReturn(changeSet).when(change).getChangeSet();
        return change;
    }

    public static ChangeSet getChangeSet() {
        DatabaseChangeLog changeLog = mock(DatabaseChangeLog.class);
        when(changeLog.getPhysicalFilePath()).thenReturn("myfilepath");

        ChangeSet changeSet = spy(new ChangeSet(changeLog));
        when(changeSet.getChangeLog()).thenReturn(changeLog);
        when(changeSet.getId()).thenReturn("id");
        when(changeSet.getAuthor()).thenReturn("author");

        return changeSet;
    }

    public static DatabaseChangeLog load(Class<?> clazz, String resource) throws LiquibaseException {
        return load(clazz.getPackage().getName().replace('.', '/'), resource);
    }

    public static DatabaseChangeLog load(String packageName, String resource) throws LiquibaseException {
        String resourcePath = packageName + '/' + resource;
        ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();

        String ext = FileUtils.getExtension(resource);
        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(ext, resourceAccessor);

        return parser.parse(resourcePath, new ChangeLogParameters(), resourceAccessor);
    }

    public static List<String> extractMessage(Collection<ValidationError> issues) {
        List<String> messages = new ArrayList<>(issues.size());
        for (ValidationError issue : issues) {
            messages.add(issue.toString());
        }
        return messages;
    }
}