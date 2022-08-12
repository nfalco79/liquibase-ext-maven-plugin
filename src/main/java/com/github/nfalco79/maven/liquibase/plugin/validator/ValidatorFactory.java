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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.github.nfalco79.maven.liquibase.plugin.validator.Validator.Scope;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ChangeStorage;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ColumnListener;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ConstraintListener;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.IChangeListener;

import liquibase.change.Change;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.AddNotNullConstraintChange;
import liquibase.change.core.AddUniqueConstraintChange;
import liquibase.change.core.AlterSequenceChange;
import liquibase.change.core.CreateIndexChange;
import liquibase.change.core.CreateSequenceChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.CreateViewChange;
import liquibase.change.core.DropAllForeignKeyConstraintsChange;
import liquibase.change.core.DropColumnChange;
import liquibase.change.core.DropIndexChange;
import liquibase.change.core.InsertDataChange;
import liquibase.change.core.ModifyDataTypeChange;
import liquibase.change.core.RenameColumnChange;
import liquibase.changelog.ChangeSet;
import liquibase.ext.nfalco79.CopyColumnChange;
import liquibase.ext.nfalco79.ResizeDataTypeChange;

public class ValidatorFactory {

    public static final String CATALOG_NAME = "catalogName";
    public static final String SCHEMA_NAME = "schemaName";
    public static final String CONSTRAINT_NAME = "constraintName";
    public static final String TABLE_NAME = "tableName";
    public static final String COLUMN_NAME = "columnName";
    public static final String COLUMN_NAMES = "columnNames";
    public static final String BASE_COLUMN_NAMES = "baseColumnNames";
    public static final String BASE_TABLE_CATALOG_NAME = "baseTableCatalogName";
    public static final String BASE_TABLE_NAME = "baseTableName";
    public static final String BASE_TABLE_SHEMA_NAME = "baseTableSchemaName";
    public static final String REFERENCED_TABLE_CATALOG_NAME = "referencedTableCatalogName";
    public static final String REFERENCED_TABLE_NAME = "referencedTableName";
    public static final String REFERENCED_TABLE_SCHEMA_NAME = "referencedTableSchemaName";
    public static final String NEW_COLUMN_NAME = "newColumnName";
    public static final String SEQUENCE_NAME = "sequenceName";
    public static final String INDEX_NAME = "indexName";
    public static final String OLD_COLUMN_NAME = "oldColumnName";
    public static final String NEW_SEQUENCE_NAME = "newSequenceName";
    public static final String OLD_SEQUENCE_NAME = "oldSequenceName";
    public static final String START_VALUE = "startValue";
    public static final String INCREMENT_BY = "incrementBy";
    public static final String DEFAULT_VALUE_COMPUTED = "defaultValueComputed";
    public static final String NEW_DATA_TYPE = "newDataType";

    private Set<String> includeChanges;
    private Set<String> excludeChanges;
    private Set<String> ignoreRules;
    private Set<Object> extraValidators = new LinkedHashSet<>();
    private Map<String, Object> singletons = new HashMap<>();
    private ChangeStorage storage;
    private Collection<IChangeListener> listeners = new CopyOnWriteArrayList<IChangeListener>();

    /**
     * Constructs an instance of this class.
     */
    public ValidatorFactory() {
        storage = new ChangeStorage();
        register(new ColumnListener());
        register(new ConstraintListener());
    }

    /**
     * Register the given listener.
     *
     * @param listener
     *            the listener to add
     */
    protected void register(IChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Notify the listeners of same Liquibase change to update the storage information.
     *
     * @param change
     *            the change passed
     */
    protected void notify(Change change) {
        Iterator<IChangeListener> it = listeners.iterator();
        while (it.hasNext()) {
            it.next().updateStorage(change, storage);
        }
    }

    /**
     * Adds a singleton instance of this change validator.
     *
     * @param validator
     *            to add
     * @return if it is added or not.
     */
    public boolean addValidator(IChangeValidator validator) {
        return extraValidators.add(validator);
    }

    /**
     * Adds a singleton instance of this changeSet validator.
     *
     * @param validator
     *            to add
     * @return if it is added or not.
     */
    public boolean addValidator(IChangeSetValidator validator) {
        return extraValidators.add(validator);
    }

    /**
     * Adds a singleton instance of this changeLog validator.
     *
     * @param validator
     *            to add
     * @return if it is added or not.
     */
    public boolean addValidator(IChangeLogValidator validator) {
        return extraValidators.add(validator);
    }

    /**
     * Create new validator collecting every possible validation for the given change.
     *
     * @param change
     *            the change to validate
     * @return the new composed validator.
     */
    public IChangeValidator newValidator(Change change) { // NOSONAR
        notify(change);

        Class<? extends Change> clazz = change.getClass();
        Collection<IChangeValidator> validators = new LinkedList<>(extraValidators.stream() //
                .filter(IChangeValidator.class::isInstance) //
                .map(IChangeValidator.class::cast) //
                .collect(Collectors.toList()));

        validators.add(scopedValidator(new IncludeExcludeChange(includeChanges, excludeChanges)));

        validators.add(scopedValidator(new NotPermittedValidator(CATALOG_NAME, SCHEMA_NAME, //
            BASE_TABLE_CATALOG_NAME, BASE_TABLE_SHEMA_NAME, //
            REFERENCED_TABLE_CATALOG_NAME, REFERENCED_TABLE_SCHEMA_NAME, //
            DEFAULT_VALUE_COMPUTED)));

        validators.add(scopedValidator(new LowerCaseValidator(TABLE_NAME, COLUMN_NAME, BASE_COLUMN_NAMES, //
            CONSTRAINT_NAME, BASE_TABLE_NAME, REFERENCED_TABLE_NAME, //
            COLUMN_NAMES, SEQUENCE_NAME, INDEX_NAME, //
            NEW_COLUMN_NAME, OLD_COLUMN_NAME, //
            NEW_SEQUENCE_NAME, OLD_SEQUENCE_NAME)));

        validators.add(scopedValidator(new Oracle11gLengthValidator()));

        if (AddColumnChange.class.isAssignableFrom(clazz)) {
            validators.add(scopedValidator(new ColumnsValidator(1, 30, 31)));
            validators.add(scopedValidator(new Oracle11gCLOBValidator()));
            validators.add(scopedValidator(new TenantIdNotNullValidator()));
            validators.add(scopedValidator(new ColumnNotNullConstraintValidator()));
            // In db2 a 4KB page size with regular table space implies a row size limit of 4005B.
            validators.add(scopedValidator((new TableSizeValidator(4005))));
            //validators.add(scopedValidator(new DefaultValueColumnValidator()));
        }
        if (AddNotNullConstraintChange.class.isAssignableFrom(clazz)) {
            validators.add(scopedValidator(new ColumnNotNullConstraintValidator()));
        }
        if (AlterSequenceChange.class.isAssignableFrom(clazz)) {
            validators.add(scopedValidator(new NotPermittedValidator(START_VALUE, INCREMENT_BY)));
        }
        // If change type is ResizeDataTypeChange, both the following statements are run
        if (ModifyDataTypeChange.class.isAssignableFrom(clazz)) {
            validators.add(scopedValidator(new ModifyDataTypeValidator()));
            if (ModifyDataTypeChange.class == clazz) {
                validators.add(scopedValidator(new Oracle11gModifyDataTypeValidator()));
            }
            validators.add(scopedValidator(new NumericValidator(31, NEW_DATA_TYPE)));
            validators.add(scopedValidator(new LOBValidator(NEW_DATA_TYPE)));
            validators.add(scopedValidator((new TableSizeValidator(4005))));
            validators.add(scopedValidator((new ColumnConstraintsValidator())));
        }
        if (ResizeDataTypeChange.class.isAssignableFrom(clazz)) {
            validators.add(scopedValidator(new Oracle11gCLOBValidator()));
        }
        if (CreateIndexChange.class.isAssignableFrom(clazz)) {
            validators.add(scopedValidator(new ColumnsValidator(1, 30, 31)));
            validators.add(scopedValidator(new DuplicatedIndexValidator()));
            // The limit for index key (or row) size in db2 is the page size divided by 4: for a 4KB page size, it is 1024B.
            validators.add(scopedValidator(new CreateIndexValidator(1024)));
        }
        if (CreateSequenceChange.class.isAssignableFrom(clazz)) {
            validators.add(scopedValidator(new RangeLengthValidator(1, 30, SEQUENCE_NAME)));
            validators.add(scopedValidator(new SequenceValidator()));
        }
        if (CreateTableChange.class.isAssignableFrom(clazz)) {
            validators.add(scopedValidator(new ColumnsValidator(1, 30, 31)));
            validators.add(scopedValidator(new Oracle11gCLOBValidator()));
            validators.add(scopedValidator(new TenantIdNotNullValidator()));
            validators.add(scopedValidator((new TableSizeValidator(4005))));
            //validators.add(scopedValidator(new DefaultValueColumnValidator()));
        }
        if (DropAllForeignKeyConstraintsChange.class.isAssignableFrom(clazz)) {
            validators.add(scopedValidator(new NotPermittedValidator(BASE_TABLE_CATALOG_NAME, BASE_TABLE_SHEMA_NAME)));
        }
        if (DropColumnChange.class.isAssignableFrom(clazz)) {
            validators.add(scopedValidator(new ColumnsValidator(1, 30, 31)));
            validators.add(scopedValidator(new DropColumnValidator()));
        }
        if (DropIndexChange.class.isAssignableFrom(clazz)) {
            validators.add(scopedValidator(new DuplicatedIndexValidator()));
        }
        if (CopyColumnChange.class.isAssignableFrom(clazz)) {
            validators.add(scopedValidator(new CopyColumnValidator()));
        }
        if (InsertDataChange.class.isAssignableFrom(clazz)) {
            validators.add(scopedValidator(new InsertDataChangeValidator()));
            validators.add(scopedValidator(new InsertColumnsValidator()));
            //validators.add(scopedValidator(new DefaultValueColumnValidator()));
        }
        if (AddUniqueConstraintChange.class.isAssignableFrom(clazz)) {
            validators.add(scopedValidator(new AddUniqueConstraintValidator()));
        }
        if (RenameColumnChange.class.isAssignableFrom(clazz)) {
            validators.add(scopedValidator((new ColumnConstraintsValidator())));
        }
        if (CreateViewChange.class.isAssignableFrom(clazz)) {
            validators.add(scopedValidator((new ReplaceViewValidator())));
        }
        return new CompositeValidator(validators, ignoreRules, storage);
    }

    @SuppressWarnings("unchecked")
    protected <T> T scopedValidator(T validator) {
        T validatorInstance = validator;

        Validator annotation = validator.getClass().getAnnotation(Validator.class);
        if (annotation == null) {
            throw new IllegalStateException("Validator " + validator.getClass() + " lacks of @Validator annotation");
        }
        if (annotation.scope() == Scope.SINGLETON) {
            String name = annotation.name();
            if (!singletons.containsKey(name)) {
                singletons.put(name, validator);
            } else {
                validatorInstance = (T) singletons.get(name);
            }
        }

        return validatorInstance;
    }

    /**
     * Returns a validator for changeset.
     *
     * @param changeSet
     *            the changeset to validate
     * @return the changeset validator
     */
    public IChangeSetValidator newChangeSetValidator(ChangeSet changeSet) {
        Collection<IChangeSetValidator> validators = new LinkedList<>(extraValidators.stream() //
                .filter(IChangeSetValidator.class::isInstance) //
                .map(IChangeSetValidator.class::cast) //
                .collect(Collectors.toList()));

        validators.add(scopedValidator(new DuplicatedIdValidator()));
        validators.add(scopedValidator(new CheckSumValidator()));
        return new CompositeChangeSetValidator(validators, ignoreRules);
    }

    public void setIncludeChanges(Set<String> includeChanges) {
        this.includeChanges = Collections.unmodifiableSet(includeChanges);
    }

    public void setExcludeChanges(Set<String> excludeChanges) {
        this.excludeChanges = Collections.unmodifiableSet(excludeChanges);
    }

    public void setIgnoreRules(Set<String> ignoreRules) {
        this.ignoreRules = Collections.unmodifiableSet(ignoreRules);
    }

    protected ChangeStorage getStorage() {
        return storage;
    }

}