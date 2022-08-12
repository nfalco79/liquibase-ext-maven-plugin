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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.util.StringUtils;

import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;
import com.github.nfalco79.maven.liquibase.plugin.validator.Validator.Scope;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateIndexChange;
import liquibase.change.core.DropIndexChange;

@Validator(name = "duplicatedIndex", scope = Scope.SINGLETON)
public class DuplicatedIndexValidator implements IChangeValidator {

    private BiMap<String, String> indexes = HashBiMap.create();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Collection<ValidationError> validate(Change change) {
        Collection<ValidationError> issues = new ArrayList<>();

        if (change instanceof CreateIndexChange) {
            CreateIndexChange indexChange = (CreateIndexChange) change;
            String indexName = indexChange.getIndexName();
            String composedKey = composedKey(indexChange.getTableName(), (List) indexChange.getColumns());

            if (!indexes.containsValue(composedKey)) {
                indexes.put(indexName, composedKey);
            } else {
                String message = "The index " + indexName + " is already defined by " + indexes.inverse().get(composedKey);
                issues.add(LiquibaseUtil.createIssue(change, "name", message));
            }
        } else if (change instanceof DropIndexChange) {
            DropIndexChange indexChange = (DropIndexChange) change;
            indexes.remove(indexChange.getIndexName());
        }

        return issues;
    }

    private String composedKey(String tableName, List<ColumnConfig> columns) {
        Set<String> keys = new LinkedHashSet<>();
        keys.add(tableName);
        columns.forEach(c -> keys.add(c.getName()));
        return StringUtils.join(keys.iterator(), "-");
    }

}
