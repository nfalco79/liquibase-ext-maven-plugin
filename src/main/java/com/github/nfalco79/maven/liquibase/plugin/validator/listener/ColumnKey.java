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
package com.github.nfalco79.maven.liquibase.plugin.validator.listener;

import java.util.Objects;

/**
 * A key implementation to lookup a given column in the storage.
 *
 * @author Nikolas Falco
 */
public class ColumnKey extends AbstractSearchKey {

    private final String tableName;
    private final String columnName;

    /**
     * Default constructor for column key search.
     *
     * @param tableName name of owner table
     * @param columnName name of this column
     */
    public ColumnKey(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, columnName);
    }

    @Override
    public String toString() {
        return tableName + '.' + columnName;
    }
}