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

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.github.nfalco79.maven.liquibase.plugin.validator.ValidatorFactory;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ConstraintInfo.ConstraintType;

import liquibase.change.AddColumnConfig;
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

public class ConstraintListenerTest {

    /*
     * Internal class to expose change storage.
     */
    private class ListenerValidatorFactory extends ValidatorFactory {
        @Override
        public ChangeStorage getStorage() {
            return super.getStorage();
        }
    }

    private AddColumnConfig newColumn(String name, String type, boolean nullable) {
        AddColumnConfig cc = new AddColumnConfig();
        cc.setName(name);
        cc.setType(type);
        if (!nullable) {
            ConstraintsConfig constraint = new ConstraintsConfig();
            constraint.setNullable(nullable);
            cc.setConstraints(constraint);
        }
        return cc;
    }

    @Test
    public void verify_CreateTable_update_storage() throws Exception {
        ColumnConfig c1 = newColumn("tenant_id", "INT8", false);
        ColumnConfig c2 = new AddColumnConfig();
        c2.setName("col2");
        c2.setType("VARCHAR");
        ConstraintsConfig constraint = new ConstraintsConfig();
        constraint.setPrimaryKey(true);
        constraint.setPrimaryKeyName("prim_key_t1");
        c2.setConstraints(constraint);

        CreateTableChange change = new CreateTableChange();
        change.setTableName("t1");
        change.addColumn(c1);
        change.addColumn(c2);

        ListenerValidatorFactory validatorFactory = new ListenerValidatorFactory();
        validatorFactory.newValidator(change);
        Assertions.assertThat(validatorFactory.getStorage() //
                .filterBy(ColumnInfo.class, new ColumnKey("t1", "col2")) //
                .allMatch(col -> (!col.getConstraints().isEmpty()))).isTrue();
    }

    @Test
    public void verify_AddColumn_update_storage() throws Exception {
        AddColumnConfig c1 = newColumn("tenant_id", "INT8", false);
        AddColumnConfig c2 = new AddColumnConfig();
        c2.setName("col2");
        c2.setType("VARCHAR");
        ConstraintsConfig constraint = new ConstraintsConfig();
        constraint.setUnique(true);
        c2.setConstraints(constraint);

        AddColumnChange change = new AddColumnChange();
        change.setTableName("t1");
        change.addColumn(c1);
        change.addColumn(c2);

        ListenerValidatorFactory validatorFactory = new ListenerValidatorFactory();
        validatorFactory.newValidator(change);
        Assertions.assertThat(validatorFactory.getStorage() //
                .filterBy(ColumnInfo.class, new ColumnKey("t1", "col2")) //
                .allMatch(col -> (!col.getConstraints().isEmpty()))).isTrue();
    }

    @Test
    public void verify_CreateIndex_update_storage() throws Exception {
        AddColumnConfig c1 = newColumn("tenant_id", "INT8", false);
        AddColumnConfig c2 = newColumn("col2", "INTEGER", false);

        CreateTableChange change = new CreateTableChange();
        change.setTableName("t1");
        change.addColumn(c1);
        change.addColumn(c2);

        CreateIndexChange createIndex = new CreateIndexChange();
        createIndex.setTableName("t1");
        List<AddColumnConfig> cols = new ArrayList<AddColumnConfig>();
        cols.add(c2);
        createIndex.setColumns(cols);

        ListenerValidatorFactory validatorFactory = new ListenerValidatorFactory();
        validatorFactory.newValidator(change);
        validatorFactory.newValidator(createIndex);
        Assertions.assertThat(validatorFactory.getStorage() //
                .filterBy(ColumnInfo.class, new ColumnKey("t1", "col2")) //
                .allMatch(col -> (col.getConstraints().get(0).getType() == ConstraintType.INDEX))) //
                .isTrue();
    }

    @Test
    public void verify_DropIndex_update_storage() throws Exception {
        AddColumnConfig c1 = newColumn("tenant_id", "INT8", false);
        AddColumnConfig c2 = newColumn("col2", "INTEGER", false);

        CreateTableChange change = new CreateTableChange();
        change.setTableName("t1");
        change.addColumn(c1);
        change.addColumn(c2);

        CreateIndexChange createIndex = new CreateIndexChange();
        createIndex.setTableName("t1");
        List<AddColumnConfig> cols = new ArrayList<AddColumnConfig>();
        cols.add(c2);
        createIndex.setColumns(cols);
        createIndex.setIndexName("ind_1");

        DropIndexChange dropIndex = new DropIndexChange();
        dropIndex.setTableName("t1");
        dropIndex.setIndexName("ind_1");

        ListenerValidatorFactory validatorFactory = new ListenerValidatorFactory();
        validatorFactory.newValidator(change);
        validatorFactory.newValidator(createIndex);
        validatorFactory.newValidator(dropIndex);
        Assertions.assertThat(validatorFactory.getStorage() //
                .filterBy(ColumnInfo.class, new ColumnKey("t1", "col2")) //
                .allMatch(col -> (col.getConstraints().isEmpty()))) //
                .isTrue();
    }

    @Test
    public void verify_AddUniqueConstraint_update_storage() throws Exception {
        AddColumnConfig c1 = newColumn("tenant_id", "INT8", false);
        AddColumnConfig c2 = newColumn("col2", "INTEGER", true);
        AddColumnConfig c3 = newColumn("col3", "CHAR", true);

        CreateTableChange change = new CreateTableChange();
        change.setTableName("t1");
        change.addColumn(c1);
        change.addColumn(c2);
        change.addColumn(c3);

        AddUniqueConstraintChange addUnique = new AddUniqueConstraintChange();
        addUnique.setTableName("t1");
        List<String> cols = new ArrayList<String>();
        cols.add(c2.getName());
        cols.add(c3.getName());
        addUnique.setColumnNames(StringUtils.join(cols, ", "));

        ListenerValidatorFactory validatorFactory = new ListenerValidatorFactory();
        validatorFactory.newValidator(change);
        validatorFactory.newValidator(addUnique);
        Assertions.assertThat(validatorFactory.getStorage() //
                .filterBy(ColumnInfo.class, new ColumnKey("t1", "col3")) //
                .allMatch(col -> (col.getConstraints().get(0).getType() == ConstraintType.INDEX))) //
                .isTrue();
    }

    @Test
    public void verify_DropUniqueConstraint_update_storage() throws Exception {
        AddColumnConfig c1 = newColumn("tenant_id", "INT8", false);
        AddColumnConfig c2 = newColumn("col2", "INTEGER", true);
        AddColumnConfig c3 = newColumn("col3", "CHAR", true);

        CreateTableChange change = new CreateTableChange();
        change.setTableName("t1");
        change.addColumn(c1);
        change.addColumn(c2);
        change.addColumn(c3);

        AddUniqueConstraintChange addUnique = new AddUniqueConstraintChange();
        addUnique.setTableName("t1");
        List<String> cols = new ArrayList<String>();
        cols.add(c2.getName());
        cols.add(c3.getName());
        addUnique.setColumnNames(StringUtils.join(cols, ", "));
        addUnique.setConstraintName("unique_const");

        DropUniqueConstraintChange dropUnique = new DropUniqueConstraintChange();
        dropUnique.setTableName("t1");
        dropUnique.setConstraintName("unique_const");

        ListenerValidatorFactory validatorFactory = new ListenerValidatorFactory();
        validatorFactory.newValidator(change);
        validatorFactory.newValidator(addUnique);
        validatorFactory.newValidator(dropUnique);
        Assertions.assertThat(validatorFactory.getStorage() //
                .filterBy(ColumnInfo.class, new ColumnKey("t1", "col3")) //
                .allMatch(col -> (col.getConstraints().isEmpty()))) //
                .isTrue();
    }

    @Test
    public void verify_AddPrimaryKey_update_storage() throws Exception {
        AddColumnConfig c1 = newColumn("tenant_id", "INTEGER", false);

        CreateTableChange change = new CreateTableChange();
        change.setTableName("t1");
        change.addColumn(c1);

        AddPrimaryKeyChange addPrimary = new AddPrimaryKeyChange();
        addPrimary.setTableName("t1");
        List<String> cols = new ArrayList<String>();
        cols.add(c1.getName());
        addPrimary.setColumnNames(StringUtils.join(cols, ", "));

        ListenerValidatorFactory validatorFactory = new ListenerValidatorFactory();
        validatorFactory.newValidator(change);
        validatorFactory.newValidator(addPrimary);
        Assertions.assertThat(validatorFactory.getStorage() //
                .filterBy(ColumnInfo.class, new TableKey("t1")) //
                .allMatch(col -> (col.getConstraints().get(0).getType() == ConstraintType.PRIMARY_KEY))) //
                .isTrue();
    }

    @Test
    public void verify_DropPrimaryKey_update_storage() throws Exception {
        AddColumnConfig c1 = newColumn("tenant_id", "INTEGER", false);

        CreateTableChange change = new CreateTableChange();
        change.setTableName("t1");
        change.addColumn(c1);

        AddPrimaryKeyChange addPrimary = new AddPrimaryKeyChange();
        addPrimary.setTableName("t1");
        List<String> cols = new ArrayList<String>();
        cols.add(c1.getName());
        addPrimary.setColumnNames(StringUtils.join(cols, ", "));

        DropPrimaryKeyChange dropPrimary = new DropPrimaryKeyChange();
        dropPrimary.setTableName("t1");

        ListenerValidatorFactory validatorFactory = new ListenerValidatorFactory();
        validatorFactory.newValidator(change);
        validatorFactory.newValidator(addPrimary);
        validatorFactory.newValidator(dropPrimary);
        Assertions.assertThat(validatorFactory.getStorage() //
                .filterBy(ColumnInfo.class, new TableKey("t1")) //
                .allMatch(col -> (col.getConstraints().isEmpty()))) //
                .isTrue();
    }

    @Test
    public void verify_AddForeignKey_update_storage() throws Exception {
        AddColumnConfig c1 = newColumn("tenant_id1", "INTEGER", false);
        AddColumnConfig c2 = newColumn("tenant_id2", "INTEGER", false);

        CreateTableChange createTable1 = new CreateTableChange();
        createTable1.setTableName("t1");
        createTable1.addColumn(c1);

        CreateTableChange createTable2 = new CreateTableChange();
        createTable2.setTableName("t2");
        createTable2.addColumn(c2);

        AddForeignKeyConstraintChange addForeign = new AddForeignKeyConstraintChange();
        addForeign.setBaseTableName("t1");
        addForeign.setReferencedTableName("t2");
        addForeign.setBaseColumnNames(c1.getName());
        addForeign.setReferencedColumnNames(c2.getName());

        ListenerValidatorFactory validatorFactory = new ListenerValidatorFactory();
        validatorFactory.newValidator(createTable1);
        validatorFactory.newValidator(createTable2);
        validatorFactory.newValidator(addForeign);
        Assertions.assertThat(validatorFactory.getStorage() //
                .filterBy(ColumnInfo.class, new TableKey("t1")) //
                .allMatch(col -> (col.getConstraints().get(0).getType() == ConstraintType.FOREIGN_KEY))) //
                .isTrue();
    }

    @Test
    public void verify_DropForeignKey_update_storage() throws Exception {
        AddColumnConfig c1 = newColumn("tenant_id1", "INTEGER", false);
        AddColumnConfig c2 = newColumn("tenant_id2", "INTEGER", false);

        CreateTableChange createTable1 = new CreateTableChange();
        createTable1.setTableName("t1");
        createTable1.addColumn(c1);

        CreateTableChange createTable2 = new CreateTableChange();
        createTable2.setTableName("t2");
        createTable2.addColumn(c2);

        AddForeignKeyConstraintChange addForeign = new AddForeignKeyConstraintChange();
        addForeign.setBaseTableName("t1");
        addForeign.setReferencedTableName("t2");
        addForeign.setBaseColumnNames(c1.getName());
        addForeign.setReferencedColumnNames(c2.getName());
        addForeign.setConstraintName("fk_t1");

        DropForeignKeyConstraintChange dropForeign = new DropForeignKeyConstraintChange();
        dropForeign.setBaseTableName("t1");
        dropForeign.setConstraintName("fk_t1");

        ListenerValidatorFactory validatorFactory = new ListenerValidatorFactory();
        validatorFactory.newValidator(createTable1);
        validatorFactory.newValidator(createTable2);
        validatorFactory.newValidator(addForeign);
        validatorFactory.newValidator(dropForeign);
        Assertions.assertThat(validatorFactory.getStorage() //
                .filterBy(ColumnInfo.class, new TableKey("t1")) //
                .allMatch(col -> (col.getConstraints().isEmpty()))) //
                .isTrue();
    }

    @Test
    public void verify_DropAllForeignKey_update_storage() throws Exception {
        AddColumnConfig c1 = newColumn("tenant_id1", "INTEGER", false);
        AddColumnConfig c2 = newColumn("col2", "INTEGER", true);
        AddColumnConfig c1t2 = newColumn("tenant_id2", "INTEGER", false);

        CreateTableChange createTable1 = new CreateTableChange();
        createTable1.setTableName("t1");
        createTable1.addColumn(c1);
        createTable1.addColumn(c2);

        CreateTableChange createTable2 = new CreateTableChange();
        createTable2.setTableName("t2");
        createTable2.addColumn(c1t2);

        AddForeignKeyConstraintChange addForeign1 = new AddForeignKeyConstraintChange();
        addForeign1.setBaseTableName("t1");
        addForeign1.setReferencedTableName("t2");
        addForeign1.setBaseColumnNames(c1.getName());
        addForeign1.setReferencedColumnNames(c2.getName());
        addForeign1.setConstraintName("fk_t1");

        AddForeignKeyConstraintChange addForeign2 = new AddForeignKeyConstraintChange();
        addForeign2.setBaseTableName("t1");
        addForeign2.setReferencedTableName("t2");
        addForeign2.setBaseColumnNames(c2.getName());
        addForeign2.setReferencedColumnNames(c1t2.getName());
        addForeign2.setConstraintName("fk2_t1");

        DropAllForeignKeyConstraintsChange dropForeign = new DropAllForeignKeyConstraintsChange();
        dropForeign.setBaseTableName("t1");

        ListenerValidatorFactory validatorFactory = new ListenerValidatorFactory();
        validatorFactory.newValidator(createTable1);
        validatorFactory.newValidator(createTable2);
        validatorFactory.newValidator(addForeign1);
        validatorFactory.newValidator(addForeign2);
        validatorFactory.newValidator(dropForeign);
        Assertions.assertThat(validatorFactory.getStorage() //
                .filterBy(ColumnInfo.class, new TableKey("t1")) //
                .allMatch(col -> (col.getConstraints().isEmpty()))) //
                .isTrue();
    }
}