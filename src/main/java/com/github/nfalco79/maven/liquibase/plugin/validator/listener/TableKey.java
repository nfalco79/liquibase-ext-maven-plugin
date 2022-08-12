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
 * A key implementation to lookup all object of a given table in the storage.
 */
public class TableKey extends AbstractSearchKey {

    private final String tableName;

    /**
     * Default constructor for a table search.
     *
     * @param tableName
     *            the name of the table
     */
    public TableKey(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public int hashCode() { // NOSONAR equals is inherited
        return Objects.hash(tableName);
    }

    @Override
    public String toString() {
        return tableName;
    }
}