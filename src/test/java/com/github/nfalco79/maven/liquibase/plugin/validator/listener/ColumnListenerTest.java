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

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.Test;

import com.github.nfalco79.maven.liquibase.plugin.validator.ValidatorFactory;

import liquibase.change.AddColumnConfig;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.AddNotNullConstraintChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropColumnChange;
import liquibase.change.core.DropTableChange;
import liquibase.change.core.ModifyDataTypeChange;
import liquibase.change.core.RenameColumnChange;
import liquibase.change.core.RenameTableChange;

public class ColumnListenerTest {

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
        ColumnConfig c2 = newColumn("t_key", "BYTEA", false);

        CreateTableChange change = new CreateTableChange();
        change.setTableName("comfin_configuration");
        change.addColumn(c1);
        change.addColumn(c2);

        ListenerValidatorFactory validatorFactory = new ListenerValidatorFactory();
        validatorFactory.newValidator(change);
        Assertions.assertThat(validatorFactory.getStorage()).hasSize(2);
    }

    @Test
    public void verify_AddColumn_update_storage() throws Exception {
        AddColumnConfig c1 = newColumn("tenant_id", "INT8", false);
        AddColumnConfig c2 = newColumn("t_key", "BYTEA", false);

        AddColumnChange change = new AddColumnChange();
        change.setTableName("comfin_configuration");
        change.addColumn(c1);
        change.addColumn(c2);

        ListenerValidatorFactory validatorFactory = new ListenerValidatorFactory();
        validatorFactory.newValidator(change);
        Assertions.assertThat(validatorFactory.getStorage()).hasSize(2) //
                .containsOnlyKeys( //
                        new ColumnKey(change.getTableName(), c1.getName()), //
                        new ColumnKey(change.getTableName(), c2.getName()));
    }

    @Test
    public void verify_DropTable_update_storage() throws Exception {
        ColumnConfig c1 = newColumn("tenant_id", "INT8", false);
        ColumnConfig c2 = newColumn("t_key", "BYTEA", false);

        CreateTableChange change = new CreateTableChange();
        change.setTableName("comfin_configuration");
        change.addColumn(c1);
        change.addColumn(c2);

        DropTableChange dropChange = new DropTableChange();
        dropChange.setTableName("comfin_configuration");

        ListenerValidatorFactory validatorFactory = new ListenerValidatorFactory();
        validatorFactory.newValidator(change);
        validatorFactory.newValidator(dropChange);
        Assertions.assertThat(validatorFactory.getStorage()).isEmpty();
    }

    @Test
    public void verify_DropColumn_update_storage() throws Exception {
        ColumnConfig c1 = newColumn("tenant_id", "INT8", false);
        ColumnConfig c2 = newColumn("t_key", "BYTEA", false);

        CreateTableChange change = new CreateTableChange();
        change.setTableName("comfin_configuration");
        change.addColumn(c1);
        change.addColumn(c2);

        DropColumnChange dropChange = new DropColumnChange();
        dropChange.setTableName("comfin_configuration");
        dropChange.addColumn(c2);

        ListenerValidatorFactory validatorFactory = new ListenerValidatorFactory();
        validatorFactory.newValidator(change);
        validatorFactory.newValidator(dropChange);
        Assertions.assertThat(validatorFactory.getStorage()).hasSize(1) //
                .containsOnlyKeys(new ColumnKey(change.getTableName(), c1.getName()));
    }

    @Test
    public void verify_RenameTable_update_storage() throws Exception {
        ColumnConfig c1 = newColumn("tenant_id", "INT8", false);
        ColumnConfig c2 = newColumn("t_key", "BYTEA", false);

        CreateTableChange change = new CreateTableChange();
        change.setTableName("comfin_configuration");
        change.addColumn(c1);
        change.addColumn(c2);

        RenameTableChange renameChange = new RenameTableChange();
        renameChange.setOldTableName("comfin_configuration");
        renameChange.setNewTableName("test");

        ListenerValidatorFactory validatorFactory = new ListenerValidatorFactory();
        validatorFactory.newValidator(change);
        validatorFactory.newValidator(renameChange);
        Assertions.assertThat(validatorFactory.getStorage()).hasSize(2) //
                .containsOnlyKeys( //
                        new ColumnKey(renameChange.getNewTableName(), c1.getName()), //
                        new ColumnKey(renameChange.getNewTableName(), c2.getName()));
    }

    @Test
    public void verify_RenameColumn_update_storage() throws Exception {
        ColumnConfig c1 = newColumn("tenant_id", "INT8", false);
        ColumnConfig c2 = newColumn("t_key", "BYTEA", false);

        CreateTableChange change = new CreateTableChange();
        change.setTableName("comfin_configuration");
        change.addColumn(c1);
        change.addColumn(c2);

        RenameColumnChange renameChange = new RenameColumnChange();
        renameChange.setTableName("comfin_configuration");
        renameChange.setOldColumnName("t_key");
        renameChange.setNewColumnName("key");

        ListenerValidatorFactory validatorFactory = new ListenerValidatorFactory();
        validatorFactory.newValidator(change);
        validatorFactory.newValidator(renameChange);
        Assertions.assertThat(validatorFactory.getStorage()).hasSize(2) //
                .containsOnlyKeys( //
                        new ColumnKey(change.getTableName(), c1.getName()), //
                        new ColumnKey(change.getTableName(), renameChange.getNewColumnName()));
    }

    @Test
    public void verify_AddNotNullConstraint_update_storage() throws Exception {
        ColumnConfig c1 = newColumn("tenant_id", "INT8", true);
        ColumnConfig c2 = newColumn("t_key", "BYTEA", true);

        CreateTableChange change = new CreateTableChange();
        change.setTableName("comfin_configuration");
        change.addColumn(c1);
        change.addColumn(c2);

        AddNotNullConstraintChange otherChange = new AddNotNullConstraintChange();
        otherChange.setTableName("comfin_configuration");
        otherChange.setColumnName("t_key");
        otherChange.setColumnDataType("BYTEA");

        ListenerValidatorFactory validatorFactory = new ListenerValidatorFactory();
        validatorFactory.newValidator(change);
        validatorFactory.newValidator(otherChange);

        Condition<IStorageInfo> c2IsNotNullCondition = new Condition<IStorageInfo>() {
            @Override
            public boolean matches(IStorageInfo value) {
                return value.getKey().equals(new ColumnKey(change.getTableName(), c2.getName())) //
                        && !((ColumnInfo) value).isNullable();
            }
        };

        Assertions.assertThat(validatorFactory.getStorage()).hasSize(2) //
                .containsOnlyKeys( //
                        new ColumnKey(change.getTableName(), c1.getName()), //
                        new ColumnKey(change.getTableName(), c2.getName())) //
                .hasValueSatisfying(c2IsNotNullCondition);
    }

    @Test
    public void verify_ModifyDataType_update_storage() throws Exception {
        ColumnConfig c1 = newColumn("tenant_id", "INTEGER", true);
        ColumnConfig c2 = newColumn("t_key", "VARCHAR(31)", true);

        CreateTableChange change = new CreateTableChange();
        change.setTableName("comfin_configuration");
        change.addColumn(c1);
        change.addColumn(c2);

        ModifyDataTypeChange otherChange = new ModifyDataTypeChange();
        otherChange.setTableName("comfin_configuration");
        otherChange.setColumnName("t_key");
        otherChange.setNewDataType("INTEGER");

        ListenerValidatorFactory validatorFactory = new ListenerValidatorFactory();
        validatorFactory.newValidator(change);
        validatorFactory.newValidator(otherChange);

        Condition<IStorageInfo> c2HasChangedTypeCondition = new Condition<IStorageInfo>() {
            @Override
            public boolean matches(IStorageInfo value) {
                return value.getKey().equals(new ColumnKey(change.getTableName(), c2.getName())) //
                        && ((ColumnInfo) value).getType().equals("INTEGER");
            }
        };

        Assertions.assertThat(validatorFactory.getStorage()).hasSize(2) //
                .containsOnlyKeys( //
                        new ColumnKey(change.getTableName(), c1.getName()), //
                        new ColumnKey(change.getTableName(), c2.getName())) //
                .hasValueSatisfying(c2HasChangedTypeCondition);
    }
}