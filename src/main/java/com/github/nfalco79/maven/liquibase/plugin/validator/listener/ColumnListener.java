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

import java.util.List;
import java.util.stream.Collectors;

import com.github.nfalco79.maven.liquibase.plugin.util.StringUtil;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.AddNotNullConstraintChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropColumnChange;
import liquibase.change.core.DropNotNullConstraintChange;
import liquibase.change.core.DropTableChange;
import liquibase.change.core.ModifyDataTypeChange;
import liquibase.change.core.RenameColumnChange;
import liquibase.change.core.RenameTableChange;

/**
 * Listener for changes which involve new columns.
 */
public class ColumnListener implements IChangeListener {

    @Override
    public void updateStorage(Change change, ChangeStorage storage) {
        if (change instanceof CreateTableChange) {
            handle(storage, (CreateTableChange) change);
        } else if (change instanceof AddColumnChange) {
            handle(storage, (AddColumnChange) change);
        } else if (change instanceof RenameTableChange) {
            handle(storage, (RenameTableChange) change);
        } else if (change instanceof RenameColumnChange) {
            handle(storage, (RenameColumnChange) change);
        } else if (change instanceof AddNotNullConstraintChange) {
            handle(storage, (AddNotNullConstraintChange) change);
        } else if (change instanceof DropNotNullConstraintChange) {
            handle(storage, (DropNotNullConstraintChange) change);
        } else if (change instanceof ModifyDataTypeChange) {
            handle(storage, (ModifyDataTypeChange) change);
        } else if (change instanceof DropTableChange) {
            handle(storage, (DropTableChange) change);
        } else if (change instanceof DropColumnChange) {
            handle(storage, (DropColumnChange) change);
        }
    }

    private void handle(ChangeStorage storage, DropColumnChange dropColumn) {
        String tableName = dropColumn.getTableName();
        dropColumn.getColumns().forEach(c -> storage.remove(new ColumnKey(tableName, c.getName())));
    }

    private void handle(ChangeStorage storage, DropTableChange dropTable) {
        TableKey key = new TableKey(dropTable.getTableName());
        storage.keySet().removeIf(k -> k.equals(key));
    }

    private void handle(ChangeStorage storage, ModifyDataTypeChange modifyType) {
        String newDataType = modifyType.getNewDataType();
        String type = StringUtil.removeParam(newDataType);
        String length = StringUtil.getParam(newDataType);

        storage.filterBy(ColumnInfo.class, new ColumnKey(modifyType.getTableName(), modifyType.getColumnName())) //
            .forEach(info -> {
                info.setType(type);
                info.setLength(length);
            });
    }

    private void handle(ChangeStorage storage, DropNotNullConstraintChange dropNotNull) {
        storage.filterBy(ColumnInfo.class, new ColumnKey(dropNotNull.getTableName(), dropNotNull.getColumnName())) //
                .forEach(info -> info.setNullable(true));
    }

    private void handle(ChangeStorage storage, AddNotNullConstraintChange addNotNull) {
        storage.filterBy(ColumnInfo.class, new ColumnKey(addNotNull.getTableName(), addNotNull.getColumnName())) //
                .forEach(info -> info.setNullable(false));
    }

    private void handle(ChangeStorage storage, RenameColumnChange renameColumn) {
        String newColumnName = renameColumn.getNewColumnName();
        String oldColumnName = renameColumn.getOldColumnName();

        storage.filterBy(ColumnInfo.class, new ColumnKey(renameColumn.getTableName(), oldColumnName)) //
                .forEach(info -> info.setName(newColumnName));
    }

    private void handle(ChangeStorage storage, RenameTableChange renameTable) {
        String newTableName = renameTable.getNewTableName();
        String oldTableName = renameTable.getOldTableName();

        List<ColumnInfo> a = storage.filterBy(ColumnInfo.class, new TableKey(oldTableName)).collect(Collectors.toList());
        for (ColumnInfo info : a) {
                info.setTable(newTableName);
        }
    }

    private void handle(ChangeStorage storage, AddColumnChange addColumn) {
        String tableName = addColumn.getTableName();

        processColumns(storage, tableName, addColumn.getColumns());
    }

    private void handle(ChangeStorage storage, CreateTableChange createTable) {
        String tableName = createTable.getTableName();

        processColumns(storage, tableName, createTable.getColumns());
    }

    private void processColumns(ChangeStorage storage, String table, List<? extends ColumnConfig> columns) {
        for (ColumnConfig col : columns) {
            String name = col.getName();
            String type = StringUtil.removeParam(col.getType());
            String length = StringUtil.getParam(col.getType());
            ColumnInfo info = new ColumnInfo(table, name);

            ConstraintsConfig constraints = col.getConstraints();
            Boolean nullable = constraints == null || constraints.isNullable() == null || constraints.isNullable();
            info.setNullable(nullable);
            info.setType(type);
            info.setLength(length);

            storage.put(info.getKey(), info);
        }
    }

    @Override
    public boolean applyTo(Change change) {
        return change instanceof CreateTableChange // NOSONAR
                || change instanceof AddColumnChange //
                || change instanceof RenameTableChange //
                || change instanceof RenameColumnChange //
                || change instanceof AddNotNullConstraintChange //
                || change instanceof DropNotNullConstraintChange //
                || change instanceof ModifyDataTypeChange //
                || change instanceof DropTableChange //
                || change instanceof DropColumnChange;
    }

}
