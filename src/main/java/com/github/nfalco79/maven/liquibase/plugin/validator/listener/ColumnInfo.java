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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class to collect information about a column.
 */
public class ColumnInfo implements IStorageInfo {
    private class ColumnInfoKey implements IStorageKey {

        public String getTable() {
            return table;
        }

        public String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            return Objects.hash(table, name);
        }

        @Override
        public boolean equals(Object obj) { // NOSONAR
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                ColumnInfoKey other = (ColumnInfoKey) obj;
                return Objects.equals(table, other.getTable()) //
                        && Objects.equals(name, other.getName());
            } else if (obj instanceof IStorageKey) {
                return isEqualsTo((IStorageKey) obj);
            }
            return false;
        }

        @Override
        public String toString() {
            return table + "." + name;
        }

        @Override
        public boolean isEqualsTo(IStorageKey obj) { // NOSONAR
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (obj instanceof ColumnKey) {
                ColumnKey other = (ColumnKey) obj;
                return Objects.equals(table, other.getTableName()) //
                        && Objects.equals(name, other.getColumnName());
            } else if (obj instanceof TableKey) {
                TableKey other = (TableKey) obj;
                return Objects.equals(table, other.getTableName());
            } else {
                return true;
            }
        }
    }

    private String table;
    private String name;
    private boolean nullable = true;
    private String type;
    private String oldType;
    private String length;
    private List<ConstraintInfo> constraints = new ArrayList<ConstraintInfo>();

    /**
     * Default constructor. Storage all column informations to be used by
     * validators.
     *
     * @param table
     *            name
     * @param name
     *            of this column.
     */
    public ColumnInfo(String table, String name) {
        this.table = table;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    /**
     * Return the column type without the length (e.g. VARCHAR(X) only VARCHAR)
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.oldType = this.type;
        this.type = type;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public List<ConstraintInfo> getConstraints() {
        return constraints; // NOSONAR
    }

    public void setConstraints(List<ConstraintInfo> constraints) {
        this.constraints = constraints; // NOSONAR
    }

    @Override
    public IStorageKey getKey() {
        return new ColumnInfoKey();
    }

    @Override
    public Object getValue() {
        return this;
    }

    @Override
    public String toString() {
        return table + '.' + name;
    }

    public String getOldType() {
        return oldType;
    }

    public void setOldType(String oldType) {
        this.oldType = oldType;
    }
}