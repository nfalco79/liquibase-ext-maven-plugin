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
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ConstraintInfo.ConstraintType;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.change.core.AddPrimaryKeyChange;
import liquibase.change.core.AddUniqueConstraintChange;
import liquibase.change.core.CreateIndexChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropAllForeignKeyConstraintsChange;
import liquibase.change.core.DropForeignKeyConstraintChange;
import liquibase.change.core.DropIndexChange;
import liquibase.change.core.DropPrimaryKeyChange;
import liquibase.change.core.DropUniqueConstraintChange;

/**
 * Listener for changes which involve new columns.
 */
public class ConstraintListener implements IChangeListener {

    @Override
    public void updateStorage(Change change, ChangeStorage storage) { // NOSONAR
        if (change instanceof CreateTableChange) {
            handle(storage, (CreateTableChange) change);
        } else if (change instanceof AddColumnChange) {
            handle(storage, (AddColumnChange) change);
        } else if (change instanceof CreateIndexChange) {
            handle(storage, (CreateIndexChange) change);
        } else if (change instanceof AddUniqueConstraintChange) {
            handle(storage, (AddUniqueConstraintChange) change);
        } else if (change instanceof AddPrimaryKeyChange) {
            handle(storage, (AddPrimaryKeyChange) change);
        } else if (change instanceof AddForeignKeyConstraintChange) {
            handle(storage, (AddForeignKeyConstraintChange) change);
        } else if (change instanceof DropIndexChange) {
            handle(storage, (DropIndexChange) change);
        } else if (change instanceof DropPrimaryKeyChange) {
            handle(storage, (DropPrimaryKeyChange) change);
        } else if (change instanceof DropAllForeignKeyConstraintsChange) {
            handle(storage, (DropAllForeignKeyConstraintsChange) change);
        } else if (change instanceof DropForeignKeyConstraintChange) {
            handle(storage, (DropForeignKeyConstraintChange) change);
        } else if (change instanceof DropUniqueConstraintChange) {
            handle(storage, (DropUniqueConstraintChange) change);
        }
    }

    private void handle(ChangeStorage storage, DropUniqueConstraintChange dropUnique) {
        String tableName = dropUnique.getTableName();
        String constraintName = dropUnique.getConstraintName();

        storage.filterBy(ColumnInfo.class, new TableKey(tableName)).forEach(info -> {
            List<ConstraintInfo> constraints = info.getConstraints();
            constraints.removeIf(con -> (con.getName().equals(constraintName)));
        });
    }

    private void handle(ChangeStorage storage, DropForeignKeyConstraintChange dropForeignKey) {
        String tableName = dropForeignKey.getBaseTableName();
        String constraintName = dropForeignKey.getConstraintName();

        storage.filterBy(ColumnInfo.class, new TableKey(tableName)).forEach(info -> {
            List<ConstraintInfo> constraints = info.getConstraints();
            constraints.removeIf(con -> (con.getName().equals(constraintName)));
        });
    }

    private void handle(ChangeStorage storage, DropAllForeignKeyConstraintsChange dropAllForeignKeys) {
        String tableName = dropAllForeignKeys.getBaseTableName();

        storage.filterBy(ColumnInfo.class, new TableKey(tableName)).forEach(info -> {
            List<ConstraintInfo> constraints = info.getConstraints();
            constraints.removeIf(con -> (con.getType() == ConstraintType.FOREIGN_KEY));
        });
    }

    private void handle(ChangeStorage storage, DropPrimaryKeyChange dropPrimaryKey) {
        String tableName = dropPrimaryKey.getTableName();
        String constraintName = dropPrimaryKey.getConstraintName();

        storage.filterBy(ColumnInfo.class, new TableKey(tableName)).forEach(info -> {
            List<ConstraintInfo> constraints = info.getConstraints();
            constraints.removeIf(con -> (con.getName().equals(constraintName) || con.getType() == ConstraintType.PRIMARY_KEY));
        });
    }

    private void handle(ChangeStorage storage, DropIndexChange dropIndex) {
        String tableName = dropIndex.getTableName();
        String constraintName = dropIndex.getIndexName();

        storage.filterBy(ColumnInfo.class, new TableKey(tableName)).forEach(info -> {
            List<ConstraintInfo> constraints = info.getConstraints();
            constraints.removeIf(con -> (con.getName().equals(constraintName)));
        });
    }

    private void handle(ChangeStorage storage, AddForeignKeyConstraintChange addForeignKey) {
        String tableName = addForeignKey.getBaseTableName();
        String constraintName = addForeignKey.getConstraintName();
        String[] foreignKeyCols = addForeignKey.getBaseColumnNames().split(",\\s+");

        for (String foreignKeyCol : foreignKeyCols) {
            Optional<ColumnInfo> colInfo = storage.filterBy(ColumnInfo.class, new ColumnKey(tableName, foreignKeyCol)).findFirst();
            if (colInfo.isPresent()) {
                List<ConstraintInfo> consInfo = colInfo.get().getConstraints();
                ConstraintInfo cons = new ConstraintInfo(constraintName);
                cons.setType(ConstraintType.FOREIGN_KEY);
                consInfo.add(cons);
            }
        }
    }

    private void handle(ChangeStorage storage, AddPrimaryKeyChange addPrimaryKey) {
        String tableName = addPrimaryKey.getTableName();
        String constraintName = addPrimaryKey.getConstraintName();
        String[] primaryKeyCols = addPrimaryKey.getColumnNames().split(",\\s+");

        for (String primaryKeyCol : primaryKeyCols) {
            Optional<ColumnInfo> colInfo = storage.filterBy(ColumnInfo.class, new ColumnKey(tableName, primaryKeyCol)).findFirst();
            if (colInfo.isPresent()) {
                List<ConstraintInfo> consInfo = colInfo.get().getConstraints();
                ConstraintInfo cons = constraintName != null ? new ConstraintInfo(constraintName) : new ConstraintInfo();
                cons.setType(ConstraintType.PRIMARY_KEY);
                consInfo.add(cons);
            }
        }
    }

    private void handle(ChangeStorage storage, AddUniqueConstraintChange addUnique) {
        String tableName = addUnique.getTableName();
        String constraintName = addUnique.getConstraintName();
        String[] uniqueCols = addUnique.getColumnNames().split(",\\s+");

        for (String uniqueCol : uniqueCols) {
            Optional<ColumnInfo> colInfo = storage.filterBy(ColumnInfo.class, new ColumnKey(tableName, uniqueCol)).findFirst();
            if (colInfo.isPresent()) {
                List<ConstraintInfo> consInfo = colInfo.get().getConstraints();
                ConstraintInfo cons = constraintName != null ? new ConstraintInfo(constraintName) : new ConstraintInfo();
                cons.setType(ConstraintType.INDEX);
                consInfo.add(cons);
            }
        }
    }

    private void handle(ChangeStorage storage, CreateIndexChange createIndex) {
        String tableName = createIndex.getTableName();
        String indexName = createIndex.getIndexName();

        createIndex.getColumns().forEach(col -> {
            Optional<ColumnInfo> colInfo = storage.filterBy(ColumnInfo.class, new ColumnKey(tableName, col.getName())).findFirst();
            if (colInfo.isPresent()) {
                List<ConstraintInfo> consInfo = colInfo.get().getConstraints();
                ConstraintInfo cons = indexName != null ? new ConstraintInfo(indexName) : new ConstraintInfo();
                cons.setType(ConstraintType.INDEX);
                consInfo.add(cons);
            }
        });
    }

    private void handle(ChangeStorage storage, AddColumnChange addColumn) {
        String tableName = addColumn.getTableName();

        processColumns(storage, tableName, addColumn.getColumns());
    }

    private void handle(ChangeStorage storage, CreateTableChange createTable) {
        String tableName = createTable.getTableName();

        processColumns(storage, tableName, createTable.getColumns());
    }

    private void processColumns(ChangeStorage storage, String tableName, List<? extends ColumnConfig> columns) {
        for (ColumnConfig col : columns) {
            String colName = col.getName();
            ConstraintsConfig constraints = col.getConstraints();

            Optional<ColumnInfo> colInfo = storage.filterBy(ColumnInfo.class, new ColumnKey(tableName, colName)).findFirst();
            if (colInfo.isPresent()) {
                List<ConstraintInfo> consInfo = colInfo.get().getConstraints();

                if (constraints != null) {
                    if (BooleanUtils.isTrue(constraints.isPrimaryKey())) {
                        String primaryKeyName = constraints.getPrimaryKeyName();
                        ConstraintInfo cons = primaryKeyName != null ? new ConstraintInfo(primaryKeyName) : new ConstraintInfo();
                        cons.setType(ConstraintType.PRIMARY_KEY);
                        consInfo.add(cons);
                    }
                    if (BooleanUtils.isTrue(constraints.isUnique())) {
                        String uniqueName = constraints.getUniqueConstraintName();
                        ConstraintInfo cons = uniqueName != null ? new ConstraintInfo(uniqueName) : new ConstraintInfo();
                        cons.setType(ConstraintType.INDEX);
                        consInfo.add(cons);
                    }
                    if (StringUtils.isNotEmpty(constraints.getReferences())) {
                        String foreignKeyName = constraints.getForeignKeyName();
                        ConstraintInfo cons = foreignKeyName != null ? new ConstraintInfo(foreignKeyName) : new ConstraintInfo();
                        cons.setType(ConstraintType.FOREIGN_KEY);
                        consInfo.add(cons);
                    }
                }
                storage.put(colInfo.get().getKey(), colInfo.get());
            }
        }
    }

    @Override
    public boolean applyTo(Change change) { // NOSONAR
        return change instanceof CreateTableChange // NOSONAR
                || change instanceof AddColumnChange //
                || change instanceof CreateIndexChange //
                || change instanceof AddUniqueConstraintChange //
                || change instanceof AddPrimaryKeyChange //
                || change instanceof AddForeignKeyConstraintChange //
                || change instanceof DropIndexChange //
                || change instanceof DropUniqueConstraintChange //
                || change instanceof DropPrimaryKeyChange //
                || change instanceof DropForeignKeyConstraintChange //
                || change instanceof DropAllForeignKeyConstraintsChange;
    }

}
